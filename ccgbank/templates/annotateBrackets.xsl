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
<!--Paren rules for brackets around sentences & np mods-->

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<!--Treenode above the sent-paren in question-->
<xsl:template match="Treenode[*[2]/@pos1='PUNCT_LPAREN1']">

	<!--Choice between lcb and -lrb-->
	<xsl:variable name="lexVal" select="concat('-r',substring(*[2]/@lexeme,3,5))"/>
	<Treenode>
		<xsl:apply-templates select="@*|*[1]"/>		
		<Treenode cat="" head="0" dtr="2" cat0="NewlyAdded">
			<xsl:apply-templates select="@*[not(name()='Header')]"/>		
			<complexcat>
				<xsl:apply-templates select="*[1]"/>
				<slash dir="/" mode="*"/>
				<atomcat type="punct">
      		<fs>
        		<feat attr="lex" val="{$lexVal}"/>
       		</fs>
       	</atomcat>
			</complexcat>
			<Leafnode>
				<xsl:copy-of select="*[2]/@*"/>
				<complexcat>
					<atomcat type="sent">
            <fs inheritsFrom="1"/>
          </atomcat>					
					<slash dir="/" mode="*"/>
					<atomcat type="punct">
          	<fs id="2">
            	 <feat attr="lex" val="{$lexVal}"/>
           	</fs>
        	</atomcat>
					<slash dir="/" mode="*"/>
					<atomcat type="sent">
            <fs id="1"/>
          </atomcat>				
      	</complexcat>
			</Leafnode>
			<xsl:apply-templates select="*[3]"/>		
		</Treenode>
		<Leafnode cat="punct" lexeme="{$lexVal}" pos="RRB" pos1="PUNCT_RPAREN" cat0="RRB">		
    	<atomcat type="punct">
      	<fs id="1">
        	<feat attr="lex" val="{$lexVal}"/>
        </fs>
      </atomcat>
    </Leafnode>
	</Treenode>
</xsl:template>
	
<!--Treenode above the np-mod paren in question-->
<xsl:template match="Treenode[*[2]/@pos1='PUNCT_LPAREN2']">
	
	<!--Choice between lcb and -lrb-->
	<xsl:variable name="lexVal" select="concat('-r',substring(*[2]/@lexeme,3,5))"/>
	<Treenode>
		<xsl:copy-of select="@*"/>
		<xsl:apply-templates select="*[1]"/>		
		<Treenode cat="" head="0" dtr="2" cat0="NewlyAdded">
			<xsl:copy-of select="@*"/>
			<complexcat>
				<xsl:apply-templates select="*[1]/*"/>
				<slash dir="/" mode="*"/>
				<atomcat type="punct">
      		<fs>
        		<feat attr="lex" val="{$lexVal}"/>
       		</fs>
       	</atomcat>
			</complexcat>

			<Leafnode>
				<xsl:copy-of select="*[2]/@*"/>
				<complexcat>
					<xsl:call-template name="args">
          	<xsl:with-param name="case">res</xsl:with-param>
          </xsl:call-template>
					<slash dir="/" mode="*"/>
					<atomcat type="punct">
          	<fs id="2">
            	 <feat attr="lex" val="{$lexVal}"/>
           	</fs>
        	</atomcat>
					<slash dir="/" mode="*"/>
					<xsl:call-template name="args">
          	<xsl:with-param name="case">arg</xsl:with-param>
          </xsl:call-template>
      	</complexcat>
			</Leafnode>
			<xsl:apply-templates select="*[3]"/>		
		</Treenode>
		<Leafnode cat="punct" lexeme="{$lexVal}" pos="RRB" pos1="PUNCT_RPAREN" cat0="RRB">
    	<atomcat type="punct">
      	<fs id="1">
        	<feat attr="lex" val="{$lexVal}"/>
        </fs>
      </atomcat>
    </Leafnode>
	</Treenode>
</xsl:template>

<xsl:template name="args">
	<xsl:param name="case"/>
			<xsl:if test="$case='res'">
				<atomcat type="np">
      		<fs inheritsFrom="1">
					</fs>
      	</atomcat>
      	<slash dir="\" mode="&lt;"/>
      	<atomcat type="np">
      		<fs id="1">
					</fs>
      	</atomcat>
			</xsl:if>
			<xsl:if test="$case='arg'">
				<complexcat>
					<atomcat type="np">
      			<fs inheritsFrom="1">
						</fs>
      		</atomcat>
      		<slash dir="\" mode="&lt;"/>
      		<atomcat type="np">
      			<fs id="1"/>
      		</atomcat>
				</complexcat>
			</xsl:if>
</xsl:template>

<!--Delete original balancing parens and full-stop in the wrong place-->
<xsl:template match="Treenode[@cat0='S[dcl]\NP' and *[2]/@cat0='S[dcl]\NP' and *[3]/@pos='RRB']">
	<xsl:apply-templates select="*[2]"/>		
</xsl:template>
<xsl:template match="Treenode[@cat0='NP\NP' and *[2]/@cat0='NP\NP' and *[3]/@pos='RRB']">
	<xsl:apply-templates select="*[2]"/>		
</xsl:template>

<xsl:template match="Treenode[@cat='sent_1' and preceding-sibling::Leafnode/@pos1='PUNCT_LPAREN1' and *[3]/@pos='RRB']">
	<xsl:apply-templates select="*[2]"/>		
</xsl:template>

<!--Default global copy rule-->
<xsl:template match="@*|node()">
	<xsl:copy>
 		<xsl:apply-templates select="@*|node()"/>
	</xsl:copy>
</xsl:template>

</xsl:transform>











