#!/usr/bin/env python3

import argparse
import sys
import xml.etree.ElementTree as ET
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
        return 'features {\n}'

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
