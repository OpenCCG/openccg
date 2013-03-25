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
  
  
  <!-- Adds semantic categories to the logical forms. -->
  <!-- Also adds features for subject-verb agreement. -->
  
  <!-- 
  <xsl:variable name="obj" select="java:opennlp.ccgbank.extract.InsertLFHelper.new()"/>
  -->
  
  <!-- add features for verbal morphology  -->
  <xsl:template match="
    family[starts-with(@pos,'VB')]/entry/*/lf/satop/prop |
    family[starts-with(@pos,'VB')]/entry/*/lf/satop/diamond[nomvar[@name='M']]/prop
  ">
    <xsl:variable name="pos" select="ancestor::family/@pos"/>
    <xsl:variable name="cat" select="ancestor::family/@name"/>
    <xsl:copy-of select="."/>
    <xsl:choose>
      <xsl:when test="$pos='VBD'"> 
        <diamond mode="tense"> <prop name="past"/> </diamond> 
      </xsl:when>
      <xsl:when test="$pos='VBZ' or $pos='VBP'"> 
        <diamond mode="tense"> <prop name="pres"/> </diamond> 
      </xsl:when>
      <xsl:when test="$pos='VBN' and starts-with($cat,'s[pss]')"> 
        <diamond mode="partic"> <prop name="pass"/> </diamond> 
      </xsl:when>
      <xsl:when test="$pos='VBN' and starts-with($cat,'s[pt]')"> 
        <diamond mode="partic"> <prop name="past"/> </diamond> 
      </xsl:when>
      <xsl:when test="$pos='VBG'"> 
        <diamond mode="partic"> <prop name="pres"/> </diamond> 
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
  
  <!-- add number (sg or pl) for nouns -->
  <xsl:template match="
    family[starts-with(@pos,'NN')]/entry/*/lf/satop/prop |
    family[starts-with(@pos,'NN')]/entry/*/lf/satop/diamond[nomvar[@name='M']]/prop
  ">
    <xsl:variable name="pos" select="ancestor::family/@pos"/>
    <xsl:copy-of select="."/>
    <xsl:choose>
      <xsl:when test="$pos='NN' or $pos='NNP'"> 
        <diamond mode="num"> <prop name="sg"/> </diamond> 
      </xsl:when>
      <xsl:when test="$pos='NNS' or $pos='NNPS'"> 
        <diamond mode="num"> <prop name="pl"/> </diamond> 
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  
  <!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:transform>
