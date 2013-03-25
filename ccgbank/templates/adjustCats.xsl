<!--Copyright (C) 2005-2009 Scott Martin, Rajakrishan Rajkumar and Michael White
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.-->


<xsl:transform
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xalan xalan2 java">

  <xsl:output method="xml" indent="yes" xalan2:indent-amount="2"/>
  <xsl:strip-space elements="*"/>
  
  <!-- Transform which does misc adjustments of categories in parses. -->

  
  <!-- Change eg [with], [of] to 'lex' feat, instead of 'form', with full word as val. -->
  <xsl:template match="atomcat[@type='pp']/fs/feat[@attr='form']">
    <feat attr="lex" val="{@val}"/>
  </xsl:template>
  
  <!-- Change case-marking preps to use ~2 -->
  <xsl:template match="Leafnode[not(@candc) and ../@roles]/complexcat[not(atomcat[2][@type='pp'])]/atomcat[1][@type='pp']/fs[feat[@attr='form']]">
    <fs inheritsFrom="2">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>

  <!-- Change that-comp indices to s[em]_~1/s[dcl]_1, reflecting sem null status. -->
  <xsl:template match="Leafnode[not(@candc)]/complexcat[atomcat[2][@type='s']/fs[feat[@attr='form']/@val='dcl']]
    /atomcat[1][@type='s']/fs[feat[@attr='form']/@val='em']"
  >
    <fs inheritsFrom="1">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>
  
  <xsl:template match="Leafnode[not(@candc)]/complexcat[atomcat[1][@type='s']/fs[feat[@attr='form']/@val='em']]
    /atomcat[2][@type='s']/fs[feat[@attr='form']/@val='dcl']"
  >
    <fs id="1">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>
  
  
  <!-- Change to-inf indices to s[to]_~1\np_2/(s[b]_1\np_2), reflecting sem null status. -->
  <xsl:template match="Leafnode[not(@candc)]/complexcat[complexcat/atomcat[1][@type='s']/fs[feat[@attr='form']/@val='b']]
    /atomcat[1][@type='s']/fs[feat[@attr='form']/@val='to']"
  >
    <fs inheritsFrom="1">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>
  
  <xsl:template match="Leafnode[not(@candc)]/complexcat[atomcat[1][@type='s']/fs[feat[@attr='form']/@val='to']]
    /complexcat/atomcat[1][@type='s']/fs[feat[@attr='form']/@val='b']"
  >
    <fs id="1">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>
  

  
  <!-- change eg n_1/n_1 to n_~1/n_1, to support mod-index -->
  <xsl:template match="Leafnode[not(@candc)]/complexcat[
      (atomcat[1][@type='n'] and *[3][self::atomcat][@type='n'][fs/@id='1']) or 
      (atomcat[1][@type='np'] and *[3][self::atomcat][@type='np'])
    ]/atomcat[1]/fs"
  >
    <fs inheritsFrom="1">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>
  
  
  <!-- change eg n_1/n_2/(n_1/n_2) to n_~1/n_1/(n_~1/n_1), to support mod-index -->
  <!-- tbd: check sem of 'than' eg ((N/N)/(N/N))\(S[adj]\NP) --> 
  <xsl:template match="Leafnode[not(@candc) and 
      count(.//complexcat[
        (atomcat[1][@type='n'][fs/@id='1'] and *[3][self::atomcat][@type='n'][fs/@id='2']) or 
        (atomcat[1][@type='np'][fs/@id='1'] and *[3][self::atomcat][@type='np'][fs/@id='2'])
      ]) = 2
    ]//atomcat/fs[@id='1']"
  >
    <fs inheritsFrom="1">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>
  
  <xsl:template match="Leafnode[not(@candc) and 
      count(.//complexcat[
        (atomcat[1][@type='n'][fs/@id='1'] and *[3][self::atomcat][@type='n'][fs/@id='2']) or 
        (atomcat[1][@type='np'][fs/@id='1'] and *[3][self::atomcat][@type='np'][fs/@id='2'])
      ]) = 2
    ]//atomcat/fs[@id='2']"
  >
    <fs id="1">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>
  
  
  
  <!-- change eg s_1/s_1 to s_~1/s_1, to support mod-index -->
  <xsl:template match="Leafnode[not(@candc)]/complexcat[
      atomcat[1][@type='s'][fs/@id='1'] and *[3][self::atomcat][@type='s'][fs/@id='1']
    ]/atomcat[1]/fs"
  >
    <fs inheritsFrom="1">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>
  

  <!-- change eg s_1/s_2/(s_1/s_2) to s_~1/s_1/(s_~1/s_1), to support mod-index -->
  <xsl:template match="Leafnode[not(@candc) and 
      count(.//complexcat[
        atomcat[1][@type='s'] and *[3][self::atomcat][@type='s']
      ]) = 2
    ]//atomcat/fs[@id='1']"
  >
    <fs inheritsFrom="1">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>
  
  <xsl:template match="Leafnode[not(@candc) and 
      count(.//complexcat[
        atomcat[1][@type='s'] and *[3][self::atomcat][@type='s']
      ]) = 2
    ]//atomcat/fs[@id='2']"
  >
    <fs id="1">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>
  
  
  <!-- change eg s_1\np_2\(s_1\np_2) to s_~1\np_2\(s_1\np_2), to support mod-index -->
  <xsl:template match="Leafnode[not(@candc) and 
      count(.//complexcat[
        atomcat[1][@type='s'][fs/@id='1'] and *[3][self::atomcat][@type='np'][fs/@id='2']
      ]) = 2
    ]/complexcat/atomcat[1]/fs"
  >
    <fs inheritsFrom="1">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>
  

  <!-- change eg s_1\np_2/(s_3\np_2)/(s_1\np_2/(s_3\np_2)) to s_~1\np_2/(s_1\np_2)/(s_~1\np_2/(s_1\np_2)), to support mod-index -->
  <xsl:template match="Leafnode[not(@candc) and 
      count(.//complexcat[
        atomcat[1][@type='s'] and *[3][self::atomcat][@type='np']
      ]) = 4
    ]//atomcat/fs[@id='1']"
  >
    <fs inheritsFrom="1">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>
  
  <xsl:template match="Leafnode[not(@candc) and 
      count(.//complexcat[
        atomcat[1][@type='s'] and *[3][self::atomcat][@type='np']
      ]) = 4
    ]//atomcat/fs[@id='3']"
  >
    <fs id="1">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>
  

  
  <!-- default copy rule -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>
