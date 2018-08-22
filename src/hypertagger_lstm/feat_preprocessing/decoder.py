class Decoder(object):
    '''
    Replaces special character encodings with corresponding characters
    '''
    @classmethod
    def decode(cls, s):
        s = s.replace("&amp;","&")
        s = s.replace("&gt;",">")
        s = s.replace("&lt;","<")
        s = s.replace("&apos;","'")
        s = s.replace("&quot;","\"")
        s = s.replace("&#45;", "-")
        s = s.replace("&#58;", ":")
        return s;
