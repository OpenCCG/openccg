#
# converts spaces to newlines, and newlines to special <eol> chars,
# from stdin to stdout
#
import sys, re;
[sys.stdout.write(re.sub(' ','\n',re.sub('\n','<eol>',line))) for line in sys.stdin]
