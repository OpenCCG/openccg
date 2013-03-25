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

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PhraseExtractor.new()"/>

<xsl:template match="Derivation">
	<Derivation>
		<xsl:apply-templates select="@*|node()"/>
	</Derivation>
	<xsl:variable name="void" select="java:printInfo($obj)"/>
</xsl:template>

<!--Store id-->
<xsl:template match="Treenode[@Header]">
	<xsl:variable name="void" select="java:storeSentId($obj,@Header)"/>
	<Treenode>
		<xsl:apply-templates select="@*|node()"/>
	</Treenode>
</xsl:template>

<!--Store VPs
<xsl:template match="Treenode[(java:stripLex($obj,@cat)) and count(descendant::Leafnode) &gt; 1]">-->

<!--Store NPs-->
<xsl:template match="Treenode[@cat0='N' and descendant::Leafnode[1]/@cat0='N/N' and descendant::Leafnode[2]/@cat0='N/N' and descendant::Leafnode[3]/@cat0='N']">

	<xsl:for-each select="descendant::Leafnode">
		<xsl:variable name="void" select="java:storeWord($obj,@lexeme)"/>
	</xsl:for-each>

	<xsl:variable name="void" select="java:storePhrase($obj,'NP')"/>

	<!--Once a VP is detected, skip all lower level phrases-->
	<Treenode>
		<xsl:apply-templates select="@*|node()"/>
	</Treenode>
</xsl:template>

<!--Default global copy rule-->
<xsl:template match="@*|node()">
   <xsl:copy>
     <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
</xsl:template>

</xsl:transform>











