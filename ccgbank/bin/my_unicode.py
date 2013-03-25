"""
Useful functions for dealing with Unicode messiness that arises from dealing with messy
input (e.g., gibberish from the Multiple Translation Chinese corpus).
"""
import re, doctest
eval(r'u"[\u0080-\uffff]+"')
RE_XML_ILLEGAL = u'([\u0000-\u0008\u000b-\u000c\u000e-\u001f\ufffe-\uffff])' + \
                 u'|' + \
                 u'([%s-%s][^%s-%s])|([^%s-%s][%s-%s])|([%s-%s]$)|(^[%s-%s])' % \
                 (unichr(0xd800),unichr(0xdbff),unichr(0xdc00),unichr(0xdfff),
                  unichr(0xd800),unichr(0xdbff),unichr(0xdc00),unichr(0xdfff),
                  unichr(0xd800),unichr(0xdbff),unichr(0xdc00),unichr(0xdfff))
invalid_char_re = re.compile("[^\u0009\u000a\u000d\u0020-\uD7FF\uE000-\uFFFD]")

def removeInvalidChars(text):
    """
    Text is a unicode string. All characters that are not valid XML characters are removed.
    """
    return re.sub(RE_XML_ILLEGAL, "?", text)
    
if __name__=="__main__":
    doctest.testmod()
