import unittest

import xml2ccg


class TestXML2CCG(unittest.TestCase):

    def test_xml2ccg_1(self):
        self.assertEqual(xml2ccg.m1(), 1)

    def test_xml2ccg_2(self):
        self.assertEqual(xml2ccg.m2(), 1)
