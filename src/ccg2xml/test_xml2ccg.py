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


def compare_grammar_tree(left, right):
    """Compares two XML tree structures.

    If left is None, the comparison is skipped.

    Args:
        left: The left tree.
        right: The right tree.
    """
    # Special cases
    def compare_type_elements(l, r):
        """Compares two <type ... /> elements."""
        if l.tag != 'type' != r.tag:
            return False
        if l.get('name') != r.get('name'):
            return False
        if sorted(l.get('parents').split()) != sorted(r.get('parents').split()):
            return False
        return True

    def tree_sort_key(elem):
        return elem.tag + str(sorted('{}={}'.format(*a) for a in elem.attrib.items()))

    # Skip non-existent original files
    if left is None:
        return True, (None, None)

    # findall('*') selects children, so the root element is skipped on purpose,
    # as its "name" attribute differs depending on how ccg2xml is called.
    iter_left = sorted(left.findall('*'), key=tree_sort_key)
    iter_right = sorted(right.findall('*'), key=tree_sort_key)

    for l, r in zip(iter_left, iter_right):
        if l.tag != r.tag:
            return False, (l, r)
        if l.attrib != r.attrib:
            for comp in [compare_type_elements]:
                if comp(l, r):
                    break
            else:  # If none of the special comparisons breaks, return False
                return False, (l, r)

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

    @classmethod
    def tearDownClass(cls):
        """Removes the temp data after the tests."""
        rmtree(TEMP_DATA)
