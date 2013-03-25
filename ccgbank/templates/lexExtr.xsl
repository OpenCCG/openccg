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

  <xsl:output method="xml" indent="yes" xalan2:indent-amount="2" omit-xml-declaration = "yes"/>
  
  <xsl:strip-space elements="*"/>
  
  <!--Transform which reads in a xml parse rep and creates in temp.xml lexical entries for that file and compiles a freq tally-->
  
  <!--Java Program in the grammar extractor package invoked-->
  <xsl:variable name="obj" select="java:opennlp.ccgbank.extract.FreqTally.new()"/>
  
  
  <!--Eliminating the root of the xml parse rep and processing children-->
  <xsl:template match="Derivation">
    <xsl:apply-templates />
  </xsl:template>
  
  <!--Skip Treenodes-->
  <xsl:template match="Treenode">
    <!--Store ccgbank id for recording example sentences-->	
    <xsl:if test="@Header">
      <xsl:variable name="id" select="java:storeId($obj,@Header)"/>
      <!-- nb: need to pretend to use result to avoid having above call "optimized" away--> 
      <xsl:variable name="boo" select="$id"/>
    </xsl:if>
    <xsl:apply-templates/>
  </xsl:template>
  
  
  <!--Leafnodes Processed-->
  <xsl:template match="Leafnode">

    <!--Load cat into freq tally java class and see whether it is first occurence-->
		<!--Change the case of the lexeme if it is not of pos NNP/NNPS
		<xsl:variable name="lexCase" select="java:changeCase($obj,@lexeme,@pos)"/>-->
    <xsl:variable name="firstCatPos" select="java:loadTally($obj,@lexeme,@cat,@pos)"/>

    <!--First occcurence of a catspec-->
    <xsl:if test="$firstCatPos">
      <!--Lexical entry for openccg lexicon generated-->              
      <family name="{@cat}" pos="{@pos}">
        <xsl:copy-of select="@pos1"/>
        <xsl:copy-of select="@argRoles"/>
        <entry name="Primary">
          <xsl:apply-templates mode="leaf"/>
        </entry>
      </family> 
    </xsl:if>             
    
    <!-- Add lex entry for new lex combos --> 
    <!-- nb: for now, need to ignore rel for particles -->
    <xsl:variable name="rel">
      <xsl:choose>
        <xsl:when test="starts-with(@pos,'VB')"><xsl:value-of select="@rel"/></xsl:when>
        <xsl:otherwise/>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="firstLexCombo" select="java:firstLexCombo($obj,@lexeme,@stem,$rel,@cat,@pos,@class)"/>
    <xsl:if test="$firstLexCombo">
      <entry word="{@lexeme}" pos="{@pos}" family="{@cat}">
        <!-- add stems when present -->
        <xsl:copy-of select="@stem"/>
        <!-- add rel too -->
        <xsl:if test="$rel != ''">
          <xsl:attribute name="rel"><xsl:value-of select="$rel"/></xsl:attribute>
        </xsl:if>

				<!--Add BBN semantic class if present-->
				<xsl:if test="@class">
          <xsl:attribute name="class"><xsl:value-of select="@class"/></xsl:attribute>
        </xsl:if>
      </entry>
    </xsl:if>
  
  </xsl:template>
  
  <!--Copy Rule for leaf nodes-->
  <xsl:template match="@*|node()" mode="leaf">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="leaf"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>