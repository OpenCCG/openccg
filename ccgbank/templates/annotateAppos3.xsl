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

<!--Transform which corrects the structure of , np\np marked appositives to , x ==> np\np-->

<xsl:template match="/">
  	<xsl:apply-templates/>
</xsl:template>


<!--Cat1-->

<!--Delete existing appositive commas-->
<xsl:template match="Treenode[@cat0='NP' and *[3]/@pos1='PUNCT_APPOS_VRB']">
	<xsl:apply-templates select="*[2]"/>
</xsl:template>

<xsl:template match="Treenode[@cat0='NP\NP' and preceding-sibling::Treenode/*[3]/@pos1='PUNCT_APPOS_VRB']">

	<Treenode cat="np\np" head="0" dtr="2" cat0="NP\NP">
		<xsl:apply-templates select="@*|*[1]"/>
		<xsl:variable name="vrb-feat" select="*[2]/*[1]/atomcat[1]/fs/feat/@val"/>
		<Leafnode cat="" lexeme="," pos1="PUNCT_APPOS_VRB" cat0=",">
			<xsl:apply-templates select="preceding-sibling::Treenode/*[3]/@*"/>
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
				<complexcat>
       		<atomcat type="s">
        		<fs id="4">
          		<feat attr="form" val="{$vrb-feat}"/>
          	</fs>
        	</atomcat>
        	<slash dir="\" mode="&lt;"/>
        	<atomcat type="np">
						<fs id="1">
						</fs>
        	</atomcat>
     	 </complexcat>
			</complexcat>
		</Leafnode>
		<xsl:apply-templates select="*[2]"/>
	</Treenode>	
</xsl:template>


<!--Cat 2-->

<!--Delete existing appositive commas-->
<xsl:template match="Treenode[@cat0='NP' and *[3]/@pos1='PUNCT_APPOS_WH']">
	<xsl:apply-templates select="*[2]"/>
</xsl:template>

<xsl:template match="Treenode[@cat0='NP\NP' and name(*[2])='Leafnode' and preceding::Leafnode[1]/@pos1='PUNCT_APPOS_WH']">

	<Treenode cat="np\np" head="0" dtr="2" cat0="NP\NP">
		<xsl:apply-templates select="*[1]/@*|*[1]"/>
		<Leafnode cat="punct[,]_1" lexeme="," pos1="PUNCT_APPOS_WH" cat0=",">
			<xsl:apply-templates select="preceding::Leafnode[1]/@*"/>	
    	<atomcat type="punct">
      	<fs id="1">
       		<feat attr="lex" val=","/>
       	</fs>
      </atomcat>
    </Leafnode>	
		<Treenode cat="" head="0" dtr="2" cat0="NP\NP">
			<xsl:apply-templates select="*[1]/@*"/>
			<complexcat>
    		<xsl:apply-templates select="*[1]/*"/>
				<slash dir="\" mode="*"/>
				<atomcat type="punct">
  	    	<fs id="11">
    	  		<feat attr="lex" val=","/>
					</fs>
				</atomcat>
			</complexcat>		

			<Leafnode cat="" lexeme="{*[2]/@lexeme}" pos1="{*[2]/@pos1}" cat0="{*[2]/@cat0}">
				<xsl:apply-templates select="*[2]/@*"/>
  			<complexcat>
					<xsl:if test="name(*[2]/*[1])='complexcat'">
						<xsl:apply-templates select="*[2]/complexcat/*[position() &lt; last()-1]"/>			
					</xsl:if>
					<xsl:if test="name(*[2]/*[1])='atomcat'">
						<xsl:apply-templates select="*[2]/*[1]"/>			
					</xsl:if>
					<slash dir="\" mode="*"/>
					<atomcat type="punct">
  	    		<fs id="11">
    	  			<feat attr="lex" val=","/>
						</fs>
      		</atomcat>
					<slash dir="/" mode="*"/>
					<xsl:apply-templates select="*[2]/complexcat/*[position()=last()]"/>		
				</complexcat>
			</Leafnode>
			<xsl:apply-templates select="*[3]"/>
		</Treenode>		
	</Treenode>	
</xsl:template>

<!--Cat 3-->

<!--Delete existing appositive commas-->
<xsl:template match="Treenode[@cat0='NP' and *[3]/@pos1='PUNCT_APPOS_MISC']">
	<xsl:apply-templates select="*[2]"/>
</xsl:template>

<xsl:template match="Treenode[@cat0='NP\NP' and preceding-sibling::Treenode[1]/*[3]/@pos1='PUNCT_APPOS_MISC']">

	<Treenode cat="np\np" head="0" dtr="2" cat0="NP\NP">
		<xsl:apply-templates select="@*|*[1]"/>

		<xsl:variable name="feat" select="*[2]/*[1]/atomcat[1]/fs/feat/@val"/>
		<Leafnode cat="" lexeme="," pos1="PUNCT_APPOS_MISC" cat0=",">
			<xsl:apply-templates select="preceding-sibling::Treenode[1]/*[3]/@*"/>
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
				</complexcat>
			</complexcat>
		</Leafnode>
		<Treenode>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</Treenode>
	</Treenode>	

</xsl:template>

<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>




