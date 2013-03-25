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

<!--Java helper class-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>
<!--Transform which identifies & labels various punct cat part of speech -Part II-->

<xsl:template match="/">
  	<xsl:apply-templates/>
</xsl:template>

<!--Correct existing NP appositives-->
<xsl:template match="Leafnode[@pos1=',' and preceding-sibling::Treenode[not(descendant::Leafnode/@pos1='PUNCT_APPOS') and  @cat0='NP'] and parent::Treenode[@cat0='NP']/following-sibling::Treenode/@cat0='NP\NP']">

	<xsl:choose>
		<xsl:when test="parent::Treenode/following-sibling::Treenode/@dtr='1'">
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_APPOS_VRB')"/>	
		</xsl:when>

		<xsl:when test="parent::Treenode/following-sibling::Treenode[descendant::Leafnode[1][@pos1='WDT' or @pos1='WP' or @pos1='WRB']]">
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_APPOS_WH')"/>	
		</xsl:when>
		<xsl:otherwise>	
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_APPOS_MISC')"/>	
		</xsl:otherwise>
	</xsl:choose>
	
	<xsl:variable name="pos" select="java:getPOS($obj)"/>		
	<Leafnode cat="{@cat}" lexeme="{@lexeme}" pos1="{$pos}" cat0="{@cat0}">
		<xsl:apply-templates select="@*[not(name()='pos1')]|node()"/>
  </Leafnode>
	<xsl:variable name="dummy1" select="java:initPOS($obj)"/>
</xsl:template>

<!--Detect balancing commas for above and leave them untouched-->
<xsl:template match="Leafnode[@pos1=',' and preceding-sibling::Treenode[1]/*[2]/*[3][@pos1=',' and preceding-sibling::Treenode/@cat0='NP' and parent::Treenode[@cat0='NP']/following-sibling::Treenode/@cat0='NP\NP']]">

	<Leafnode>
		<xsl:apply-templates select="@*|node()"/>		
	</Leafnode>

</xsl:template>


<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>




