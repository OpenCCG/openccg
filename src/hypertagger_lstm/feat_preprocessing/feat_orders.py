class FeatOrders(object):
    @classmethod
    def featOrder(cls, feat_order_name):
        if feat_order_name == 'allrel':
            return ["PN","hypertag","XC","ZD","ZM","ZN","ZP","ZT","FO","NA","PR#5","AT#5"]
        elif feat_order_name == 'allrel_pname':
            return ["PN","hypertag","XC","ZD","ZM","ZN","ZP","ZT","FO","NA","PR#5","AT#5","RN#5"]
        elif feat_order_name == 'allrel_cname':
            return ["PN","hypertag","XC","ZD","ZM","ZN","ZP","ZT","FO","NA","PR#5","AT#5","AN#5"]
        else:
            return ["PN","hypertag","XC","ZD","ZM","ZN","ZP","ZT","FO","NA",
            "CN","CT","A0N","A1N","A2N","A3N","A4N","A5N","A0P","A1P","A2P","A3P","A4P","A5P",
            "X0D","X1D","X2D","X3D","X4D","X5D","MP","XM","RN","PP"]
    
    @classmethod
    def strFeats(cls):
        return ["PN","AN0","AN1","AN2","AN3","AN4","A0N","A1N","A2N","A3N","A4N","A5N","RN0","RN1","RN2","RN3","RN4"]
    
    @classmethod
    def presufFeats(cls):
        return ["prefix_1","prefix_2","prefix_3","suffix_1","suffix_2","suffix_3"]