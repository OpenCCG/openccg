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
  
  
  <!--Transform which takes in as input the temp.xml file and outputs a morph.xml file -->
  
  <!--Java Program in the grammar extractor package invoked-->
  <xsl:variable name="obj" select="java:opennlp.ccgbank.extract.MorphExtrHelper.new()"/>
  
  <!--Insert appropriate openccg morph.xml root element--> 
  <xsl:template match="ccg-lexicon">
    <morph xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../morph.xsd" name="protogrammar">
      <xsl:apply-templates/>
      <!-- 
			<entry word="*dummy*" pos="Dummy"/>
      -->
    </morph>
  </xsl:template>

  <!--Exclude all family elements from the temp file-->
  <xsl:template match="family"/>
  

  <!--Pest control for determiners--> 
  <!-- 
  <xsl:template match="entry[@word='the' and not(@family='np_1/n_1')]"/>
  -->
  
  <!-- no longer eliminating punct entries ... -->
  <!-- 
  <xsl:template match="entry[@word=',' or @word=';' or @pos=':' or @pos='LRB' or @pos='RRB']"/>
  -->

  
  <!-- Frequency and novelty check -->
  <xsl:template match="entry">
    <xsl:if test="java:checkFreqAndNoveltyStatus($obj,@word,@stem,@family,@pos,@class)">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:if>
  </xsl:template>

  
  <!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>


