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

<!--Transform which provides feats for sent final puncts like !,? . etc-->

<!--Java bkgrd class which helps punct annotation-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>
<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<!--Take away extra stuff for sents with parens around them-->
<xsl:template match="Treenode[@cat0='S[dcl]' and *[2]/*[2]/@pos1='LPAREN']">
	<xsl:apply-templates select="*[position()>1]"/>		
</xsl:template>

<!--Remove extra colons-->
<xsl:template match="Treenode[not(@Header) and *[3][following::Leafnode[1]/@pos1=':' and  @lexeme=':' and @cat0=':']]">
	<xsl:apply-templates select="*[2]"/>
</xsl:template>


<!--Periods of sentences enclosed by brackets-->
<xsl:template match="Leafnode[@cat='._1' and @pos1='.' and parent::Treenode[@cat='sent_1']/*[2]/@cat0='S[dcl]']">

	<Leafnode>
		<xsl:apply-templates select="@*"/>
		<complexcat>
    	<atomcat type="sent">
      	<fs id="1"/>
       </atomcat>
       <slash dir="\" mode="*"/>
			 <atomcat type="s">
					<fs id="1">
       			<xsl:apply-templates select="../*[2]/*[1]/fs/*"/>
					</fs>
			 </atomcat>
    </complexcat>
	</Leafnode>
	
</xsl:template>

<!--Default global copy rule-->
<xsl:template match="@*|node()">

	<xsl:copy>
 		<xsl:apply-templates select="@*|node()"/>
	</xsl:copy>
</xsl:template>

</xsl:transform>











