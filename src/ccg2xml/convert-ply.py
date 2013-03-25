#!/usr/bin/python

import sys
import re
import optparse
import os.path
import time

# Author: Ben Wing <ben@666.com>
# Date: April 2006

#############################################################################
#                                                                           #
#                              convert-ply.py                               #
#                                                                           #
#   Convert a .ply file into a .py file which can be run to generate a      #
#   compiler for a language and use it to parse a specified file.  This     #
#   program is something like a compiler-compiler-compiler -- it uses a     #
#   script to generate a compiler-compiler script, which in turn is used    #
#   to generate a compiler, which in turn processes a program in some       #
#   arbitrary syntax and does whatever it wants with it.  Very meta!!!      #
#                                                                           #
#############################################################################

# The format of a .ply file is that of a .py file with YACC-like directives
# interspersed.  %y on a line by itself switches to YACC mode, and %p
# switches back to Python mode.  In YACC mode, lines should look like this:

# lhs: rhs1 rhs2 ...: python code # first alternative
#    : rhs3 rhs4 ...: more python code # second alternative
#    : rhs5 rhs6 # alternative with default code of $$ = $1
#    : rhs7 rhs8 ...:
#        # If no code follows the colon on the same line, the code consists
#        # of all following indented lines.
#        python code
#        python code
#        ...
#
# This represents a context-free rule where LHS expands to one or more
# rules (e.g. RHS1 RHS2 ... or RHS3 RHS4 ... or RHS5 RHS6 ... etc.).
# Associated with each rule is some code, which will be invoked when the
# parser finds an appropriately matching right-hand side somewhere in
# the stream of tokens being parsed and proceeds to "reduce" the relevant
# tokens into the single left-hand side token LHS.  Associated with each
# token is a value.  For terminal tokens, the value is the string in the
# text that produced this token.  For non-terminal tokens, the value is
# determined by the code associated with the rule that produced this token
# (i.e. one of the rules with this token as its LHS).  The value of a
# non-terminal token is set by assigning to $$; values of RHS tokens are
# referenced using $1, $2, etc., where $1 is the first RHS token, $2
# is the second, etc.  Hence, the default code of $$ = $1 assigns the
# value of the first RHS token to the LHS token.  The code can also
# reference $@, which is a structure encapsulating all RHS values; this
# is mostly useful for getting at $@.lineno, a function referring to the
# starting line number of a particular token (especially $@.lineno(0), the
# starting line number of the set of RHS tokens).

# WARNING!!! Currently, PLY has a serious bug in its handling of empty
# RHS rules; often it reports a syntax error in place of properly reducing
# an empty RHS rule.  If this happens, you must rewrite the appropriate
# rules without the use of an empty RHS rule. (As of yet, I'm not sure
# exactly what the circumstances are that trigger this bug.)
#
# Note that this bug exists as of PLY 1.6, which is what we are currently
# using.  It's quite possible that later versions of PLY (especially the
# new PLY 2.x series) fix the bug.

###########################################################################
#
# Command-line options and usage
#
###########################################################################

usage = """%prog [OPTIONS] FILE ...

Convert from .ply format to a .py file, for lex/yacc.
"""

parser = optparse.OptionParser(usage=usage)
parser.add_option("-o", "--outfile",
                   default=None,
                   help="""Specify the output file.
Default is y.INFILE.py, where INFILE is the source file's name minus any
.ply extension.""",
                   metavar="FILE")

(options, args) = parser.parse_args()

def syntax_error(err, line):
    global errors
    errors += 1
    if errors > maxerr:
        raise SyntaxError("Too many errors (more than %s) when compiling" %
                          maxerr)
    sys.stderr.write("%s in file %s, line %d: %s\n" %
                     (err, current_file, current_lineno, line))
    

wordrange = r'\-a-zA-Z0-9_%'
operrange = r'\+\*\|\?'
wordre = '[%s]+' % wordrange

def make_name_python_safe(name):
    return re.sub('[^A-Za-z0-9_]', '_', name)

# Replace dollar signs in CODE to point to the actual array of RHS values.
# If RENUMBER_AT is given, however, convert $RENUMBER_AT to None, and
# subtract one from all $ references above this value.

def replace_dollar_signs(code, renumber_at=None):
    newcode = ""
    prevright = 0
    for match in re.finditer(r"""('''([^\\\n]|\\(.|\n))*?'''|
                                  \"\"\"([^\\\n]|\\(.|\n))*?\"\"\"|
                                  '([^\\\n]|\\(.|\n))*?'|
                                  \"([^\\\n]|\\(.|\n))*?\"|
                                  [#][^\n]*\n?|
                                  ([^\\'\"#]|\\(.|\n))*)""", code, re.VERBOSE):
        errored = 0
        if prevright != match.start(0):
            syntax_error("Apparent syntax error in code at position %d"
                         % prevright, code)
            errored = 1
            newcode += code[prevright:match.start(0)]
        prevright = match.end(0)
        matchstr = match.group(0)
        if not matchstr:
            if match.start(0) == len(code):
                break
            elif not errored:
                errored = 1
                syntax_error("Apparent syntax error in code at position %d"
                             % match.start(0), code)
        elif matchstr[0] in '\'"#':
            # A comment or literal; don't substitute in it
            pass
        elif renumber_at:
            # Renumber
            def replace_dollar_def(match):
                str = match.group()
                ref = int(str[1:])
                if ref == renumber_at:
                    return '[]'
                elif ref > renumber_at:
                    return '$%d' % (ref - 1)
                else:
                    return str
            matchstr = re.sub(r'\$([0-9]+)', replace_dollar_def, matchstr)
        else:
            # Apply substitutions
            matchstr = re.sub(r'\$@', 'p', matchstr)
            matchstr = re.sub(r'\$\$', 'p[0]', matchstr)
            matchstr = re.sub(r'\$([0-9]+)', r'p[\1]', matchstr)

        newcode += matchstr
    return newcode

# Output a single CFG rule
def output_python_cfg_rule(fil, lhs, rhs, code):
    global unique_no

    # Look for occurrences of foo* or foo+; handle all of them by
    # adding appropriate list rules.
    newrhs = ""
    prevright = 0
    for match in re.finditer(r'(%s)\s*([+*])' % wordre, rhs):
        matchstr = match.group(1)
        newrhs += rhs[prevright:match.start(0)]
        prevright = match.end(0)
        unique_no += 1
        matchlhs = '%s_%s_list_%s' % (lhs, matchstr, unique_no)
        newrhs += matchlhs
        if match.group(2) == '+':
            output_python_cfg_rule(fil, matchlhs, matchstr, "    $$ = [$1]\n")
        else:
            output_python_cfg_rule(fil, matchlhs, " ", "    $$ = []\n")
        output_python_cfg_rule(fil, matchlhs, '%s %s' % (matchlhs, matchstr),
                               "    $$ = $1 + [$2]\n")
    if prevright:
        newrhs += rhs[prevright:]
        rhs = newrhs

    # Look for occurrences of foo?; handle by splitting into two rules
    # (It should be possible to handle by using empty rules, but this
    # is currently broken in PLY and more-or-less randomly doesn't work,
    # with the empty rule not being recognized and a syntax error
    # resulting)
    match = re.search(r'(%s)\s*[?]' % wordre, rhs)
    if match:
        matchtoken = match.group(1) # matched token, w/o following '?'
        leftrhs = rhs[0:match.start(0)] # text before match
        rightrhs = rhs[match.end(0):] # text after match
        # Output the "it's there" alternative
        output_python_cfg_rule(fil, lhs, leftrhs + matchtoken + rightrhs, code)
        # compute token ID, as would be referenced by a $# reference, based
        # on the text before the match.  first delete everything before a
        # '|' (alternatives) then count the number of words.
        tokennum = len(re.sub('.*\|', '', leftrhs).split()) + 1
        code = replace_dollar_signs(code, tokennum)
        # Output the "it's not there" alternative, with the dollar references
        # renumbered.  We should *not* attempt any tail-recursion elimination
        # here, in case there are further foo? occurrences later in the file.
        output_python_cfg_rule(fil, lhs, leftrhs + rightrhs, code)
    else:
        unique_no += 1
        print >> fil, "def p_%s_%d(p):" % (make_name_python_safe(lhs),
                                           unique_no)
        rhs = rhs.strip()
        rhs = re.sub(r'\s*\|\s*', r'\n    | ', rhs)
        rhs = re.sub(r'\n\s*\n', '\n', rhs)
        if rhs.find('\n') >= 0:
            print >> fil, "    '''%s : %s'''" % (lhs, rhs)
        else:
            print >> fil, "    '%s : %s'" % (lhs, rhs)
        code = replace_dollar_signs(code)
        print >> fil, code

def output_default_python_cfg_rule(fil, lhs, rhs):
    output_python_cfg_rule(fil, lhs, rhs, "    $$ = $1\n")

def finish_any_cfg(fil):
    global curlhs, currhs, yacc_python_mode, yacc_python_code
    if currhs:
        # A RHS not yet finished; finish it
        if yacc_python_code:
            output_python_cfg_rule(fil, curlhs, currhs, yacc_python_code)
        else:
            output_default_python_cfg_rule(fil, curlhs, currhs)
    clear_rule_context()

def clear_rule_context():
    global curlhs, currhs, yacc_python_mode, yacc_python_code
    curlhs = None
    currhs = None
    yacc_python_mode = False
    yacc_python_code = None

## Process file(s)

args = args or ['-']
for arg in args:
    global current_file
    current_file = arg
    # Open input and output files
    if arg == '-':
        fil = sys.stdin
    else:
        fil = open(arg)
    if options.outfile:
        outarg = options.outfile
    else:
        if arg == '-':
            outarg = 'y.ccg.py'
        else:
            fname = arg
            if fname.endswith('.ply'):
                fname = fname[0:-4]
            (fdir, ffile) = os.path.split(fname)
            outarg = os.path.join(fdir, 'y.%s.py' % ffile)
    outfil = open(outarg, 'w')

    # Initialize state
    errors = 0
    maxerr = 5
    unique_no = 0
    clear_rule_context()
    mode = 'python'
    contline = None

    print >> outfil, """#!/usr/bin/python

################## NOTE NOTE NOTE ##################
#
# This file (%s) was automatically generated from %s.
# Generated by %s at %s.
#
# DO NOT MODIFY THIS FILE DIRECTLY.  YOUR CHANGES WILL BE LOST.
# Instead, modify the file `%s' that generated this file, and then
# rerun `%s%s %s'.
#
################## NOTE NOTE NOTE ##################
""" % (outarg, current_file, sys.argv[0], time.asctime(), current_file,
       sys.argv[0],
       options.outfile and " -o %s" % options.outfile or "",
       current_file)

    global current_lineno
    current_lineno = 0
    for line in fil:
        current_lineno += 1
        line = line.rstrip("\r\n")
        if contline:
            line = contline + line
            contline = None
        if line == '%y':
            mode = 'yacc'
        elif line == '%p':
            mode = 'python'
            finish_any_cfg(outfil)
        elif line == '%l':
            mode = 'lex'
        else:
            if mode == 'python':
                print >> outfil, line
            else:
                if yacc_python_mode:
                    if re.match(r'\S', line):
                        yacc_python_mode = False
                    else:
                        yacc_python_code += line + '\n'
                        continue
                if re.match(r'\s*#.*$', line):
                    print >> outfil, line
                    continue
                elif line and line[-1] == '\\':
                    contline = line[0:-1]
                    continue
                elif re.match(r'\s*$', line):
                    print >> outfil, line
                    continue
                # Eliminate comments, but conservatively, to avoid any
                # possibility of removing comments inside of quotes (which
                # should occur only in Python code, anyway, in which case
                # it doesn't really matter)
                line = re.sub(r'''^([^\'\"#]*)#.*$''', '\1', line)
                if mode == 'yacc':
                    match = re.match(r'(%s)\s*(:.*)$' % wordre, line)
                    if match:
                        # We are starting a new rule
                        finish_any_cfg(outfil)
                        curlhs = match.group(1)
                        line = ' ' + match.group(2)
                    match = re.match(r'(\s*)(:)?([^:]*)(:.*)?$', line)
                    if not match or not match.group(1):
                        syntax_error("Unrecognized rule beginning", line)
                    if re.match(r'^.*[^%s\s%s].*$' % (operrange, wordrange),
                                match.group(3)):
                        syntax_error("Illegal characters in RHS", line)
                    if not match.group(2):
                        # We are continuing an RHS
                        if currhs == None:
                            syntax_error("Invalid RHS continuation", line)
                        else:
                            currhs += match.expand(r' \3')
                    else:
                        # We are starting an RHS
                        if curlhs == None:
                            syntax_error("Invalid RHS without LHS", line)
                        else:
                            if currhs:
                                output_default_python_cfg_rule(outfil, curlhs,
                                                               currhs)
                            currhs = match.group(3)
                    if match.group(4):
                        # strip colon, spaces
                        code = match.group(4)[1:].strip()
                        if not code:
                            # Start eating the rest of the code until new rule
                            yacc_python_mode = True
                            yacc_python_code = ""
                        else:
                            code = '    ' + code + '\n'
                            output_python_cfg_rule(outfil, curlhs, currhs,
                                                   code)
                            currhs = None
    finish_any_cfg(outfil)
    fil.close()
    outfil.close()
