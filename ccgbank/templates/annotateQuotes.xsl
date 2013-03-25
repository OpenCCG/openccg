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

<!--Transform to insert analyses for quotation marks into CCG derivs-->

<xsl:template match="/">
  <xsl:apply-templates select="@*|node()"/>
</xsl:template>

<xsl:variable name="rquote">''</xsl:variable> 
<xsl:variable name="lquote">``</xsl:variable> 

<!--Confer categories to quoted phrases-->

<xsl:template match="Treenode[(*[1][self::atomcat])  and @quote-info]">

	<xsl:variable name="quoteSpan" select="concat(substring-before(@quote-info,' '),' ')"/>

	<xsl:choose >
		<xsl:when test = "count(parent::Treenode[starts-with(@quote-info,$quoteSpan)])=0" >

			<!--Store result cat-->
			<xsl:variable name="res1" select="java:storeRes($obj,@cat)"/>
			<xsl:variable name="res" select="java:getRes($obj)"/>

			<Treenode cat="{$res}" head="0" dtr="2">
				<atomcat type="{$res}"/>
				<!--<xsl:apply-templates select="*[1]"/>-->
				<xsl:call-template name = "bal_punct"/>
				<Leafnode cat="punct['']_1" lexeme="{$rquote}" pos1="PUNCT_QUOTE" cat0="{$rquote}" lexeme0="{$rquote}" pos="{$rquote}">
					<atomcat type="punct">
						<fs id="3">
							<feat attr="lex" val="{$rquote}"/>
						</fs>
					</atomcat>
				</Leafnode>
			</Treenode>
    </xsl:when>
    <xsl:otherwise >
			<xsl:call-template name = "tree" />
    </xsl:otherwise>
  </xsl:choose> 
</xsl:template>

<xsl:template name = "bal_punct" >

	<xsl:variable name="res" select="java:getRes($obj)"/>

	<xsl:variable name="temp" select="substring-after(@quote-info,' ')"/>
	<xsl:variable name="termPunct" select="substring-after($temp,' ')"/>

	<Treenode head="0" dtr="2">
		
		<complexcat>
			<atomcat type="{$res}"/>
			<slash dir="/" mode="*"/>
			<atomcat type="punct">
				<fs>
					<feat attr="lex" val="{$rquote}"/>
				</fs>
			</atomcat>
		</complexcat>
		<xsl:call-template name = "quoteLeaf" />
		<xsl:call-template name = "tree" />
		
	</Treenode>

</xsl:template>

<xsl:template name = "quoteLeaf" >

	<xsl:variable name="res" select="java:getRes($obj)"/>
	<xsl:variable name="info" select="substring-after(@quote-info,' ')"/>
	<xsl:variable name="balStatus" select="substring-before($info,' ')"/>
	<xsl:variable name="termPunct" select="substring-after($info,' ')"/>

	<Leafnode cat="s_~1/*punct[{$rquote}]_3/*punct[,]_2/*s_1" lexeme="{$lquote}" pos1="PUNCT_QUOTE" cat0="{$lquote}" lexeme0="{$lquote}" pos="{$lquote}">
    <complexcat>
      <atomcat type="{$res}">
        <fs id="1"/>
      </atomcat>
			<slash dir="/" mode="*"/>
			<atomcat type="punct">
				<fs id="3">
					<feat attr="lex" val="{$rquote}"/>
				</fs>
			</atomcat>
			<slash dir="/" mode="*"/>
      <atomcat type="{*[1]/@type}">
        <fs id="2"/>
      </atomcat>
    </complexcat>
  </Leafnode>
</xsl:template>

<xsl:template name = "tree" >
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









