import functools
import sys
import unittest

import xml.etree.ElementTree as ET
import xml.dom.minidom as minidom

from io import StringIO
from pathlib import Path
from shutil import rmtree
from unittest.mock import patch

from ccg2xml import main as ccg2xml
from xml2ccg import xml2ccg, XMLGrammar


TEST_DATA = Path(__file__).parent.parent.parent / 'test' / 'ccg2xml'
TEMP_DATA = Path(__file__).parent.parent.parent / 'test' / 'ccg2xml' / 'tmp'


def xml2ccg_argv(grammar):
    """Prepares the command line arguments for xml2ccg.

    Args:
        grammar: The grammar to be used.
    """
    args = ['xml2ccg', str(TEST_DATA / grammar)]
    return patch.object(sys, 'argv', args)


def ccg2xml_argv(grammar):
    """Prepares the command line arguments for ccg2xml.

    Args:
        grammar: The grammar to be used.
    """
    args = ['ccg2xml', '-',
            '--prefix', '{}-'.format(grammar),
            '--quiet',
            '--dir', str(TEMP_DATA / grammar)]
    return patch.object(sys, 'argv', args)


class SysIn:
    """Wraps sysin so that the input of ccg2xml can be captured easily."""
    def __enter__(self):
        self.original_stdin = sys.stdin
        sys.stdin = StringIO()
        return sys.stdin

    def __exit__(self, exc_type, exc_value, traceback):
        sys.stdin = self.original_stdin


class SysOut:
    """Wraps sysout so that the output of xml2ccg can be captured easily."""
    def __enter__(self):
        self.original_stdout = sys.stdout
        sys.stdout = StringIO()
        return sys.stdout

    def __exit__(self, exc_type, exc_value, traceback):
        sys.stdout = self.original_stdout


def compare_type_elements(left, right):
    """Compares two <type ... /> elements."""
    if left.tag != 'type' != right.tag:
        return False
    if left.get('name') != right.get('name'):
        return False
    if sorted(left.get('parents').split()) != sorted(right.get('parents').split()):
        return False
    return True


def compare_file_tags(left, right):
    """Compares two elements with a file attribute <... file="..." />.
    Returns True if they are the same, except for different filenames."""
    if 'file' not in left.attrib.keys() and 'file' not in right.attrib.keys():
        return False
    if left.tag != right.tag:
        return False
    return True


def flatten_morph_macros_with_fs_feat_val(tree):
    """ccg2xml does not handle the special case mentioned in tiny ccg correctly.

    A feature of the form

        case<0>: acc0:p-case;

    should be converted to

        <macro name="@acc0">
          <fs id="0" attr="case" val="p-case"/>
        </macro>

    but instead is converted to

        <macro name="@acc0">
          <fs id="0">
            <feat attr="case" val="p-case" />
          </fs>
        </macro>

    So far, it seems that this is the only case where feat actually gets
    assigned a val, thus this method fixes the situation by replacing
    all fs inside a macro inside the tree with a single-element version if and
    only if a val is present for the feat element.
    """
    for macro in tree.findall('macro'):
        fs = macro.find('fs')
        if fs is None:
            continue
        feat = fs.find('feat')
        if feat is None:
            continue
        val = feat.get('val')
        if val is None:
            continue

        fs.attrib['attr'] = feat.get('attr')
        fs.attrib['val'] = val
        fs.remove(feat)
    return tree


def tree_sort_key(elem):
    return elem.tag + str(sorted('{}={}'.format(*a) for a in elem.attrib.items()))


def recursive_tree_comparison(left, right):
    if left.tag != right.tag:
        return False
    elif left.attrib != right.attrib:
        return False
    elif left.find('*') is not None and right.find('*') is None:
        return False
    elif left.find('*') is None:
        return True
    else:
        return all(recursive_tree_comparison(l, r) for l, r in zip(iter(left), iter(right)))


def compare_grammar_tree(left, right):
    """Compares two XML tree structures.

    If left is None, the comparison is skipped.

    Args:
        left: The left tree.
        right: The right tree.
    """

    # Skip non-existent original files
    if left is None:
        return True, (None, None)

    for l in left.findall('entry'):
        if l.get('stem') is None and l.get('word') is not None:
            l.attrib['stem'] = l.get('word')

    right = flatten_morph_macros_with_fs_feat_val(right)

    # findall('*') selects children, so the root element is skipped on purpose,
    # as its "name" attribute differs depending on how ccg2xml is called.
    list_left = sorted(left.findall('*'), key=tree_sort_key)
    list_right = sorted(right.findall('*'), key=tree_sort_key)

    iter_right = iter(list_right)
    r = None
    for l in list_left:
        equal = False
        first_r = None
        while not equal:
            try:
                r = next(iter_right)
                if first_r is None:
                    first_r = r
            except StopIteration:
                return False, (l, first_r)

            if recursive_tree_comparison(l, r):
                break

            for comp in [compare_type_elements, compare_file_tags]:
                if comp(l, r):
                    equal = True
                    break

    return True, (None, None)


def compare_xmls(test_instance, grammar):
    """Loads the TEST and TEMP grammars specified by grammar using XMLGrammar.

    Each individual part, as specified in XMLGrammar.valid_filenames, is
    compared and asserted. This is collected as "soft asserts" and reported
    completely at the end.

    Args:
        test_instance: Should be a test object, usually "self" when using
                       unittest.
        grammar: The grammar to be used.

    Raises:
        AssertionError if not all parts of the grammars are equal.
    """
    original = XMLGrammar(TEST_DATA / grammar)
    generated = XMLGrammar(TEMP_DATA / grammar)

    test_map = {}
    for fn in XMLGrammar.valid_filenames:
        test, problem = compare_grammar_tree(getattr(original, fn),
                                             getattr(generated, fn))
        test_map[fn] = {
            'original': problem[0],
            'generated': problem[1],
            'test': test,
        }

    assert all(test_map[k]['test'] for k in test_map.keys()), generate_message(test_map)


def generate_message(test_map):
    """Generates a slightly more informative assertion message in case a test fails.

    Args:
        test_map: A test map of the form:
                    {
                        'test_key': {
                            'original': the expected value,
                            'generated': the actual value,
                            'test': the test result (True or False)
                        }
                        ...
                    }
    Returns:
        A string listing each test key with its original and generated values,
        but only for those where the test value is False.
    """
    tmpl = """{fn} test failed:
    Generated:
    {generated}

    Original:
    {original}
    """
    messages = []

    def prettify(xml):
        lines = []
        pretty = minidom.parseString(ET.tostring(xml)).toprettyxml()
        for line in pretty.splitlines():
            if len(line.strip()) != 0:
                lines.append(line)
        return '\n    '.join(lines)

    for k, v in test_map.items():
        if v['test']:
            continue
        generated = prettify(v['generated'])
        original = prettify(v['original'])
        messages.append(tmpl.format(fn=k, generated=generated, original=original))

    return '\n'.join(['The two grammars do not match.'] + messages)


def test_grammar(grammar):
    """Simplifies access to multiple similar test cases.

    A function annotated with test_grammar converts the grammar inside the
    TEST_DATA directory with xml2ccg into a ccg file and passes it to ccg2xml.
    ccg2xml in turn outputs the new set of xml files into TEMP_DATA / grammar.

    Then, both xml grammars are loaded and their xml files compared.

    Args:
        grammar: The grammar to be used.
    """
    def wrapper(func):
        @functools.wraps(func)
        def test_function(self, *args, **kwargs):

            with xml2ccg_argv(grammar), SysOut() as sout:
                xml2ccg()
                generated_ccg = sout.getvalue()

            with ccg2xml_argv(grammar), SysIn() as sin:
                sin.write(generated_ccg)
                sin.seek(0)
                ccg2xml()

            compare_xmls(self, grammar)

        return test_function
    return wrapper


class TestXML2CCG(unittest.TestCase):
    """This class tests the xml2ccg functionality in conjunction with ccg2xml.

    While the test methods only use the generated XML files present in
    subdirectories of ${OPENCCG_HOME}/test/ccg2xml, the doc strings of each
    test method mention the original source files.

    There are some *.ccg files inside the ${OPENCCG_HOME}/test/ccg2xml
    directory. Those are specialized test cases and also available as
    pre-compiled xml grammar directories.
    """

    @test_grammar('grammar_template')
    def test_grammar_template(self):
        """${OPENCCG_HOME}/src/ccg2xml/grammar_template.ccg"""
        pass

    @test_grammar('tinytiny')
    def test_tinytiny(self):
        """${OPENCCG_HOME}/ccg-format-grammars/tinytiny/tinytiny.ccg"""
        pass

    @test_grammar('tiny')
    def test_tiny(self):
        """${OPENCCG_HOME}/ccg-format-grammars/tiny/tiny.ccg"""
        pass

    @test_grammar('inherit')
    def test_inherit(self):
        """${OPENCCG_HOME}/ccg-format-grammars/inherit/inherit.ccg"""
        pass

    @test_grammar('arabic')
    def test_arabic(self):
        """${OPENCCG_HOME}/ccg-format-grammars/arabic/arabic.ccg
           ${OPENCCG_HOME}/src/ccg2xml/arabic.ccg
           (Both files are the same)
        """
        pass

    @test_grammar('diaspace')
    def test_diaspace(self):
        """${OPENCCG_HOME}/ccg-format-grammars/diaspace/diaspace.ccg"""
        pass

    @classmethod
    def tearDownClass(cls):
        """Removes the temp data after the tests."""
        rmtree(TEMP_DATA)
