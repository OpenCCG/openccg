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
<!--Transform which intr colon rules-->

<xsl:template match="/">
	<xsl:apply-templates/>
</xsl:template>

<!--Cat 1-->
<xsl:template match="Treenode[@Header and count(*)=2 and descendant::Leafnode[last() and @lexeme=':' and @cat0=':']]">

	<Treenode>
		<xsl:copy-of select="@*"/>
		<xsl:apply-templates select="*"/>
		<Leafnode cat="sent_1\*x" lexeme=":" pos1=":" pos=":" cat0=":">
      <complexcat>
        <atomcat type="sent">
          <fs id="1"/>
        </atomcat>
        <slash dir="\" mode="*"/>
				<xsl:choose>
					<xsl:when test="*[2]/*[1][self::atomcat]">
						<atomcat type="{*[2]/*[1]/@type}">
          		<fs id="1"/>
		        </atomcat>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates select="*[2]/*[1]"/>
					</xsl:otherwise>
				</xsl:choose>
      </complexcat>
    </Leafnode>
	</Treenode>
</xsl:template>

<!--Cat 2-->
<!--<xsl:template match="Treenode[*[2][java:removeFeats($obj,@cat0)='(S\NP)/S'] and *[3][@lexeme=':' and @cat0=':']]">-->

<xsl:template match="Treenode[java:removeFeats($obj,@cat0)='(S\NP)/S' and java:removeFeats($obj,*[2][self::Leafnode]/@cat0)='(S\NP)/S' and *[3]/@pos1=':']">

	<Treenode>
		<xsl:apply-templates select="@*|*[1]"/>

		<Leafnode cat="{*[2]/@cat}" lexeme="{*[2]/@lexeme}" pos1="{*[2]/@pos1}" cat0="{*[2]/@cat0}">	
			<xsl:apply-templates select="*[2]/@*"/>
			<complexcat>
				<xsl:apply-templates select="*[2]/*[1]/*"/>
				<slash dir="/" mode="*"/>
				<atomcat type="punct">
    			<fs id="4">
						<feat attr="lex" val=":"/>
					</fs>
  			</atomcat>
			</complexcat>
		</Leafnode>
		<Leafnode cat="{*[3]/@cat}" lexeme="{*[3]/@lexeme}" pos1="PUNCT_COLON" cat0="{*[3]/@cat0}">
			<xsl:apply-templates select="*[3]/@*[not(name()='pos1')]"/>
    	<atomcat type="punct">
    		<fs id="1">
					<feat attr="lex" val=":"/>
				</fs>
  		</atomcat>
		</Leafnode>
	</Treenode>	
</xsl:template>

<!--Cat 3-->
<xsl:template match="Treenode[@cat0='S[dcl]/S[dcl]' and *[2]/@cat0='S[dcl]/S[dcl]' and *[3][@lexeme=':' and @cat0=':']]">

	<Treenode>
	<xsl:apply-templates select="@*|*[1]|*[2]"/>
	<Leafnode cat="{*[3]/@cat}" lexeme=":" pos1="PUNCT_COLON_SAY" cat0=":">
		<xsl:apply-templates select="*[3]/@*[not(name()='pos1')]"/>
		<complexcat>
			<atomcat type="s">
  	  	<fs inheritsFrom="2">
					<feat attr="form" val="dcl"/>
      	</fs>
    	</atomcat>
    	<slash dir="/" mode="&gt;"/>
    	<atomcat type="s">
    		<fs id="2">
					<feat attr="form" val="dcl"/>
      	</fs>
    	</atomcat>			
			<slash dir="\" mode="*"/>
			<complexcat>
				<atomcat type="s">
  	  		<fs id="1">
						<feat attr="form" val="dcl"/>
      		</fs>
    		</atomcat>
    		<slash dir="/" mode="&gt;"/>
    		<atomcat type="s">
    			<fs id="2">
						<feat attr="form" val="dcl"/>		
      		</fs>
    		</atomcat>			
			</complexcat>
		</complexcat>
	</Leafnode>
	</Treenode>
</xsl:template>

<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>
