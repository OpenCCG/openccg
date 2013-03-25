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

<!--Java Program in the parse package invoked-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.ApposTally.new()"/>

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<!--Storing id of the sent in a java var-->
<xsl:template match="Treenode[@Header]">
	<xsl:variable name="dummy1" select="java:storeId($obj,@Header)"/>
	<Treenode cat="{@cat}" Header="{@Header}" head="{@head}" dtr="{@dtr}" cat0="{@cat}">
  	<xsl:apply-templates select="@*|node()"/>
	</Treenode>
</xsl:template>


<!--Heuristic 2: Template which does cue analysis-->

<!--np[conj] Commas which are not preceded by a colon selected-->
<xsl:template match="Treenode[@cat='np[conj]' and Leafnode[@lexeme=',' and @pos1='PUNCT_CONJ'] and not(../preceding-sibling::Leafnode/@lexeme=':')]">
	<!--Re-init balance status var in background java class-->
	<xsl:variable name="dummy0" select="java:flushVars($obj)"/>
	
	<!--np1: Main np , np2: appos np-->

	<!--Find lexical mtl of np1+np2-->
	<xsl:apply-templates select="preceding-sibling::*[1]" mode="interLex"/>
	<xsl:variable name="dummy1" select="java:storeLex($obj,'X','X')"/>
	<xsl:apply-templates select="*[3]" mode="interLex"/>
	
	<!--Stick heads into the tail of the sentence-->
	<xsl:variable name="dummy4" select="java:storeLex($obj,'Heads','')"/>
	
	<!--Find heads of np1, np2 (appos np)-->
	<xsl:apply-templates select="preceding-sibling::*[1]" mode="headFindGen1"/>	
	<xsl:apply-templates select="*[3]" mode="headFindGen2"/>	

	<xsl:variable name="res" select="java:cueAnalysis($obj)"/>
	<xsl:variable name="resCaps" select="java:getCaps($obj,$res)"/>

	<!--Get and display result of analysis-->
	
	<xsl:choose>
	<xsl:when test="not($res='conj')">
		<!--<xsl:variable name="dummy1" select="java:printLex($obj)"/>-->
	
	<!--Modify np[conj]-->
	<Treenode cat="np\np" head="{@head}" dtr="{@dtr}" cat0="{@cat0}">
		<xsl:apply-templates select="@*"/>
		<complexcat>
			<atomcat type="np">
    		<fs id="1">
				</fs>
    	</atomcat>
			<slash dir="\" mode="&lt;"/>
			<atomcat type="np">
    		<fs id="1">
				</fs>
    	</atomcat>
		</complexcat>
		<Leafnode cat="np_~1\np_1/*np_2" lexeme="{Leafnode/@lexeme}" pos1="PUNCT_{$resCaps}" cat0="{Leafnode/@lexeme}">
			<xsl:apply-templates select="*[2]/@*[not(name()='pos1')]"/>
			<complexcat>
				<atomcat type="np">
    			<fs inheritsFrom="1">
					</fs>
    		</atomcat>
				<slash dir="\" mode="&lt;"/>
				<atomcat type="np">
    			<fs id="1">
					</fs>
    		</atomcat>
				<slash dir="/" mode="*"/>
				<atomcat type="np">
    			<fs id="3">
					</fs>
    		</atomcat>
			</complexcat>
		</Leafnode>
  	<xsl:apply-templates select="*[position()>2]"/>
  </Treenode>
	</xsl:when>	
	<xsl:otherwise>
		<Treenode cat="np[{$res}]" head="{@head}" dtr="{@dtr}" cat0="{@cat0}">
    	<xsl:apply-templates select="@*[not(name='cat')]|node()"/>
    </Treenode>
	</xsl:otherwise>
	</xsl:choose>	
</xsl:template>	

<!--Template which traps head noun of main np-->
<xsl:template match = "Leafnode[count(atomcat)=1 and atomcat[1]/@type='n']" mode="headFindGen1">
	<xsl:variable name="lex" select="concat('-',@lexeme)"/>
	<xsl:variable name="dummy1" select="java:storeLex($obj,$lex,@pos1)"/>
	<!--Stores head info-->
	<xsl:variable name="dummy2" select="java:storeHead($obj,@lexeme,@pos1,1)"/>
</xsl:template>
<!--Skipping modifiers in head nouns-->
<xsl:template match = "Treenode[@cat='np\np']" mode="headFindGen1"/>

<!--Template which traps head noun of appos np-->
<xsl:template match = "Leafnode[count(atomcat)=1 and atomcat[1]/@type='n']" mode="headFindGen2">
	<xsl:variable name="lex" select="concat('-',@lexeme)"/>
	<xsl:variable name="dummy1" select="java:storeLex($obj,$lex,@pos1)"/>
	<!--Stores head info-->
	<xsl:variable name="dummy2" select="java:storeHead($obj,@lexeme,@pos1,2)"/>
</xsl:template>
<!--Skipping modifiers in head nouns-->
<xsl:template match = "Treenode[@cat='np\np']" mode="headFindGen2"/>


<!--Copying intervening lexical mtl-->
<xsl:template match = "Leafnode" mode="interLex">
	<!--Store intervening lexical mtl -->
  <xsl:variable name="dummy" select="java:storeLex($obj,@lexeme,@pos1)"/>
</xsl:template>
<xsl:template match = "Treenode" mode="interLex">
	<xsl:apply-templates mode="interLex"/>
</xsl:template>

<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>




