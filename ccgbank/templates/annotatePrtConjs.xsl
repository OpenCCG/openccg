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

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<xsl:variable name="obj-punct" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>

<xsl:variable name="obj-mw" select="java:opennlp.ccgbank.convert.MWHelper.new()"/>

<xsl:template match="Treenode[*[2][self::Leafnode and child::PRT[@dir='\']/@pos1='PUNCT_LEX_CONJ']]">

	<xsl:variable name="punct" select="*[2]/PRT[@dir='\']"/>

	<Treenode head="1" dtr="2">
		<complexcat>	
			<xsl:apply-templates select="*[2]/*[1]/*[position() &lt; last()-1]"/>
		</complexcat>	
	
		<Leafnode>
			<xsl:apply-templates select="$punct/@*[not(name()='dir' or name()='type')]"/>
			<xsl:call-template name="prt-def">
				<xsl:with-param name = "node" select="$punct"/>
			</xsl:call-template>
		</Leafnode>
		<Treenode>
			<xsl:apply-templates select="@*"/>
			<complexcat>
				<xsl:apply-templates select="*[1]/*"/>
				<slash dir="\" mode="*"/>
				<xsl:call-template name="prt-def">
					<xsl:with-param name = "node" select="$punct"/>
				</xsl:call-template>
			</complexcat>
			<xsl:apply-templates select="*[position()>1]"/>
		</Treenode>
	</Treenode>
</xsl:template>

<!--Make a copy of pos for future modification-->
<xsl:template match="Leafnode[@pos1='CONJP_HEADXX' or PRT/@pos1='PUNCT_LEX_CONJ-SUCC' or PRT/@pos1='PUNCT_LEX_CONJ']">

	<xsl:variable name="void" select="java:initSettings($obj-mw)"/>			

	<xsl:for-each select="PRT[@dir='/']">
		<xsl:variable name="void" select="java:storePrt($obj-mw,@term_no,@lexeme)"/>			
	</xsl:for-each>

	<xsl:call-template name="prt-expander">
		<xsl:with-param name = "func" select="."/>
	</xsl:call-template>		
		
	
</xsl:template>

<xsl:template name="prt-expander">
	<xsl:param name="func"/>

	
	<xsl:variable name="prtNo" select="java:getPrt($obj-mw)"/>			
	<xsl:variable name="nextPrt" select="java:peekPrt($obj-mw,number($prtNo)+1)"/>			
	<xsl:choose>
		<xsl:when test="string-length($prtNo)>0">

			<xsl:variable name="prt" select="$func/child::PRT[@term_no=$prtNo]"/>	

			<Treenode head="0" dtr="2" cat0="NewlyAdded">
				<complexcat>
					<xsl:apply-templates select="$func/*[1]/*[position() &lt; last()-1]"/>
					<xsl:for-each select="$func/PRT[@dir='\']">
						<slash dir="\" mode="*"/>
						<xsl:call-template name="prt-def">
							<xsl:with-param name = "node" select="."/>
						</xsl:call-template>
					</xsl:for-each>
					<slash dir="/" mode="*"/>
					<xsl:apply-templates select="$func/*[1]/*[position() &gt; last()-1]"/>
					<xsl:if test="string-length($nextPrt)>0">
						<slash dir="/" mode="*"/>
						<atomcat type="prt">
        			<fs>
								<feat attr="lex" val="{$nextPrt}"/>	
							</fs>
        		</atomcat>
					</xsl:if>
				</complexcat>
				<xsl:call-template name="prt-expander">
					<xsl:with-param name = "func" select="$func"/>
				</xsl:call-template>

				<xsl:if test="$prt/@dir='/'">
					<Leafnode>
						<xsl:apply-templates select="$prt/@*"/>
						<xsl:variable name="void1" select="java:globalInit($obj-punct)"/>		
						<xsl:call-template name="prt-def">
							<xsl:with-param name = "node" select="$prt"/>
						</xsl:call-template>						
					</Leafnode>
				</xsl:if>
			</Treenode>
		</xsl:when>
		<xsl:otherwise>
			<Leafnode>
				<xsl:apply-templates select="$func/@*"/>
				<complexcat>
					<xsl:apply-templates select="$func/*[1]/*[position() &lt; last()-1]"/>

					<xsl:for-each select="$func/child::PRT[@dir='\']">
						<xsl:variable name="void" select="java:storePrt($obj-mw,@term_no,@lexeme)"/>			
					</xsl:for-each>

					<xsl:for-each select="$func/PRT[@dir='\']">

						<xsl:variable name="prtNo" select="java:getPrt($obj-mw)"/>			
						<xsl:variable name="prt-node" select="$func/PRT[@term_no=$prtNo]"/>
						<slash dir="\" mode="*"/>
						<xsl:call-template name="prt-def">
							<xsl:with-param name = "node" select="$prt-node"/>
						</xsl:call-template>						
					</xsl:for-each>

					<xsl:apply-templates select="$func/*[1]/*[position() &gt; last()-2]"/>

					<xsl:for-each select="$func/child::PRT[@dir='/']">
						<xsl:variable name="void" select="java:storePrt($obj-mw,@term_no,@lexeme)"/>			
					</xsl:for-each>
		
					<xsl:variable name="void1" select="java:globalInit($obj-punct)"/>		
					<xsl:variable name="void2" select="java:setglobalId($obj-punct,3)"/>		
			
					<xsl:for-each select="$func/PRT[@dir='/']">

						<xsl:variable name="prtNo" select="java:getPrt($obj-mw)"/>			
						<xsl:variable name="prt-node" select="$func/PRT[@term_no=$prtNo]"/>
						<slash dir="/" mode="*"/>
						<xsl:call-template name="prt-def">
							<xsl:with-param name = "node" select="$prt-node"/>
						</xsl:call-template>						
					</xsl:for-each>
				</complexcat>
				<xsl:apply-templates select="*[self::PRT]"/>        	 
			</Leafnode>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="prt-def">
	<xsl:param name="node"/>

	<xsl:variable name="id" select="java:getglobalId($obj-punct)"/>		

	<atomcat type="{$node/@type}">
  	<fs id="{$id}">
			<feat attr="lex" val="{$node/@lexeme}"/>	
		</fs>
  </atomcat>
</xsl:template>

<!--Default global copy rule-->
<xsl:template match="@*|node()">
   <xsl:copy>
     <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
</xsl:template>

</xsl:transform>











