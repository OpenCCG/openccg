class NERWord:
    """
    A simple wrapper for NER-labelled words.
    """
    def __init__(self, wd, label=None, delim="/"):
        self.wd = wd
        self.label = label
        self.delim = delim

    @staticmethod
    def parseLineOfWords(ln):
        """
        Parses a line of words labelled with NE labels (e.g., "<PERSON>John Smith</PERSON> entered the <LOCATION>United States</LOCATION>").
        """
        res = []
        for w in ln:
            if "</" in w and w.endswith(">"):
                parts = w.partition("</")
                (wd,lb) = (parts[0], "</"+parts[2])

                if "<" in wd and ">" in wd:
                    parts = wd.partition(">")
                    wd = parts[2]
                    lb = lb[2:-1]
            elif w.startswith("<") and ">" in w:
                parts = w.partition(">")
                (wd,lb) = (parts[2], parts[0]+">")
            else:
                (wd,lb) = (w,None)
            res.append((wd,lb))

        # now distribute the labels to words between within the <TAG>...</TAG> labels.
        final_res = []
        i = len(res) - 1
        while i >= 0:
            (wd,lb) = res[i]
            if lb is None:
                final_res.append(NERWord(wd,lb))
            elif not "</" in lb:
                final_res.append(NERWord(wd,lb))
            else:
                final_res.append(NERWord(wd,lb[2:-1]))
                i -= 1
                (wd_prm,lb_prm) = res[i]
                # apply this label until we 
                while lb_prm is None:
                    final_res.append(NERWord(wd_prm, lb[2:-1]))
                    i -= 1
                    (wd_prm,lb_prm) = res[i]
                final_res.append(NERWord(wd_prm, lb[2:-1]))
            i -= 1
        final_res.reverse()
        return final_res

    def getLabel(self):
        return self.label

    def getWord(self):
        return self.wd
    
    def __repr__(self): return self.__str__()
    
    def __str__(self): return self.wd + self.delim + (self.getLabel() if not self.getLabel() is None else "")
    
        
