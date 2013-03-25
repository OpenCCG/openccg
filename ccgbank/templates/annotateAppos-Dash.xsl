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

<!--Delete existing appositive commas-->
<xsl:template match="Treenode[*[3]/@lexeme='--' and *[2]/*[2]/@pos1='PUNCT_ELAB_DASH']">
		<xsl:apply-templates select="*[2]"/>
</xsl:template>

<xsl:template match="Treenode[@cat0='NP\NP' and *[2]/@pos1='PUNCT_ELAB_DASH']">

	<Treenode cat="np\np" head="0" dtr="2" cat0="NP\NP">
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
		<xsl:call-template name="tree"/>
	</Treenode>	
	
</xsl:template>

<xsl:template name="tree">

	<xsl:choose>
		<xsl:when test="parent::*/*[3]/@lexeme='--'">
			<Treenode cat="np\np/*punct[,]" head="0" dtr="2">
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
					<slash dir="/" mode="*"/>
        	<atomcat type="punct">
          	<fs id="2">
            	<feat attr="lex" val="--"/>
          	</fs>
        	</atomcat>
      	</complexcat>
				<xsl:call-template name="leaf"/>
			</Treenode>	
			<Leafnode cat="punct[--]_1" lexeme="--" pos=":" pos1="PUNCT_ELAB_DASH_BAL" cat0=",">
    		<atomcat type="punct">
      		<fs id="1">
        		<feat attr="lex" val="--"/>
        	</fs>
      	</atomcat>
    	</Leafnode>
		</xsl:when>
		<xsl:otherwise>
				<xsl:call-template name="leaf"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="leaf">
	
	<Leafnode cat="" lexeme="--" pos=":" pos1="PUNCT_ELAB_DASH" cat0=",">
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
			<xsl:if test="parent::*/*[3]/@lexeme='--'">
      	<slash dir="/" mode="*"/>
	    	<atomcat type="punct">
  	  		<fs id="2">
    				<feat attr="lex" val="--"/>
					</fs>
      	</atomcat>
			</xsl:if>
			<slash dir="/" mode="*"/>
			<complexcat>
    		<atomcat type="np">
					<fs inheritsFrom="1"/>
      	</atomcat>
      	<slash dir="\" mode="&lt;"/>
      	<atomcat type="np">
      		<fs id="1">
					</fs>
      	</atomcat>
			</complexcat>
		</complexcat>
	</Leafnode>
	<xsl:apply-templates select="*[3]"/>
</xsl:template>

<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>




