#
# reverses the conversion from spaces to newlines, and newlines to special <eol> chars,
# from stdin to stdout
#
import sys, re;
[sys.stdout.write(re.sub('<eol>','\n',re.sub('\n',' ',line))) for line in sys.stdin]
