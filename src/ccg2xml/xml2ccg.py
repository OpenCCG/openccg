#!/usr/bin/env python3

import argparse
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


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

    def __getattribute__(self, attr):
        if attr in XMLGrammar.valid_filenames:
            try:
                return ET.parse(next(self.path.glob('*' + attr + '.xml')))
            except StopIteration:
                return ET.ElementTree()
        return object.__getattribute__(self, attr)


def convert_to_ccg(xml_grammar):
    """Converts an XMLGrammar to a ccg file string.

    Args:
        xml_grammar: An XMLGrammar representing the grammar xml files.

    Returns:
        A string which can be stored as a *.ccg file and converted via
        ccg2xml.
    """
    # TODO(shoeffner): Do the actual conversion :-)
    return ''


def xml2ccg():
    """Entry point of the program."""
    arguments = parse_args()

    result = convert_to_ccg(arguments.folder)

    print(result, file=arguments.output)


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
