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

<!--Transform to collapse relevant MWUs completely-->

<xsl:template match="/">
  <xsl:apply-templates select="@*|node()"/>
</xsl:template>

<!--Completely collapse PERSON, ORG, WOA & GPE classes and partially all other Treenodes containing NNP(S),CD pos tags and %, $ lexemes-->

<xsl:template match="/">
  <xsl:apply-templates select="@*|node()"/>
</xsl:template>

<xsl:template match="Treenode[((starts-with(@bbn-info,'ENAMEX=PERSON') or starts-with(@bbn-info,'ENAMEX=ORGANIZATION|') or starts-with(@bbn-info,'ENAMEX=GPE|') or contains(@bbn-info,'ENAMEX=WORK_OF_ART')) or count(descendant::Leafnode)=count(descendant::Leafnode[starts-with(@pos,'NNP') or @pos='CD' or @lexeme='$' or @lexeme='%'])) and (*[1][self::atomcat] or java:checkTreeInfo($obj,ancestor-or-self::*[@Header]/@Header,@nt_id,number(count(*[1]/descendant::*[self::slash or self::atomcat]))))]">

	<xsl:variable name="leaf" select="descendant::Leafnode"/>

	<xsl:for-each select="$leaf">
		<xsl:variable name="void" select="java:collapse($obj,@lexeme,'1')"/>
	</xsl:for-each>

	<xsl:variable name="lexeme" select="java:collapse($obj,'','2')"/>
	<xsl:variable name="class" select="substring-after(substring-before(@bbn-info,' '),'=')"/>

	<xsl:for-each select="$leaf">
		<xsl:variable name="void" select="java:collapse($obj,@lexeme0,'1')"/>
	</xsl:for-each>

	<xsl:variable name="lexeme0" select="java:collapse($obj,'','2')"/>
	<xsl:variable name="header" select="ancestor-or-self::*[@Header]/@Header"/>

	<Leafnode>
		<xsl:apply-templates select="$leaf[last()]/@*[not(name()='lexeme' and name()='lexeme0')]"/>
		<xsl:attribute name="lexeme"><xsl:value-of select="$lexeme"/></xsl:attribute>
		<xsl:attribute name="lexeme"><xsl:value-of select="$lexeme0"/></xsl:attribute>
		<xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
		<xsl:if test="$leaf[last()]/@class">
			<xsl:attribute name="class"><xsl:value-of select="$leaf[last()]/@class"/></xsl:attribute>
		</xsl:if>
		<xsl:if test="$leaf[position()=1]/@fntag">
			<xsl:attribute name="fntag"><xsl:value-of select="$leaf[position()=1]/@fntag"/></xsl:attribute>
		</xsl:if>
				<xsl:if test="$leaf[position()=1]/@tpc">
			<xsl:attribute name="tpc"><xsl:value-of select="$leaf[position()=1]/@tpc"/></xsl:attribute>
		</xsl:if>

		<xsl:choose>
			<xsl:when test="*[1][self::atomcat]">
				<atomcat type="{@cat}">
    			<fs id="1"/>
    		</atomcat>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="tree-info" select="java:getTreeInfo($obj,concat($header,' ',@nt_id))"/>
				<xsl:apply-templates select="*[1]" mode="tree-id"/>
				
			</xsl:otherwise>
		</xsl:choose>
	</Leafnode>
</xsl:template>

<!--Template which allots actual Treenode ids from info in the bkgrnd class-->
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
</xsl:template>

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











