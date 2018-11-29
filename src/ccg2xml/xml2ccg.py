#!/usr/bin/env python3

import argparse
import re
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
            if feature.name in features:
                raise KeyError('Feature {} specified twice in types.'.format(feature.name))
            features[feature.name] = feature

        special_macros = []
        # Get all feature structure ids from morph.xml
        for macro in self.morph.iter('macro'):
            feature_val = None
            # Simple numeric id
            if macro.find('fs') is not None:
                fs = macro.find('fs')
                feature_id = fs.get('id')
                feature = fs.get('attr')
                if fs.find('feat') is not None:
                    feature = fs.find('feat').get('attr')
                    feature_val = fs.find('feat').get('val')
                if fs.get('val') is not None:
                    feature_val = fs.get('val')
                    special_macro = SpecialMacro(macro.get('name'),
                                                 feature_id,
                                                 feature,
                                                 feature_val)
                    special_macros.append(special_macro)
                    continue
            # <diamond mode=""> declaration
            elif macro.find('lf') is not None:
                feature = macro.get('name')[1:]
                lf = macro.find('lf')
                satop = lf.find('satop')
                feature_id = maybe_quote(satop.get('nomvar'))
                if satop.find('diamond') is not None:
                    diamond = satop.find('diamond')
                    mode = diamond.get('mode')
                else:
                    mode = satop.find('prop').get('name')
                if feature_id != mode:
                    feature_id = '{}:{}'.format(feature_id, mode)
            else:
                # TODO(shoeffner): Is there actually a different case?
                continue
            try:
                features[feature].feature_struct_ids.append(feature_id)
            except KeyError:  # New feature from a macro definition
                features[feature] = Feature(xml_type=macro, val=feature_val)
                features[feature].name = feature
                features[feature].feature_struct_ids.append(feature_id)

        # Traverse again to add all children to their parents (second pass to
        # ensure all parents exist)
        for feature in features.values():
            parents = []
            if feature.xml is not None:
                parents = feature.xml.get('parents', '').split()
                prop = feature.xml.find('./lf/satop/diamond/prop')
                if prop is not None:
                    name = prop.get('name')
                    if name in features:
                        sf = SimpleFeature(name)
                        feature.children.append(sf)
                        feature.toplevel = True
            if parents:
                if len(parents) > 1:
                    feature.additional_parents += parents[1:]
                features[parents[0]].children.append(feature)

        for feature in special_macros:
            if feature.attr in features:
                feature.additional_parents += feature.attr
            features[feature.attr + feature.name] = feature

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

    def _create_simple_rules(self, tag, rulename):
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
        directions = {
            'forward': '+',
            'backward': '-'
        }

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

    def _create_typeraising_rules(self):
        """Parses all typeraising rules.

        This works similar to the create_simple_rules function, but
        typeraise rules don't have the harmonic argument but useDollar,
        as well as additional subelements.
        """
        directions = {
            'forward': '+',
            'backward': '-'
        }

        rules = []
        for item in self.rules.iter('typeraising'):
            rule = 'typeraise {}'.format(directions[item.get('dir')])
            if item.get('useDollar', 'false') == 'true':
                rule += ' $'

            # complex rule: argument => result
            if item.find('arg') is not None and item.find('result') is not None:
                argument = CategoryParser().parse_cat(item.find('arg').find('*'))
                result = CategoryParser().parse_cat(item.find('result').find('*'))
                rule += ': {} => {}'.format(argument, result)

            rules.append(rule + ';')
        return rules

    def _create_typechanging_rules(self):
        """Parses all typechanging rules.

        This works similar to the create_typeraising_rules function,
        typechanging rules have a slightly different format.
        """
        rules = []
        for item in self.rules.iter('typechanging'):
            rule = 'typechange'

            # complex rule: argument => result
            if item.find('arg') is not None and item.find('result') is not None:
                argument = CategoryParser().parse_cat(item.find('arg').find('*'))
                result = CategoryParser().parse_cat(item.find('result').find('*'))
                rule += ': {} => {}'.format(argument, result)

            rules.append(rule + ';')
        return rules

    @property
    def ccg_rules(self):
        """Create the rule section for a ccg file.

        Due to simplicity, this algorithm just assumes a reset of all defaults
        and marks each rule explicitly.
        """
        if self.rules is None:
            return ''

        rules = ['no;']  # Remove all defaults
        rules += self._create_simple_rules('application', 'app')
        rules += self._create_simple_rules('composition', 'comp')
        rules += self._create_simple_rules('substitution', 'sub')
        rules += self._create_typeraising_rules()
        rules += self._create_typechanging_rules()

        rule_section = 'rule {{\n  {}\n}}'.format('\n  '.join(r for r in rules if r))
        # @shoeffner: Not sure if this is needed, but just in case put forward (+) first
        rule_section = rule_section.replace('-+', '+-')
        return rule_section

    @property
    def ccg_lexicon(self):
        if self.lexicon is None:
            return ''

        families = map(Family, self.lexicon.iter('family'))

        lexicon_section = '\n\n'.join(map(str, families))
        return lexicon_section

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
    def __init__(self, xml_type=None, val=None):
        if xml_type is None:
            self.xml = None
            self.name = None
            self.toplevel = False
        else:
            self.xml = xml_type
            # found in types.xml
            self.name = xml_type.get('name').replace('@', '')
            # toplevel if it has no parents
            self.toplevel = xml_type.get('parents') is None

        self.val = val
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

        if self.val is not None:  # Special feature
            if depth == 0:
                return '{}<{}>: {}:{};'.format(maybe_quote(self.name),
                                               ','.join(self.feature_struct_ids),
                                               maybe_quote(self.xml.get('attr')),
                                               maybe_quote(self.val))

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
                          name=maybe_quote(self.name),
                          syntactic=syntactic,
                          parents=parents,
                          colon=colon,
                          children=children,
                          semicolon=semicolon,
                          licensing=licensing)


class SimpleFeature(Feature):
    def __init__(self, name):
        super().__init__()
        self.name = name

    def __str__(self, depth=0):
        if depth != 0 and self.additional_parents:
            return '{}[{}]'.format(maybe_quote(self.name),
                                   ' '.join(map(maybe_quote, self.additional_parents)))
        return maybe_quote(self.name)


class SpecialMacro(Feature):
    """Allows to do create explicit feature instructions.

        <macro name="@acc0">
          <fs id="0" attr="case" val="p-case"/>
        </macro>

    is converted to

        case<0>: acc0:p-case;
    """

    def __init__(self, name, feature_id, feature, val):
        super().__init__()
        self.toplevel = True
        self.name = name.replace('@', '')
        self.id = feature_id
        self.attr = feature
        self.val = val

    def __str__(self, depth=0):
        if depth == 0:
            fs = ['{}<{}>: {}:{};'.format(maybe_quote(self.attr),
                                          maybe_quote(self.id),
                                          maybe_quote(self.name),
                                          maybe_quote(self.val))]
            # A toplevel special feature consideres its children as siblings
            for f in self.children:
                fs.append('{}<{}>: {}:{};'.format(maybe_quote(f.attr),
                                                  maybe_quote(f.id),
                                                  maybe_quote(f.name),
                                                  maybe_quote(f.val)))
            return '\n  '.join(fs)

        else:
            return '{}:{}'.format(maybe_quote(self.name),
                                  maybe_quote(self.val))


def maybe_quote(word):
    if re.match('.*[^a-zA-Z0-9-+%_*]+.*', word):
        if "'" in word and '"' in word:
            raise ValueError('Can not handle single and double quotes in a single word:  {}'.format(word))
        elif "'" in word:
            return '"{}"'.format(word)
        else:
            return "'{}'".format(word)
    return word


class Word:
    def __init__(self, xml_entry):
        self.xml = xml_entry
        self.form = xml_entry.get('word')  # inflected form
        self.stem = xml_entry.get('stem', self.form)
        self.form = maybe_quote(self.form)
        self.stem = maybe_quote(self.stem)
        self.family = xml_entry.get('pos', '')
        self.attributes = xml_entry.get('class', '').split()
        for key in ['pred', 'excluded', 'coart']:
            val = xml_entry.get(key)
            if val is not None:
                self.attributes.append('{}={}'.format(key, val))
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
                          family=maybe_quote(self.family),
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
            form = self.form
            if self.features:
                form += ': '
        if self.features:
            features = ' '.join(map(maybe_quote, self.features))

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


class FamilyEntry:
    def __init__(self, xml_entry):
        self.name = xml_entry.get('name')
        self.category = CategoryParser().parse_cat(xml_entry.find('*'))

    def __str__(self):
        fmt = 'entry{name}: {catstring};'

        name = ' ' + maybe_quote(self.name) if not self.name.startswith('Entry-') else ''
        catstring = self.category

        return fmt.format(name=name, catstring=catstring).replace('[*DEFAULT*]', '*')


class CategoryParser:
    def parse_cat(self, cat):
        if cat.tag == 'complexcat':
            return self.parse_complexcat(cat)
        elif cat.tag == 'atomcat':
            return self.parse_atomcat(cat)
        elif cat.tag == 'setarg':
            return self.parse_setarg(cat)
        elif cat.tag == 'slash':
            return self.parse_slash(cat)
        elif cat.tag == 'dollar':
            return self.parse_dollar(cat)
        elif cat.tag == 'lf':
            return ': ' + self.parse_lf(cat)
        else:
            raise ValueError('Unknown tag {}'.format(str(cat)))

    def parse_complexcat(self, complexcat):
        return ''.join(map(self.parse_cat, complexcat))

    def parse_atomcat(self, atomcat):
        fmt = '{type}{fs}'
        type_ = maybe_quote(atomcat.get('type', ''))
        fs = []
        lf = []
        for elem in atomcat:
            if elem.tag == 'fs':
                fs.append(self.parse_fs(elem))
            elif elem.tag == 'lf':
                fmt = '{type}{fs}: {lf}'
                lf.append(self.parse_lf(elem))
        if lf:
            return fmt.format(type=type_, fs=''.join(fs), lf=''.join(lf))
        return fmt.format(type=type_, fs=''.join(fs))

    def parse_setarg(self, setarg):
        results = ''.join(map(self.parse_cat, setarg))
        return '{{{}}}'.format(results)

    def parse_slash(self, slash):
        mode = slash.get('mode', '')
        dir = slash.get('dir', '')
        if dir == '' and mode == '':
            return ''
        # If only a mode is given, assume default dir for that mode
        if dir == '':
            if mode == '.':
                dir = '|'
            elif mode == '>':
                dir = '/'
            elif mode == '<':
                dir = '\\'
        if (dir == '/' and mode == '>') \
                or (dir == '\\' and mode == '<') \
                or (dir == '|' and mode == '.'):
            mode = ''
        return ' {}{} '.format(dir, mode)

    def parse_dollar(self, dollar):
        return '${}'.format(dollar.get('name', ''))

    def parse_diamond(self, diamond):
        if len(diamond) == 1:
            fmt = '<{mode}>{name}'
            mode = maybe_quote(diamond.get('mode', ''))
            name = diamond.find('*').get('name', '')
            return fmt.format(mode=mode, name=name)
        fmt = '<{mode}>({props})'
        mode = diamond.get('mode', '')
        dstrings = []
        for elem in diamond:
            if elem.tag == 'diamond':
                dstrings.append(self.parse_diamond(elem))
            elif elem.tag in ['nomvar', 'prop']:
                dstrings.append(elem.get('name', ''))
            else:
                raise ValueError("Unknown tag inside 'diamond': {}".format(elem.tag))
            dstrings.append('^')
        if dstrings[-1] == '^':
            del dstrings[-1]
        return fmt.format(mode=mode, props=' '.join(dstrings))

    def parse_lf(self, lf):
        satop = lf.find('satop')
        cat = satop.get('nomvar', '')

        catprops = []
        for elem in satop:
            if elem.tag == 'prop':
                catprops.append(elem.get('name', ''))
            elif elem.tag == 'diamond':
                diamond = self.parse_diamond(elem)
                if diamond:
                    catprops.append(diamond)
        if len(catprops) > 0:
            cat += '({})'.format(' '.join(catprops))

        return cat

    def parse_fs(self, fs):
        result = ''

        id_ = fs.get('id')
        if id_ is not None:
            result += '<{id}>'.format(id=id_)
        else:
            inherited = fs.get('inheritsFrom')
            if inherited is not None:
                result += '<~{}>'.format(inherited)

        features = []
        for feat in fs:
            nametag = feat.find('lf') or feat.find('featvar')
            if nametag is not None:
                nametag = nametag.find('nomvar') or nametag
                name = nametag.get('name')
            else:
                name = feat.get('attr')
            val = feat.get('val')
            if val is not None:
                name += '={}'.format(maybe_quote(val))
            if name is not None:
                features.append(name)

        if features:
            result += '[{}]'.format(' '.join(features))

        return result


class Family:
    def __init__(self, xml_family):
        self.xml = xml_family
        self.name = xml_family.get('name')
        self.pos = xml_family.get('pos')

        self.entries = [FamilyEntry(entry) for entry in xml_family.iter('entry')]
        self.members = []

        # store all additional attributes, which are not special in some sense
        captured = ['pos', 'name', 'closed']
        self.attributes = {k: v for k, v in xml_family.attrib.items() if k not in captured}

    def __str__(self):
        fmt = ('family {name}{attr} {{\n'
               '  {entries}\n'
               '  {members}\n'
               '}}')

        attributes = [] if self.name == self.pos else [maybe_quote(self.pos)]
        attributes += ['{}="{}"'.format(maybe_quote(k), maybe_quote(v)) for k, v in self.attributes.items()]
        attr = ('(' + ', '.join(attributes) + ')') if attributes else ''

        entries = '\n  '.join(map(str, self.entries))
        members = '\n  '.join(map(str, self.members))

        return fmt.format(name=maybe_quote(self.name),
                          attr=attr,
                          entries=entries,
                          members=members)


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
