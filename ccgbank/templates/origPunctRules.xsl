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

<!--Transform which inserts punct cats corresponding to orig binary rules in corpus-->

<!--Java helper class-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.OrigPunctRules.new()"/>

<!--Punctuation whose context is to be determined-->
<xsl:variable name="punct">,</xsl:variable>


<xsl:template match="/">
 	<xsl:apply-templates/>
</xsl:template>

<!--Store the sentence id of a sent in the bkgrnd java class-->
<xsl:template match="Treenode[@Header]">
	<xsl:variable name="dummy1" select="java:storeId($obj,@Header)"/>
	<Treenode>
		<xsl:copy-of select="@*"/>
		<xsl:apply-templates/>
	</Treenode>
</xsl:template>

<xsl:template match="Leafnode[@Header]">
	<xsl:variable name="dummy1" select="java:storeId($obj,@Header)"/>
	<Leafnode>
		<xsl:copy-of select="@*"/>
		<xsl:apply-templates/>
	</Leafnode>
</xsl:template>

<!--For every leaf node which has the selected punctuation mark-->
<xsl:template match="Leafnode[(@lexeme=',' and @cat0=',') or (@lexeme='--' or @lexeme=':' or @lexeme='...' and (@cat0=':'))  or (@pos='.' and @cat='._1')]">
	
	<!--Send arguments and results of the mark to the bkgrnd java class-->
	<xsl:variable name="res" select="parent::Treenode/*[1]"/>
	<xsl:variable name="prec" select="preceding-sibling::*[1][self::Leafnode or self::Treenode]/*[1]"/>
	<xsl:variable name="foll" select="following-sibling::*[1][self::Leafnode or self::Treenode]/*[1]"/>

	<xsl:variable name="dummy0" select="java:initId($obj)"/>	
	<Leafnode cat="{@cat}" lexeme="{@lexeme}" lexeme0="{@lexeme0}" pos="{@pos}" cat0="{@cat0}" pos1="PUNCT">
		<complexcat>
			<xsl:apply-templates select="$res" mode="res"/>
			<xsl:if test="count($prec)>0">
				<slash dir="\" mode="*"/>	
				<xsl:apply-templates select="$prec" mode="arg"/>
			</xsl:if>	
			<xsl:if test="count($foll)>0">
				<slash dir="/" mode="*"/>	
				<xsl:apply-templates select="$foll" mode="arg"/>
			</xsl:if>			
		</complexcat>
	</Leafnode>
</xsl:template>

<xsl:template match="atomcat[1]" mode="res">
	<atomcat>
		<xsl:copy-of select="@*"/>
		<fs inheritsFrom="1">
		</fs>
		<xsl:apply-templates mode="res"/>
	</atomcat>
</xsl:template>

<xsl:template match="atomcat[1]" mode="arg">
	<atomcat>
		<xsl:copy-of select="@*"/>
		<fs id="1">
		</fs>
		<xsl:apply-templates mode="arg"/>
	</atomcat>
</xsl:template>

<xsl:template match="fs" mode="res"/>
<xsl:template match="fs" mode="arg"/>

<xsl:template match="@*|node()" mode="res">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="res"/>
    </xsl:copy>
  </xsl:template>

<xsl:template match="@*|node()" mode="arg">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="arg"/>
    </xsl:copy>
  </xsl:template>

<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>




