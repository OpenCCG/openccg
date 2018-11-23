#!/usr/bin/env python3

import argparse
import sys
import xml.etree.ElementTree as ET
from collections import OrderedDict
from pathlib import Path
from datetime import datetime as dt


GRAMMAR_TEMPLATE = """\
{grammar_name:#^57}
#
# This grammar was automatically generated from OpenCCG
# xml files using the xml2ccg tool.
#
# Conversion date: {conversion_date}
#
# For a tutorial on using this file, please refer to
# http://www.utcompling.com/wiki/openccg/visccg-tutorial
#

###################### Features #########################

{features}

######################## Words ##########################

{words}

######################## Rules ##########################

{rules}

################# Lexicon/Categories ####################

{lexicon}

####################### Testbed #########################

{testbed}
"""


class XMLGrammar:
    """This class wraps a single directory containing all xml files for an
    OpenCCG grammar into a flat API.

    Each file (grammar.xml, morph.xml, ...), as defined in
    XMLGrammar.valid_filenames, can be accessed as an XML ElementTree as a
    simple attribute:

        xml_grammar = XMLGrammar(path_to_grammar)
        xml_grammar.morph  # The parsed (prefix-)morph.xml of the grammar
        xml_grammar.rules  # The parsed (prefix-)rules.xml of the grammar
    """
    valid_filenames = ['grammar', 'morph', 'lexicon', 'rules', 'types', 'testbed']

    def __init__(self, path):
        self.path = Path(path)
        self.cache = {}

    def __getattribute__(self, attr):
        if attr in XMLGrammar.valid_filenames:
            try:
                if attr not in self.cache:
                    self.cache[attr] = ET.parse(next(self.path.glob('*' + attr + '.xml'))).getroot()
            except (StopIteration, ET.ParseError):
                self.cache[attr] = None
            return self.cache[attr]
        return object.__getattribute__(self, attr)

    @property  # noqa: MC0001 ("too complex")
    def ccg_features(self):
        """Generates a feature { ... } string for the ccg file."""
        if self.types is None:
            return 'feature {\n}'

        # Get all types / features from types.xml
        features = OrderedDict()
        for type_ in self.types.iter('type'):
            feature = Feature(type_)
            features[feature.name] = feature

        # Get all feature structure ids from morph.xml
        for macro in self.morph.iter('macro'):
            # Simple numeric id
            if macro.find('fs') is not None:
                fs = macro.find('fs')
                feature_id = fs.get('id')
                feature = fs.find('feat').get('attr')
            # <diamond mode=""> declaration
            elif macro.find('lf') is not None:
                feature = macro.get('name')[1:]
                lf = macro.find('lf')
                feature_id = lf.find('satop').get('nomvar')
                mode = lf.find('satop').find('diamond').get('mode')
                if feature_id != mode:
                    feature_id = '{}:{}'.format(feature_id, mode)
            else:
                # TODO(shoeffner): Is there actually a different case?
                continue
            features[feature].feature_struct_ids.append(feature_id)

        # Traverse again to add all children to their parents (second pass to
        # ensure all parents exist)
        for feature in features.values():
            parents = feature.xml.get('parents', '').split()
            if parents:
                if len(parents) > 1:
                    feature.additional_parents = parents[1:]
                features[parents[0]].children.append(feature)

        # Determine which features are distributive
        dist_features = self.lexicon.find('distributive-features')
        if dist_features is not None:
            for feature_name in dist_features.get('attrs', '').split():
                features[feature_name].distributive = True

        # Determine licensing features
        lic_features = self.lexicon.find('licensing-features')
        if lic_features is not None:
            for feat in lic_features.iter('feat'):
                feature_name = feat.get('val', feat.get('attr'))
                features[feature_name].licensing_features.update(feat.attrib)

        # Print all toplevel features (all others are thus printed implicitly)
        feature_string = '\n\n  '.join(str(f) for f in features.values() if f.toplevel)

        # Relation sorting can also be specified
        rel_sort_str = ''
        rel_sort = self.lexicon.find('relation-sorting')
        if rel_sort is not None:
            rel_sort_str = rel_sort.get('order')
            rel_sort_str = '\n\nrelation-sorting: {};'.format(rel_sort_str)

        feature_section = 'feature {{\n  {}\n}}'.format(feature_string) + rel_sort_str
        return feature_section

    @property
    def ccg_words(self):
        if self.morph is None:
            return ''

        # Find all words
        words = map(Word, self.morph.iter('entry'))

        # Note that at this point '\n'.join(map(str,  words)) should
        # already be a valid ccg file. However, it is better to compress this
        # format a little bit, thus the words can be merged into groups. This
        # is still less elegant than macros, but to infer plural-s or
        # 3rd-person singular-s macros is rather difficult, so we leave it as
        # this for now.

        # Merge words with the same header into groups
        word_groups = {}
        for word in words:
            header = word.header()
            if header not in word_groups:
                word_groups[header] = []
            word_groups[header].append(word)

        # Format groups or single words
        word_strings = []
        for header, group in word_groups.items():
            if len(group) == 1:
                word_strings.append(str(group[0]))
            else:
                bodies = '\n  '.join(w.body(True) for w in group)
                word_string = '{} {{\n  {}\n}}'
                word_strings.append(word_string.format(header, bodies))

        words_section = '\n'.join(word_strings)
        return words_section

    @property
    def ccg_rules(self):
        """Create the rule section for a ccg file.

        Due to simplicity, this algorithm just assumes a reset of all defaults
        and marks each rule explicitly.
        """
        if self.rules is None:
            return ''
        # TODO(shoeffner): The rule parsing is likely not done yet, but only
        # parses the tiny-rules.xml properly. Especially "pro-drop" (i.e.
        # typechange rules) is not implemented yet.

        directions = {
            'forward': '+',
            'backward': '-'
        }

        def create_simple_rules(tag, rulename):
            """Parses simple rules for a tag into the rules rulename and
            xrulename.

            If the attribute harmonic is true, the normal rule is modified
            according to the dir attribute (forward = +, backward = -),
            if harmonic is false, the cross-rulename (xrulename) is modified.

            The results are e.g.:

                app +;
                xapp +-;

            for tag applications with the rulename app, in which the
            harmonic = false rules were given for both directions, while for
            the harmonic = true rules, only the forward direction was given.

            Returns:
                A list of rule strings, e.g. ['app +;', 'xapp +-;']
            """
            rule = ''
            xrule = ''
            for item in self.rules.iter(tag):
                if item.get('harmonic', 'true') == 'true':
                    rule += directions[item.get('dir')]
                else:
                    xrule += directions[item.get('dir')]
            if rule:
                rule = '{} {};'.format(rulename, rule)
            if xrule:
                xrule = 'x{} {};'.format(rulename, xrule)
            return [rule, xrule]

        def create_typeraise_rules():
            """Parses all typeraise rules.

            This works similar to the create_simple_rules function, but
            typeraise rules don't have the harmonic argument but useDollar,
            as well as additional subelements.
            """
            rules = []
            for item in self.rules.iter('typeraising'):
                rule = 'typeraise {}'.format(directions[item.get('dir')])
                if item.get('useDollar', 'false') == 'true':
                    rule += ' $'

                # complex rule: argument => result
                if item.text is not None:
                    # TODO(shoeffner): tiny-rules also has <fs/>, which is not
                    # handled yet. This is in general very simple so far.
                    argument = item.find('arg').find('atomcat').get('type')
                    result = item.find('result').find('atomcat').get('type')
                    rule += ': {} => {}'.format(argument, result)

                rules.append(rule + ';')
            return rules

        rules = ['no;']  # Remove all defaults
        rules += create_simple_rules('application', 'app')
        rules += create_simple_rules('composition', 'comp')
        rules += create_simple_rules('substitution', 'sub')
        rules += create_typeraise_rules()
        # TODO(shoeffner): add typechange rules

        rule_section = 'rule {{\n  {}\n}}'.format('\n  '.join(r for r in rules if r))
        # @shoeffner: Not sure if this is needed, but just in case put forward (+) first
        rule_section = rule_section.replace('-+', '+-')
        return rule_section

    @property
    def ccg_lexicon(self):
        return ''

    @property
    def ccg_testbed(self):
        """Creates the ccg testbed.

        The testbed is as a section with a list of sentences and parse numbers:

        testbed {
          one sentence: 1;
          another sentence: 2;
          wrong sentence: 0;
          ! known failure: 0;
        }
        """
        if self.testbed is None:
            return 'testbed {\n}'

        fmt = '  {known}{string}: {numOfParses};'

        lines = []
        for item in self.testbed.iter('item'):
            known = '! ' if item.get('known') == 'true' else ''
            line = fmt.format(known=known,
                              string=item.get('string'),
                              # Omitting the number is, according to tiny.ccg,
                              # maybe equivalent to 1
                              numOfParses=item.get('numOfParses', '1'))
            lines.append(line)
        testbed_section = 'testbed {\n' + '\n'.join(lines) + '\n}'
        return testbed_section

    @property
    def ccg(self):
        """Converts this XMLGrammar to a ccg file string.

        Returns:
            A string which can be stored as a *.ccg file and converted via
            ccg2xml. In fact, the GRAMMAR_TEMPLATE is populated with whatever is
            needed.
        """
        sections = {
            'features': self.ccg_features,
            'words': self.ccg_words,
            'rules': self.ccg_rules,
            'lexicon': self.ccg_lexicon,
            'testbed': self.ccg_testbed
        }

        # Fill the GRAMMAR_TEMPLATE.
        # Make sure the grammar name is wrapped in spaces and ends in ccg to follow
        # the example grammars included in OpenCCG.
        return GRAMMAR_TEMPLATE.format(grammar_name=' {}.ccg '.format(self.path.stem),
                                       conversion_date=dt.now().isoformat(),
                                       **sections)


class Feature:
    def __init__(self, xml_type):
        self.xml = xml_type
        # found in types.xml
        self.name = xml_type.get('name')
        # toplevel if it has no parents
        self.toplevel = xml_type.get('parents') is None

        self.licensing_features = {}
        self.children = []

        # Denoted with ! in ccg, found in lexicon in distributive-features
        self.distributive = False

        # Explicit parents in case of multiple inheritance
        self.additional_parents = []

        # Feature structure IDs
        self.feature_struct_ids = []

    def _gather_feature_struct_ids(self):
        features = self.feature_struct_ids.copy()
        for child in self.children:
            features += child._gather_feature_struct_ids()
        return list(set(features))

    def __str__(self, depth=0):
        fmt = '{dist}{name}{syntactic}{licensing}{parents}{colon}{children}{semicolon}'

        dist = ''
        syntactic = ''
        parents = ''
        colon = ''
        children = ' '.join(child.__str__(depth+1) for child in self.children)
        semicolon = ''
        licensing = ''

        if self.toplevel:
            dist = '!' if self.distributive else ''

            # gather feature structure ids from all children
            features = self._gather_feature_struct_ids()
            if features:
                syntactic = '<{}>'.format(','.join(features))
            if self.children:
                colon = ': '
            semicolon = ';'
        else:
            if self.additional_parents:
                parents = '[{}]'.format(' '.join(self.additional_parents))
            if children:
                if depth > 0:
                    children = ' {{\n  {spaces}{children}\n{spaces}}}\n{spaces}'\
                        .format(spaces='  ' * depth, children=children)
                else:
                    children = '{{{}}}'.format(children)

        if self.licensing_features:
            licensing = ', '.join('{}={}'.format(k, v)
                                  for k, v in self.licensing_features.items()
                                  if k not in ['attr', 'val'])
            licensing = '(' + licensing + ')'

        return fmt.format(dist=dist,
                          name=self.name,
                          syntactic=syntactic,
                          parents=parents,
                          colon=colon,
                          children=children,
                          semicolon=semicolon,
                          licensing=licensing)


class Word:
    def __init__(self, xml_entry):
        self.xml = xml_entry
        self.form = xml_entry.get('word')  # inflected form
        self.stem = xml_entry.get('stem')
        self.family = xml_entry.get('pos', '')
        # TODO(shoeffner): Handle pred, excluded, coart as additional
        # attributes
        self.attributes = xml_entry.get('class', '').split()
        self.features = [m[1:] for m in xml_entry.get('macros', '').split()]

    def header(self):
        """Returns the header part of a word, that is its steam, family,
        and possible attributes, such as classes. The header is always
        prefixed by 'word'.
        """
        fmt = 'word {stem}{family_colon}{family}{attr}'

        family_colon = ':' if self.family else ''
        attr = ''
        if self.attributes:
            attr = '({})'.format(','.join(self.attributes))

        return fmt.format(stem=self.stem,
                          family_colon=family_colon,
                          family=self.family,
                          attr=attr)

    def body(self, explicit=False):
        """Returns the body part of a word, that is its form and features,
        of which there are macro names.

        Args:
            explicit: If explicit is True, the form will be assumed to be
                      different from its stem, even if they are the same.
                      This is important for word group, where the stem also
                      equals one of the inflected forms.
        """
        fmt = '{form}{features};'

        form = ''
        features = ''
        if self.form != self.stem or explicit:
            form = self.form + ': '
        if self.features:
            features = ' '.join(self.features)

        return fmt.format(form=form,
                          features=features)

    def __str__(self):
        fmt = '{header}{colon}{body}'

        colon = ''
        if self.form != self.stem:
            colon = ' '
            fmt = '{header}{colon}{{\n  {body}\n}}'
        elif self.features:
            colon = ': '

        return fmt.format(header=self.header(),
                          colon=colon,
                          body=self.body())

def xml2ccg():
    """Entry point of the program."""
    arguments = parse_args()
    print(arguments.folder.ccg, file=arguments.output)


def parse_args():
    """Parses the program aguments.

    Returns:
        The parsed arguments.
    """
    parser = argparse.ArgumentParser()
    parser.add_argument('folder', nargs='?', type=XMLGrammar, default='.',
                        help="The path to the directory containing the grammar's xml files.")
    parser.add_argument('-o', '--output', nargs='?', type=argparse.FileType('w'),
                        default=sys.stdout, help="The output file. Defaults to STDOUT.")
    return parser.parse_args()


if __name__ == '__main__':
    xml2ccg()
