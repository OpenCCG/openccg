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

    @property
    def ccg_features(self):
        """Generates a feature { ... } string for the ccg file."""
        if self.types is None:
            return 'feature {\n}'

        # Get all types / features from types.xml
        features = OrderedDict()
        for type_ in self.types.iter('type'):
            feature = Feature(type_)
            features[feature.name] = feature

        # Traverse again to add all children to their parents (second pass to
        # ensure all parents exist)
        for feature in features.values():
            for parent in feature.xml.get('parents', '').split():
                features[parent].children.append(feature)

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

        return 'feature {{\n  {}\n}}'.format(feature_string) + rel_sort_str

    @property
    def ccg_words(self):
        return ''

    @property
    def ccg_rules(self):
        return ''

    @property
    def ccg_lexicon(self):
        return ''

    @property
    def ccg_testbed(self):
        if self.testbed is None:
            return 'testbed {\n}'

        fmt = '  {string}: {numOfParses};'

        lines = []
        for item in self.testbed.iter('item'):
            line = fmt.format(string=item.get('string'),
                              numOfParses=item.get('numOfParses'))
            lines.append(line)

        return 'testbed {\n' + '\n'.join(lines) + '\n}'

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

        self.syntactic_features = []
        self.licensing_features = {}
        self.children = []

        # Denoted with ! in ccg, found in lexicon in distributive-features
        self.distributive = False

    def __str__(self, depth=0):
        fmt = '{dist}{name}{syntactic}{licensing}{colon}{children}{semicolon}'

        dist = ''
        syntactic = ''
        colon = ''
        children = ' '.join(child.__str__(depth+1) for child in self.children)
        semicolon = ''
        licensing = ''

        if self.toplevel:
            dist = '!' if self.distributive else ''
            if self.syntactic_features:
                syntactic = '<{}>'.format(','.join(self.syntactic_features))
            # TODO: macros are similar to syntatic
            if self.children:
                colon = ': '
            semicolon = ';'
        else:
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
                          colon=colon,
                          children=children,
                          semicolon=semicolon,
                          licensing=licensing,
                          )


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
