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

<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.InfoHelper.new()"/>

<!--Transform to collapse relevant MWUs (i.e. shared task ones) completely-->

<xsl:template match="/">
  <xsl:apply-templates select="@*|node()"/>
</xsl:template>

<xsl:template match="/">
  <xsl:apply-templates select="@*|node()"/>
</xsl:template>

<!--Only MW atomic treenodes with shared task NE annotation -->
<xsl:template match="Treenode[starts-with(@bbn-info,'ENAMEX=NAME') and count(child::*)>2 and descendant::Leafnode[last()]/child::atomcat]">

	<xsl:variable name="leaf" select="descendant::Leafnode"/>

	<xsl:for-each select="$leaf">
		<xsl:variable name="void" select="java:collapse($obj,@lexeme,'1')"/>
	</xsl:for-each>

	<xsl:variable name="lexeme" select="java:collapse($obj,'','2')"/>

	<xsl:for-each select="$leaf">
		<xsl:variable name="void" select="java:collapse($obj,@lexeme0,'1')"/>
	</xsl:for-each>

	<xsl:variable name="lexeme0" select="java:collapse($obj,'','2')"/>
	<xsl:variable name="header" select="ancestor-or-self::*[@Header]/@Header"/>

	<Leafnode lexeme="{$lexeme}" lexeme0="{$lexeme0}" pos="{$leaf[last()]/@pos}" pos1="{$leaf[last()]/@pos1}" >
    <xsl:apply-templates select="@*"/>
		<xsl:if test="$leaf[last()]/@class">
			<xsl:attribute name="class"><xsl:value-of select="$leaf[last()]/@class"/></xsl:attribute>
		</xsl:if>
		<atomcat type="{@cat}">
    	<fs id="1"/>
		</atomcat>
	</Leafnode>
	

</xsl:template>

<!--Default global copy rule-->
   <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>











