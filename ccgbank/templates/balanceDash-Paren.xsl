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

<!--Transform which adds the balancing punct to dash and left bracket-->


<!--Java helper class-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<!--Delete existing balancing dash-->
<xsl:template match="Treenode[*[3]/@lexeme='--' and descendant::Leafnode/@pos1='PUNCT_ELAB_DASH_CAT']">
	<xsl:apply-templates select="*[2]"/>
</xsl:template>

<!--Delete existing balancing bracket-->
<xsl:template match="Treenode[*[3][@lexeme='-rrb-' or @lexeme='-rcb-'] and *[2]/*[2]/@pos1='PUNCT_LPAREN0']">
	<xsl:apply-templates select="*[2]"/>
</xsl:template>

<xsl:template match="Treenode[*[2][@pos1='PUNCT_ELAB_DASH_CAT' or @pos1='PUNCT_LPAREN0']]">
	
	<xsl:choose>
		<xsl:when test="*[2]/@pos1='PUNCT_LPAREN0'">
      <xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_LPAREN0')"/>
			<xsl:variable name="dummy1" select="java:storeFeat($obj,concat('-r',substring(*[2]/@lexeme,3,5)))"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_ELAB_DASH_CAT')"/>
			<xsl:variable name="dummy1" select="java:storeFeat($obj,*[2]/@lexeme)"/>
    </xsl:otherwise>
  </xsl:choose>
	<xsl:variable name="pos" select="java:getPOS($obj)"/>
	<xsl:variable name="lexVal" select="java:getFeat($obj)"/>

	<Treenode cat="" head="0" dtr="2" cat0="NP\NP">
		<xsl:apply-templates select="@*|*[1]"/>	
		<Treenode cat="np\np/*punct[,]" head="0" dtr="2" cat0="">
			<xsl:apply-templates select="@*"/>	
      <complexcat>
				<xsl:apply-templates select="*[1]/*"/>	
				<slash dir="/" mode="*"/>
        <atomcat type="punct">
         	<fs id="2">
           	<feat attr="lex" val="{$lexVal}"/>
         	</fs>
        </atomcat>
      </complexcat>
			<Leafnode cat="{*[2]/@cat}" lexeme="{*[2]/@lexeme}" pos1="{$pos}" cat0="{*[2]/@cat0}">
				<xsl:apply-templates select="*[2]/@*[not(name()='pos1')]"/>	
				<complexcat>
							
					<xsl:apply-templates select="*[2]/complexcat/*[position() &lt; last()-1]"/>

					<slash dir="/" mode="*"/>
					<atomcat type="punct">
  	  			<fs id="11">
    					<feat attr="lex" val="{$lexVal}"/>
						</fs>
      		</atomcat>
					<slash dir="/" mode="*"/>
					<xsl:apply-templates select="*[2]/complexcat/*[position()=last()]"/>
				</complexcat>
			</Leafnode>
			<xsl:apply-templates select="*[3]"/>	
		</Treenode>	
		<Leafnode cat="punct[--]_1" lexeme="{$lexVal}" pos=":" pos1="{$pos}_BAL" cat0=",">
    	<atomcat type="punct">
     		<fs id="1">
       		<feat attr="lex" val="{$lexVal}"/>
       	</fs>
     	</atomcat>
    </Leafnode>
		<xsl:variable name="dummy0" select="java:featInit($obj)"/>
		<xsl:variable name="dummy1" select="java:initPOS($obj)"/>	
	</Treenode>	
</xsl:template>

<!--Default global copy rule-->
	<xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
	</xsl:template>

</xsl:transform>




