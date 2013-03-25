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
  xmlns:exsl="http://exslt.org/common"
  extension-element-prefixes="exsl"
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xalan xalan2 java">

  <xsl:output method="xml" indent="yes" xalan2:indent-amount="2"/>
  <xsl:strip-space elements="*"/>

<!--Java class to manipulate strings of args and results of conj rule-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.InfoHelper.new()"/>
<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="Treenode[@ptb-tag=@ptb-tag0]">
  <Treenode>
    <xsl:apply-templates select="@*[not(name()='ptb-tag0')]|node()"/>
  </Treenode>
</xsl:template>

<xsl:template match="Treenode[not(@ptb-tag=@ptb-tag0) and @ptb-tag and @ptb-tag0]">
  <Treenode>
		<xsl:attribute name="CAUTION">X</xsl:attribute>
    <xsl:apply-templates select="@*[not(name()='ptb-tag0')]|node()"/>
  </Treenode>
</xsl:template>

<xsl:template match="Treenode[@ptb-tag0 and not(@ptb-tag)]">
  <Treenode>
		 <xsl:attribute name="ptb-tag"><xsl:value-of select="@ptb-tag0"/></xsl:attribute>
	   <xsl:apply-templates select="@*[not(name()='ptb-tag0')]|node()"/>
  </Treenode>
</xsl:template>


<xsl:template match="Leafnode[@ptb-tag=@ptb-tag0]">
  <Leafnode>
    <xsl:apply-templates select="@*[not(name()='ptb-tag0')]|node()"/>
  </Leafnode>
</xsl:template>

<xsl:template match="Leafnode[@ptb-tag0 and not(@ptb-tag)]">
  <Leafnode>
		 <xsl:attribute name="ptb-tag"><xsl:value-of select="@ptb-tag0"/></xsl:attribute>
	   <xsl:apply-templates select="@*[not(name()='ptb-tag0')]|node()"/>
  </Leafnode>
</xsl:template>

<xsl:template match="Leafnode[not(@ptb-tag=@ptb-tag0) and @ptb-tag and @ptb-tag0]">
  <Leafnode>
		<xsl:attribute name="CAUTION">X</xsl:attribute>
    <xsl:apply-templates select="@*[not(name()='ptb-tag0')]|node()"/>
  </Leafnode>
</xsl:template>


<!--Default global copy rule-->
<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:transform>
