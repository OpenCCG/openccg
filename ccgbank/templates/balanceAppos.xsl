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

<!--Java helper class-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>
<!--Transform which ensures balancing of appositive nps-->

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<!--Mark cat of unbal appos punct which actually has a balancing comma-->
<xsl:template match="Treenode[*[2][preceding::Leafnode[1]/@pos1='PUNCT_APPOS_WH' and count(descendant::atomcat[@type='punct'])=1] and following::Leafnode[1]/@pos1=',' ]">

	<Treenode>
		<xsl:apply-templates select="@*|*[1]"/>
	
		<Treenode>
			<xsl:apply-templates select="@*"/>
			<complexcat>
				<xsl:apply-templates select="*[1]/*"/>
				<slash dir="/" mode="*"/>
				<atomcat type="punct">
  				<fs id="3">
						<feat attr="lex" val=","/>
					</fs>
  			</atomcat>
			</complexcat>
			<Leafnode cat="{*[2]/@cat}" lexeme="{*[2]/@lexeme}" pos1="{*[2]/@pos1}" cat0="{*[2]/@cat0}">
				<xsl:apply-templates select="*[2]/@*"/>
				<complexcat>
					<xsl:apply-templates select="*[2]/*[1]/*[position() &lt; 6]" mode="punctFeat"/>
					<slash dir="/" mode="*"/>
					<atomcat type="punct">
  					<fs id="3">
							<feat attr="lex" val=","/>
						</fs>
  				</atomcat>
					<slash dir="/" mode="*"/>
					<xsl:apply-templates select="*[2]/*[1]/*[position() &gt; 6]"/>
				</complexcat>
 	 		</Leafnode>
			<xsl:apply-templates select="*[3]" mode="avoid"/>
		</Treenode>
		<Leafnode cat="" lexeme="," pos1="TRIAL_{*[2]/@pos1}_BAL"  pos="," cat0=",">
  		<atomcat type="punct">
   			<fs id="1">
					<feat attr="lex" val=","/>
				</fs>
  		</atomcat>
 		</Leafnode>
	</Treenode>
</xsl:template>

<!--Mark cat of unbal appos punct which actually has a balancing comma-->
<xsl:template match="Treenode[*[2][(@pos1='PUNCT_APPOS_MISC' or @pos1='PUNCT_APPOS' or @pos1 ='PUNCT_APPOS_VRB') and not (descendant::atomcat[@type='punct']) and not(../../preceding-sibling::Leafnode[1]/@cat0='(S[dcl]\S[dcl])/NP')] and (following::Leafnode[1]/@pos1=',' or *[3]/descendant::Leafnode[position()=last()]/@pos1=',')]">

	<Treenode>
		<xsl:apply-templates select="@*|*[1]"/>
	
		<Treenode>
			<xsl:apply-templates select="@*"/>
			<complexcat>
				<xsl:apply-templates select="*[1]/*"/>
				<slash dir="/" mode="*"/>
				<atomcat type="punct">
  				<fs id="3">
						<feat attr="lex" val=","/>
					</fs>
  			</atomcat>
			</complexcat>
			<Leafnode cat="{*[2]/@cat}" lexeme="{*[2]/@lexeme}" pos1="{*[2]/@pos1}" cat0="{*[2]/@cat0}">
				<xsl:apply-templates select="*[2]/@*"/>
				<complexcat>
					<xsl:apply-templates select="*[2]/*[1]/*[position() &lt; 4]" mode="punctFeat"/>
					<slash dir="/" mode="*"/>
					<atomcat type="punct">
  					<fs id="2">
							<feat attr="lex" val=","/>
						</fs>
  				</atomcat>
					<slash dir="/" mode="*"/>
					<xsl:apply-templates select="*[2]/*[1]/*[position() &gt; 4]"/>
				</complexcat>
 	 		</Leafnode>
			<xsl:apply-templates select="*[3]" mode="avoid"/>
		</Treenode>
		<Leafnode cat="" lexeme="," pos1="TRIAL_{*[2]/@pos1}_BAL"  pos="," cat0=",">
  		<atomcat type="punct">
   			<fs id="1">
					<feat attr="lex" val=","/>
				</fs>
  		</atomcat>
 		</Leafnode>
	</Treenode>
</xsl:template>

<xsl:template match="Leafnode/complexcat/*[1]/fs" mode="punctFeat">
	<fs>
		<xsl:apply-templates select="@*"/>
		<xsl:apply-templates mode="punctFeat"/>
	</fs>
</xsl:template>
<xsl:template match="Leafnode/complexcat/*[1]/fs/feat[@attr='bal']" mode="punctFeat"/>

<!--punctFeat global copy rule-->
  <xsl:template match="@*|node()" mode="punctFeat">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="punctFeat"/>
    </xsl:copy>
  </xsl:template>


<!--avoid global copy rule-->
  <xsl:template match="@*|node()" mode="avoid">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="avoid"/>
    </xsl:copy>
  </xsl:template>


<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>
