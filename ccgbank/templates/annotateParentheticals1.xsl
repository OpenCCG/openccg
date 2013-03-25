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
<!--Transform which identifies & labels sentence medial parentheticals-->

<xsl:template match="/">
    <xsl:apply-templates/>
</xsl:template>

<xsl:template match="Treenode[@Header]">
	<xsl:variable name="dummy0" select="java:storePOS($obj,@Header)"/>
	<Treenode>
		<xsl:apply-templates select="@*|node()"/>
	</Treenode>
</xsl:template>


<!--Lexicalizing adverbial parentheticals-->
<xsl:template match="Leafnode[@pos1='RB'and preceding::Leafnode[1][@pos1=',' and ancestor::Treenode[@Header=(java:getPOS($obj))]] and following::Leafnode[1][@pos1=',' and ancestor::Treenode[@Header=(java:getPOS($obj))]]]">

	<xsl:variable name="comma1" select="preceding::Leafnode[1]"/>
	<xsl:variable name="comma2" select="following::Leafnode[1]"/>
	<xsl:variable name="res" select="*[1]/*"/>

	<Treenode head="1" dtr="2">
		<xsl:apply-templates select="*[1]"/>
		<Leafnode pos1="PUNCT_ADV-PARENTHETICAL_BAL1" pos=",">
			<xsl:apply-templates select="$comma1/@*[not(name()='pos1')]"/>
			<atomcat type="punct">
      	<fs id="1">
      		<feat attr="lex" val=","/>
        </fs>
    	</atomcat>
		</Leafnode>		
		<Treenode head="0" dtr="2">
			<complexcat>
				<xsl:apply-templates select="$res"/>
				<slash dir="\" mode="*"/>
				<atomcat type="punct">
      		<fs id="7">
      			<feat attr="lex" val=","/>
        	</fs>
      	</atomcat>
			</complexcat>		
			<Leafnode>
				<xsl:attribute name="paren">ADV-PAREN</xsl:attribute>
				<xsl:apply-templates select="@*"/>
				<complexcat>
					<xsl:apply-templates select="$res"/>
					<slash dir="\" mode="*"/>
					<atomcat type="punct">
      			<fs id="7">
      				<feat attr="lex" val=","/>
        		</fs>
      		</atomcat>
					<slash dir="/" mode="*"/>
					<atomcat type="punct">
      			<fs id="8">
      				<feat attr="lex" val=","/>
        		</fs>
      		</atomcat>
				</complexcat>
			</Leafnode>	
			<Leafnode pos1="PUNCT_ADV-PARENTHETICAL_BAL2" pos=",">
				<xsl:apply-templates select="$comma2/@*[not(name()='pos1')]"/>
				<atomcat type="punct">
      		<fs id="1">
      			<feat attr="lex" val=","/>
        	</fs>
    		</atomcat>
			</Leafnode>		
		</Treenode>
	</Treenode>
</xsl:template>

<!--PP/Verbal-parenthetical: First comma selects parenthetical and following comma-->
<xsl:template match="Treenode[(not(@cat0='NP\NP') and (@cat0='S/S' or @cat0='(S\NP)/(S\NP)' or @cat0='(S\NP)\(S\NP)' or starts-with(@ptb-tag0,'PP'))) and preceding::Leafnode[1][((@lexeme='--' and @pos1=':') or @pos1=',') and ancestor::Treenode[@Header=(java:getPOS($obj))]] and following::Leafnode[1][((@lexeme='--' and @pos1=':') or @pos1=',') and ancestor::Treenode[@Header=(java:getPOS($obj))]]]">

	<xsl:variable name="punct1" select="preceding::Leafnode[1]"/>
	<xsl:variable name="punct2" select="following::Leafnode[1]"/>
	<xsl:variable name="punct-lex" select="$punct2/@lexeme"/>
	<xsl:variable name="res" select="descendant::Leafnode[1]/*[1]/*[1]"/>

	<xsl:choose>
		<xsl:when test="@dtr='1'">
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_PARENTHETICAL_VRB')"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_PARENTHETICAL')"/>
		</xsl:otherwise>
			
	</xsl:choose>
	<xsl:variable name="pos" select="java:getPOS($obj)"/>		
	<xsl:variable name="void1" select="java:initPOS($obj)"/>		

	<Treenode>
		<xsl:apply-templates select="@*[not(name()='ptb-tag0' or name()='constr')]"/>
		<xsl:apply-templates select="*[1]"/>

		<Treenode>
			<xsl:apply-templates select="@*[not(name()='ptb-tag0' or name()='constr')]"/>
			<complexcat>
				<xsl:apply-templates select="*[1]"/>
				<slash dir="/" mode="*"/>
				<atomcat type="punct">
      		<fs id="1">
      	  	<feat attr="lex" val="{$punct-lex}"/>
        	</fs>
      	</atomcat>
			</complexcat>

			<Leafnode pos1="{$pos}" pos="{$punct1/@pos}">
				<xsl:apply-templates select="$punct1/@*[not(name()='pos1')]"/>
				<complexcat>
					<xsl:apply-templates select="*[1]" mode="mod-arg"/>
					<slash dir="/" mode="*"/>
					<atomcat type="punct">
      			<fs id="7">
      	  		<feat attr="lex" val="{$punct-lex}"/>
        		</fs>
      		</atomcat>
					<slash dir="/" mode="*"/>
					
					<xsl:if test="@dtr='2'">
						<xsl:apply-templates select="*[1]" mode="mod-arg"/>
					</xsl:if>
					
					<xsl:if test="@dtr='1'">
						<xsl:apply-templates select="*[2]/*[1]" mode="vrb-arg"/>
						<xsl:variable name="id" select="java:globalInit($obj)"/>		
					</xsl:if>
				</complexcat>
			</Leafnode>

			<xsl:choose>
				<xsl:when test="@dtr='1'">
					<xsl:apply-templates select="*[2]"/>
				</xsl:when>
				<xsl:otherwise>
					<Treenode>
						<xsl:attribute name="constr">PP-PAREN</xsl:attribute>
						<xsl:apply-templates select="@*|node()"/>
					</Treenode>		
				</xsl:otherwise>
			</xsl:choose>
		</Treenode>
		<Leafnode pos1="PUNCT_PARENTHETICAL_BAL" pos="{$punct2/@pos}">
			<xsl:apply-templates select="$punct2/@*[not(name()='pos1')]"/>
			<atomcat type="punct">
      	<fs id="1">
      		<feat attr="lex" val="{$punct-lex}"/>
        </fs>
    	</atomcat>
		</Leafnode>	
	</Treenode>

</xsl:template>

<xsl:template match="atomcat[@type='s']" mode="mod-arg">

	<xsl:variable name="id" select="java:getPOS($obj)"/>		

	<atomcat type="s">
		<xsl:choose>
			<xsl:when test="string-length($id) &gt; 0">
				<fs id="1"/>
				<xsl:variable name="void" select="java:initPOS($obj)"/>		
			</xsl:when>
			<xsl:otherwise>
				<fs inheritsFrom="1"/>
				<xsl:variable name="void" select="java:storePOS($obj,'1')"/>		
			</xsl:otherwise>
		</xsl:choose>
	</atomcat>
	
</xsl:template>
<xsl:template match="atomcat[@type='np']" mode="mod-arg">
	<atomcat type="np">
		<fs id="2"/>
	</atomcat>
</xsl:template>

<xsl:template match="atomcat[@type='s']" mode="vrb-arg">

	<xsl:variable name="id0" select="number(java:getglobalId($obj))-1"/>		

	<xsl:choose>
		<xsl:when test="$id0 &lt; 3">
			<xsl:variable name="void" select="java:setglobalId($obj,2)"/>		
		</xsl:when>	
		<xsl:otherwise>
			<xsl:variable name="void" select="java:setglobalId($obj,number($id0))"/>
		</xsl:otherwise>
	</xsl:choose>

	<atomcat type="s">
		<xsl:variable name="id" select="java:getglobalId($obj)"/>		
		<fs id="{$id}"/>
	</atomcat>
	
</xsl:template>
<xsl:template match="atomcat[@type='np']" mode="vrb-arg">
	<atomcat type="np">
		<fs id="2"/>
	</atomcat>
</xsl:template>

<!-- Spl copy rule 1-->
  <xsl:template match="@*|node()" mode="mod-arg">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="mod-arg"/>
    </xsl:copy>
  </xsl:template>

<!-- Spl copy rule 2-->
  <xsl:template match="@*|node()" mode="vrb-arg">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="vrb-arg"/>
    </xsl:copy>
  </xsl:template>

<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>
