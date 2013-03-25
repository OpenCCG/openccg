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

<!--Transform to perform uncurrying & allot term_nos to lexical items-->

<xsl:template match="/">
  <xsl:apply-templates select="@*|node()"/>
</xsl:template>

<xsl:template match="Treenode">

	<xsl:if test="@Header">
		<xsl:variable name="void" select="java:storeSentId($obj,@Header)"/>
	</xsl:if>

	<xsl:if test="@ptb-tag0">
		<xsl:variable name="void" select="java:removeFirstPassAnnotation($obj,@ptb-tag0)"/>
	</xsl:if>


	<xsl:variable name="chi" select="descendant::Leafnode"/>

  <xsl:for-each select="$chi">
    <xsl:variable name="void1" select="java:storeEdges($obj,number(@term_no))"/>
  </xsl:for-each>

	<xsl:variable name="tag" select="java:getTag($obj,'true')"/>

  <Treenode>

		<xsl:if test="string-length($tag) &gt; 0 ">
			<xsl:attribute name="ptb-tag"><xsl:value-of select="$tag"/></xsl:attribute>
			<xsl:variable name="void" select="java:countSecPass($obj)"/>

		</xsl:if>

    <xsl:apply-templates select="@*|node()"/>
  </Treenode>

	<xsl:if test="@Header">
		<xsl:variable name="void" select="java:recordRem($obj)"/>
	</xsl:if>

</xsl:template>

<!--Leafnodes containing function tags-->
<xsl:template match="Leafnode">
  <xsl:variable name="tag" select="java:getTag($obj,@term_no,'true')"/>

  <Leafnode>
		<xsl:if test="string-length($tag) &gt; 0">
    	<xsl:attribute name="ptb-tag"><xsl:value-of select="$tag"/></xsl:attribute>
			<xsl:variable name="void" select="java:countSecPass($obj)"/>		
			
		</xsl:if>
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











