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

<!--Java helper class-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>
<!--Dots rule-->

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<!--Treenode above the comma in question-->
<xsl:template match="Treenode[@cat0='S[dcl]\NP' and ((*[3]/@cat0='S[dcl]\NP' and *[2]/@lexeme='...') or (*[2]/@cat0='S[dcl]\NP' and *[3]/@lexeme='...'))]">
	
	<xsl:choose>
		<xsl:when test="*[2]/@lexeme='...'">
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_DOTS1')"/>
		</xsl:when>
		<xsl:when test="*[3]/@lexeme='...'">
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_DOTS2')"/>
		</xsl:when>
	</xsl:choose>

	<xsl:variable name="pos" select="java:getPOS($obj)"/>

	<Treenode cat="{@cat}" head="0" dtr="{@dtr}" cat0="{@cat0}">
		<xsl:variable name="feat" select="*[1]/*[1]/fs/feat/@val"/>	
		<xsl:apply-templates select="*[1]"/>		

		<!--dots occurs after adj-->
		<xsl:if test="$pos='PUNCT_DOTS2'">
			<xsl:apply-templates select="*[2]"/>
		</xsl:if>

		<Leafnode cat="" lexeme="..." pos1="{$pos}" cat0=",">
			<xsl:if test="$pos='PUNCT_DOTS2'">
				<xsl:apply-templates select="*[3]/@*[not(name()='pos1')]"/>
			</xsl:if>
			<xsl:if test="$pos='PUNCT_DOTS1'">
				<xsl:apply-templates select="*[2]/@*[not(name()='pos1')]"/>
			</xsl:if>

			<complexcat>
      	<atomcat type="s">
        	<fs inheritsFrom="1">
						<feat attr="form" val="{$feat}"/>
          </fs>
        </atomcat>
        <slash dir="\" mode="&lt;"/>
        <atomcat type="np">
					<fs id="2"/>
				</atomcat>

				<!--dots occurs after adj-->
				<xsl:if test="$pos='PUNCT_DOTS2'">
					<slash dir="\" mode="*"/>		
				</xsl:if>
				
				<!--dots occurs before adj-->
				<xsl:if test="$pos='PUNCT_DOTS1'">
					<slash dir="/" mode="*"/>		
				</xsl:if>
        
				<complexcat>
        	<atomcat type="s">
        		<fs id="1">
							<feat attr="form" val="{$feat}"/>
          	</fs>
        	</atomcat>
        	<slash dir="\" mode="&lt;"/>
        	<atomcat type="np">
						<fs id="2"/>
					</atomcat> 
        </complexcat>
      </complexcat>
		</Leafnode>
		<!--dots occurs before adj-->
		<xsl:if test="$pos='PUNCT_DOTS1'">
			<xsl:apply-templates select="*[3]"/>
		</xsl:if>
		<xsl:variable name="pos" select="java:featInit($obj)"/>
	</Treenode>
</xsl:template>



<!--Default global copy rule-->
<xsl:template match="@*|node()">
	<xsl:copy>
 		<xsl:apply-templates select="@*|node()"/>
	</xsl:copy>
</xsl:template>

</xsl:transform>











