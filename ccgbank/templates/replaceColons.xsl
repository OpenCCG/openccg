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

<!--Colons offend maxent as it searches for the first colon in a feature name-->
<!--Colons delimit real-valued feat names and their activation vals-->

<!--Replace ':' in lexeme & cat attrs by '|'-->

<xsl:template match="/">
  <xsl:apply-templates select="@*|node()"/>
</xsl:template>

	<!--Colons seem to offend maxent. So replace ':' with '|' -->
	<xsl:template match="Leafnode[contains(@lexeme,':') or contains(@pos,':') or contains(@pos1,':')]">  
		<Leafnode>
			
			<xsl:apply-templates select="@*"/>

			<xsl:if test="contains(@lexeme,':')">
					<xsl:variable name="newStr" select="java:replaceColon($obj,@lexeme0)"/>
					<xsl:attribute name="lexeme"><xsl:value-of select="$newStr"/></xsl:attribute>	
			</xsl:if>

			<xsl:if test="contains(@pos,':')">
					<xsl:variable name="newStr" select="java:replaceColon($obj,@pos)"/>
					<xsl:attribute name="pos"><xsl:value-of select="$newStr"/></xsl:attribute>	
			</xsl:if>
			<xsl:if test="contains(@pos1,':')">
					<xsl:variable name="newStr" select="java:replaceColon($obj,@pos1)"/>
					<xsl:attribute name="pos1"><xsl:value-of select="$newStr"/></xsl:attribute>	
			</xsl:if>
      <xsl:apply-templates/>
		</Leafnode>
	</xsl:template>

	<!--Change atomcat type from ':' to its lexical item type-->
	<xsl:template match="atomcat[parent::Leafnode and contains(@type,':')]">

		<atomcat>
			<xsl:variable name="newStr" select="java:replaceColon($obj,../@lexeme)"/>
			<xsl:attribute name="type"><xsl:value-of select="$newStr"/></xsl:attribute>	
			<xsl:apply-templates select="@*[not(name()='type')]"/>
			<xsl:apply-templates/>
		</atomcat>

	</xsl:template>

<!--Change feature vals with ':' to '|'-->
<xsl:template match="feat[contains(@val,':')]">

	<feat>
		
		<xsl:variable name="newStr" select="java:replaceColon($obj,@val)"/>
		<xsl:attribute name="val"><xsl:value-of select="$newStr"/></xsl:attribute>	
		<xsl:apply-templates select="@*[not(name()='val')]"/>
		<xsl:apply-templates/>
	</feat>
</xsl:template>

<!--Default global copy rule-->

   <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>











