class IndexTranslater(object):
    ''' Returns string values associated with given indices '''

    def __init__(self, tag_space, feat_spaces, feat_order):
        self.tag_space = tag_space
        self.feat_spaces = feat_spaces
        self.feat_order = feat_order
        
    def tagAtIndex(self, index):
        return self.tag_space[index]
    
    def wordAtIndex(self, index):
        return self.feat_spaces['PN'][index]
    
    def featValAtIndex(self, feat_num, index):
        return self.feat_spaces[self.feat_order[feat_num]][index]
