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

<!--Mark place names 1-->
<xsl:template match="Treenode[Leafnode[@pos1='PUNCT_APPOS_PLACE2' or @pos1='PUNCT_APPOS_PLACE_BAL']]">
	<xsl:apply-templates select="*[position() >1 and not(@pos1='PUNCT_APPOS_PLACE2') and not(@pos1='PUNCT_APPOS_PLACE_BAL')]"/>

</xsl:template>

<!--Cat 1: First set of place names-->

<xsl:template match="Treenode[*[2]/@pos1='PUNCT_APPOS_PLACE1']">
	<Treenode cat="np[mod]\np[mod]" head="0" dtr="2" cat0="NP\NP">
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
		<xsl:call-template name="tree0"/>	
	</Treenode>
</xsl:template>

<!--Cat 2: Second set of place names-->

<xsl:template match="Leafnode[preceding::Leafnode[1]/@pos1='PUNCT_APPOS_PLACE2']">
	<Treenode cat="np\np" head="0" dtr="2" cat0="NP\NP">
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
		<xsl:call-template name="tree0"/>
	</Treenode>
</xsl:template>

<xsl:template name="tree0">

	<xsl:choose>
		<xsl:when test="following::Leafnode[1]/@pos1='PUNCT_APPOS_PLACE_BAL'">
			<Treenode cat="np[mod]\np[mod]/*punct[,]" head="0" dtr="2" cat0="NP\NP\punct[,]">
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
					<slash dir="/" mode="*"/>
        	<atomcat type="punct">
          	<fs id="2">
            	<feat attr="lex" val=","/>
          	</fs>
        	</atomcat>
      	</complexcat>
				<xsl:call-template name="leaf0"/>
			</Treenode>	
			<Leafnode cat="punct[,]_1" lexeme="," pos="," pos1="PUNCT_APPOS_PLACE_BAL" cat0=",">
    		<atomcat type="punct">
      		<fs id="1">
        		<feat attr="lex" val=","/>
        	</fs>
      	</atomcat>
    	</Leafnode>
		</xsl:when>
		<xsl:otherwise>
				<xsl:call-template name="leaf0"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="leaf0">

	<Leafnode cat="np[mod]\np[mod]/*np" lexeme="," pos="," pos1="PUNCT_APPOS_PLACE" cat0=",">
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
			<xsl:if test="following::Leafnode[1]/@pos1='PUNCT_APPOS_PLACE_BAL'">
				<atomcat type="punct">
         	<fs id="2">
           	<feat attr="lex" val=","/>
         	</fs>
        </atomcat>
				<slash dir="/" mode="*"/>
			</xsl:if>
			<atomcat type="np">
	  		<fs id="3">
      	</fs>
    	</atomcat>			
		</complexcat>
	</Leafnode>	
	<xsl:choose>
		<xsl:when test="preceding::Leafnode[1]/@pos1='PUNCT_APPOS_PLACE2'">
			<Leafnode>
				<xsl:copy-of select="@*"/>
				<xsl:call-template name="modPlnp"/>
			</Leafnode>		
		</xsl:when>
		<xsl:when test="*[2]/@pos1='PUNCT_APPOS_PLACE1'">
			<Leafnode>
				<xsl:copy-of select="*[3]/@*"/>
				<xsl:call-template name="modPlnp"/>
			</Leafnode>		
		</xsl:when>
	</xsl:choose>
</xsl:template>

<xsl:template name="modPlnp">

	<atomcat type="np">
		<fs id="1">
    </fs>
  </atomcat>
</xsl:template>

<!--Cat  3-->
<xsl:template match="Treenode[@cat0='N/N' and *[2]/@pos1=',' and *[3][@cat0='N/N' and *[3]/@pos1=',']]">
	
	<Treenode cat="n[nil]/n[nil]" head="{@head}" dtr="{@dtr}" cat0="{@cat0}">
		<xsl:apply-templates select="@*"/>
		<complexcat>
     	<atomcat type="n">
       	<fs id="1">
				</fs>
       </atomcat>
       <slash dir="/" mode="&gt;"/>
       <atomcat type="n">
        <fs id="1">
				</fs>
       </atomcat>
		</complexcat>	

		<Treenode cat="n/n/*punct[,]" head="0" dtr="2">
			<xsl:apply-templates select="@*"/>
     	<complexcat>
       	<atomcat type="n">
         	<fs id="1">
					</fs>
       	</atomcat>
       	<slash dir="/" mode="&gt;"/>
       	<atomcat type="n">
         	<fs id="1">
					</fs>
       	</atomcat>
				<slash dir="/" mode="*"/>
       	<atomcat type="punct">
         	<fs id="2">
           	<feat attr="lex" val=","/>
         	</fs>
       	</atomcat>
     	</complexcat>
			<Leafnode cat=",_1" lexeme="," pos="," pos1="PUNCT_APPOS_ADDR" cat0=",">
				<complexcat>
      		<atomcat type="n">
       			<fs inheritsFrom="1">
						</fs>
       		</atomcat>
       		<slash dir="/" mode="&gt;"/>
       		<atomcat type="n">
       			<fs id="1">
						</fs>
       		</atomcat>
					<slash dir="/" mode="*"/>
	    		<atomcat type="punct">
  	  			<fs id="2">
    					<feat attr="lex" val=","/>
						</fs>
      		</atomcat>
					<slash dir="/" mode="*"/>
					<complexcat>
						<atomcat type="n">
       				<fs inheritsFrom="1">
							</fs>
       			</atomcat>
       			<slash dir="/" mode="&gt;"/>
       			<atomcat type="n">
       				<fs id="1">	
							</fs>
       			</atomcat>
					</complexcat>	
      	</complexcat>
			</Leafnode>	
			<xsl:apply-templates select="*[3]/*[2]"/>
		</Treenode>
		<Leafnode cat="punct[,]_1" lexeme="," pos="," pos1="PUNCT_APPOS_ADDR_BAL" cat0=",">
    	<atomcat type="punct">
      	<fs id="1">
       		<feat attr="lex" val=","/>
       	</fs>
     	</atomcat>
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




