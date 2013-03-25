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

<!--Transform to allot term_nos to non-terminals (Treenodes)-->

<!--Also gives full fledged cats with feat ids for hitherto unmarked Treenode cats using CreateTestbed derived aux files-->

<xsl:template match="/">
  <xsl:apply-templates select="@*|node()"/>
</xsl:template>

<xsl:template match="Treenode[@Header]">

  <xsl:variable name="void" select="java:initId($obj)"/>
  <Treenode nt_id="{generate-id()}">
   <xsl:apply-templates select="@*|node()"/>
  </Treenode>
</xsl:template>

<!--Allot xslt ids and cat ids to treenodes-->
<xsl:template match="Treenode">

	<xsl:variable name="header" select="ancestor-or-self::*[@Header]/@Header"/>
	<xsl:variable name="nt-id" select="java:getNonTermNo($obj)"/>
	<xsl:variable name="tree-info" select="java:getTreeInfo($obj,concat($header,' ',$nt-id))"/>

	<Treenode nt_id="{$nt-id}">

		<xsl:choose>
	 	<xsl:when test="string-length($tree-info)>0 and (java:checkTreeInfo($obj,$header,$nt-id,number(count(*[1]/descendant::atomcat))))">
      <xsl:attribute name="cat-full"><xsl:value-of select="$tree-info"/></xsl:attribute>
			<xsl:apply-templates select="@*"/>	
			<xsl:apply-templates select="*[1]" mode="tree-id"/>	
			<xsl:apply-templates select="*[position() &gt; 1]"/>	
    </xsl:when>
		<xsl:otherwise>
			<xsl:apply-templates select="@*|node()"/>	
		</xsl:otherwise>
		</xsl:choose>
  </Treenode>
</xsl:template>

<!--Template which allots actual Treenode ids from info in the bkgrnd class
<xsl:template match="atomcat" mode="tree-id">
	<xsl:variable name="tree-cat" select="java:getTreeId($obj)"/>
	<atomcat type="{@type}">
		<xsl:choose>
			<xsl:when test="starts-with($tree-cat,'M_')">
				<fs inheritsFrom="{substring-after($tree-cat,'_')}">
					<xsl:if test="descendant::fs">
						<xsl:apply-templates select="descendant::fs/*"/>
					</xsl:if>
				</fs>
			</xsl:when>
			<xsl:otherwise>
				<fs id="{$tree-cat}">
					<xsl:if test="descendant::fs">
						<xsl:apply-templates select="descendant::fs/*"/>
					</xsl:if>
				</fs>
			</xsl:otherwise>	
		</xsl:choose>
	</atomcat>
</xsl:template>

<xsl:template match="slash" mode="tree-id">
	<xsl:variable name="slash" select="java:getTreeSlash($obj)"/>
	<slash dir="{substring-before($slash,'_')}" mode="{substring-after($slash,'_')}"/>
</xsl:template>-->


<!--Tree-id copy rule-->
   <xsl:template match="@*|node()" mode="tree-id">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="tree-id"/>
    </xsl:copy>
  </xsl:template>


<!--Default global copy rule-->

   <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>











