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


<!--Transform which deals with parentheticals , say verbs-->

<!--Java helper class-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>

<xsl:template match="/">
 <xsl:apply-templates/>
</xsl:template>


<!--Treenode after the comma in question-->
<xsl:template match="Treenode[preceding-sibling::Leafnode[1][@pos1='PUNCT_VPAREN3' or @pos1='PUNCT_VPAREN4']]">

	<Treenode>
		<xsl:copy-of select="@*"/>
		<complexcat>
			<xsl:apply-templates select="*[1]/*"/>				
			<slash dir="\" mode="*"/>
			<atomcat type="punct">
  			<fs id="4">
					<feat attr="lex" val=","/>
				</fs>
  		</atomcat>
		</complexcat>				
		<xsl:apply-templates select="*[position()>1]"/>
	</Treenode>			
</xsl:template>

<xsl:template match="Leafnode[preceding::Leafnode[1]/@pos1='PUNCT_VPAREN3']">

	<Leafnode>
		<xsl:copy-of select="@*"/>
    <complexcat>
			<!--<xsl:apply-templates select="*[1]/*[position() &lt; 6]"/>-->
			<atomcat type="s">
      	<fs inheritsFrom="1">
        </fs>
      </atomcat>
      <slash dir="\" mode="&lt;"/>
      <atomcat type="np">
      	<fs id="2"/>
      </atomcat>
      <slash dir="\" mode="&lt;"/>
      <complexcat>
      	<atomcat type="s">
        	<fs id="1">
          </fs>
        </atomcat>
        <slash dir="\" mode="&lt;"/>
        <atomcat type="np">
        	<fs id="2"/>
        </atomcat>
      </complexcat>
			<slash dir="\" mode="*"/>
			<atomcat type="punct">
  			<fs id="4">
					<feat attr="lex" val=","/>
				</fs>
  		</atomcat>
			<xsl:if test="following-sibling::*/following::Leafnode[1]/@pos1='PUNCT_VPAREN3_BAL' or following-sibling::*[following::Treenode[1][following::Leafnode[1]/@pos1='PUNCT_VPAREN3_BAL']]">
				<slash dir="/" mode="*"/>
				<atomcat type="punct">
  				<fs id="5">
						<feat attr="lex" val=","/>
					</fs>
  			</atomcat>
			</xsl:if>
			<slash dir="/" mode="&gt;"/>
  	  <xsl:apply-templates select="*[1]/*[position() &gt; 6]" mode="res"/>
    </complexcat>
  </Leafnode>
</xsl:template>	

<xsl:template match="Leafnode/*[1]/*[position()=last() and self::atomcat]/fs" mode="res">
	<fs>
		<xsl:copy-of select="@*"/>
	</fs>
</xsl:template>	

<xsl:template match="Leafnode/*[1]/*[last() and self::complexcat]/atomcat[1]/fs" mode="res">
	<fs>
		<xsl:copy-of select="@*"/>
	</fs>
</xsl:template>	

<xsl:template match="Leafnode[preceding::Leafnode[1]/@pos1='PUNCT_VPAREN4']">

	<Leafnode>
		<xsl:copy-of select="@*"/>
    <complexcat>
			<!--<xsl:apply-templates select="*[1]/*"/>-->
			<atomcat type="s">
      	<fs inheritsFrom="1">
        </fs>
      </atomcat>
      <slash dir="\" mode="&lt;"/>
      <atomcat type="np">
      	<fs id="2"/>
      </atomcat>
      <slash dir="\" mode="&lt;"/>
      <complexcat>
      	<atomcat type="s">
        	<fs id="1">
          </fs>
        </atomcat>
        <slash dir="\" mode="&lt;"/>
        <atomcat type="np">
        	<fs id="2"/>
        </atomcat>
      </complexcat>
			<slash dir="\" mode="*"/>
			<atomcat type="punct">
  			<fs id="4">
					<feat attr="lex" val=","/>
				</fs>
  		</atomcat>				
			<xsl:if test="following-sibling::Leafnode/@pos1='PUNCT_VPAREN4_BAL'">
				<slash dir="/" mode="*"/>
				<atomcat type="punct">
  				<fs id="5">
						<feat attr="lex" val=","/>
					</fs>
  			</atomcat>
			</xsl:if>				
    </complexcat>
  </Leafnode>
</xsl:template>	

<!--Treenode which is the parent of the balancing comma in question-->
<xsl:template match="Treenode[following-sibling::Leafnode[1]/@pos1='PUNCT_VPAREN3_BAL']">
	
	<Treenode>
		<xsl:copy-of select="@*"/>
		<complexcat>
			<xsl:apply-templates select="*[1]/*"/>				
			<slash dir="\" mode="*"/>
			<atomcat type="punct">
  			<fs id="4">
					<feat attr="lex" val=","/>
				</fs>
  		</atomcat>
			<slash dir="/" mode="*"/>
			<atomcat type="punct">
  			<fs id="5">
					<feat attr="lex" val=","/>
				</fs>
  		</atomcat>								
		</complexcat>				
		<xsl:apply-templates select="*[position()>1]"/>
	</Treenode>			
</xsl:template>	

<!--Treenode above the comma in question/balancing comma-->
<xsl:template match="Treenode[Leafnode[1][@pos1='PUNCT_SAY5']]">
	<xsl:apply-templates select="*[3]"/>
</xsl:template>	

<xsl:template match="Treenode[preceding-sibling::Leafnode[1]/@pos1='PUNCT_SAY5']">	
	<Treenode>
		<xsl:copy-of select="@*"/>
		<xsl:apply-templates select="*[1]"/>

	<Treenode cat="" head="0" dtr="2" cat0="">
		<complexcat>
			<xsl:apply-templates select="*[1]/*"/>		
			<slash dir="/" mode="*"/>
			<atomcat type="punct">
  			<fs id="4">
					<feat attr="lex" val=","/>
				</fs>
  		</atomcat>				
		</complexcat>
		
		<Leafnode cat="," lexeme="," pos="," pos1="PUNCT_SAY5" cat0=",">
			<xsl:copy-of select="*[2]/@*"/>
			<complexcat>
				<atomcat type="s">
        	<fs inheritsFrom="1">
           </fs>
        </atomcat>
        <slash dir="\" mode="&lt;"/>
        <atomcat type="np">
        	<fs id="2">
           </fs>
        </atomcat>
        <slash dir="\" mode="&lt;"/>
        <complexcat>
        	<atomcat type="s">
          	<fs id="1">
           </fs>
          </atomcat>
          <slash dir="\" mode="&lt;"/>
          <atomcat type="np">
          	<fs id="2">
           	</fs>
          </atomcat>
        </complexcat>
				<slash dir="/" mode="*"/>
				<atomcat type="punct">
  				<fs id="3">
						<feat attr="lex" val=","/>
					</fs>
  			</atomcat>
				<slash dir="/" mode="*"/>
				<xsl:apply-templates select="*[2]/*[1]" mode="arg"/>
			</complexcat>
		</Leafnode>
		<xsl:apply-templates select="*[2]"/>
	</Treenode>
	<Leafnode cat="" lexeme="," pos1="PUNCT_SAY5_BAL" pos="," cat0=",">
  	<atomcat type="punct">
   		<fs id="1">
				<feat attr="lex" val=","/>
			</fs>
  	</atomcat>
 	</Leafnode>
	</Treenode>
</xsl:template>	

<!--Alloting correct ids to the category of s[dcl]\s[dcl]-->
<xsl:template match="atomcat[1]/fs" mode="arg">
	<fs id="4">
	 	<xsl:apply-templates mode="arg"/>
  </fs>
</xsl:template>

<xsl:template match="atomcat[2]/fs" mode="arg">
	<fs id="5">
	 	<xsl:apply-templates mode="arg"/>
  </fs>
</xsl:template>

<!--res copy rule-->
<xsl:template match="@*|node()" mode="res">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" mode="res"/>
  </xsl:copy>
</xsl:template>

<!--arg copy rule-->
<xsl:template match="@*|node()" mode="arg">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" mode="arg"/>
  </xsl:copy>
</xsl:template>

<!--Default global copy rule-->
<xsl:template match="@*|node()">
	<xsl:copy>
 		<xsl:apply-templates select="@*|node()"/>
	</xsl:copy>
</xsl:template>

</xsl:transform>











