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

<!--Java helper classes-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>
<xsl:variable name="obj-mw" select="java:opennlp.ccgbank.convert.MWHelper.new()"/><xsl:variable name="obj-info" select="java:opennlp.ccgbank.convert.InfoHelper.new()"/>

<!--Transform which labels various punct cat part of speech-->

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<!--Mark cat of punct like comma/semi-colon which itself acts as a punct-->
<xsl:template match="Leafnode[not(../@cat0='NP\NP[conj]') and  contains(../@cat0,'[conj]') and (@lexeme=',' or @lexeme=';' or @lexeme='--') and not(contains(following-sibling::*/@cat0,'[conj]'))]">

	<Leafnode cat="punct[{@lexeme}]" lexeme="{@lexeme}" lexeme0="{@lexeme0}" pos1="PUNCT_CONJ" cat0="{@cat0}">
		<xsl:apply-templates select="@*[not(name()='pos1')]"/>
  	<atomcat type="punct">
    	<fs id="1">
				<feat attr="lex" val="{@lexeme}"/>
			</fs>
    </atomcat>
  </Leafnode>
</xsl:template>

<!--Mark multi word conjn units-->
<xsl:template match="Treenode[contains(@cat0,'conj') and starts-with(@ptb-tag0,'CONJP')]">

	<xsl:variable name="word" select="descendant::Leafnode"/>

	 <xsl:for-each select="$word">
    <xsl:variable name="void" select="java:concatWords($obj-mw,@lexeme,@pos1,@term_no)"/>
   </xsl:for-each>

	<xsl:variable name="lex" select="java:getInfo($obj-mw,1)"/>
	<xsl:variable name="pos" select="java:getInfo($obj-mw,2)"/>
	<xsl:variable name="term_no" select="java:getInfo($obj-mw,3)"/>

	<Leafnode stem="{$lex}" lexeme="{substring-before($lex,'_')}" pos="CC" pos1="CONJP_HEAD" term_no="{$term_no}">
		<xsl:apply-templates select="@*[not(name()='dtr' or name()='head' or name()='term_no')]"/>
		<xsl:for-each select="$word[position()>1]">
    	<PRT dir="/" type="prt">
				<xsl:apply-templates select="./@*"/>
			</PRT>
   </xsl:for-each>

	</Leafnode>

</xsl:template>	

<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>
