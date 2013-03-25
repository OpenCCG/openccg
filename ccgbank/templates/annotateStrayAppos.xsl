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
<!--Transform which labels various punct cat part of speech-->

<xsl:template match="/">
  	<xsl:apply-templates/>
</xsl:template>

<!--Cat 1: -->

<xsl:template match="Treenode[*[2]/@pos1='PUNCT_NP_MOD']">

	<Treenode cat="" head="1" dtr="2" cat0="">
		<xsl:copy-of select="@*"/>
		<xsl:apply-templates select="*[1]"/>
		<Treenode cat="np\np/*punct[,]" head="0" dtr="2" cat0=",">
			<complexcat>
				<xsl:apply-templates select="*[1]/*"/>
				<slash dir="/" mode="*"/>
				<atomcat type="punct">
        	<fs id="3">
          	<feat attr="lex" val=","/>
          </fs>
        </atomcat>
			</complexcat>

			<xsl:variable name="arg1" select="*[3]/*[2]/@cat0"/>

			<Leafnode cat="{@cat}" lexeme="," pos1="{$arg1}" pos="," cat0="{@cat0}">
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
					<atomcat type="punct">
        		<fs id="2">
          		<feat attr="lex" val=","/>
          	</fs>
        	</atomcat>
					<slash dir="/" mode="*"/>
					
					<xsl:choose>
						<xsl:when test="$arg1='NP\NP'">
							<complexcat>
								<atomcat type="np">
									<fs inheritsFrom="1"/>
      						<!--<fs inheritsFrom="1">
										<feat attr="mod-punct" val="nil"/>
									</fs>-->
      					</atomcat>
								<slash dir="\" mode="&lt;"/>
    						<atomcat type="np">
      						<fs id="1">
									</fs>
      					</atomcat>
							</complexcat>
						</xsl:when>
						<xsl:when test="$arg1='S[adj]\NP'">
							<complexcat>
	              <atomcat type="s">
                	<fs id="3">
                  	<feat attr="form" val="adj"/>
                  </fs>
                </atomcat>
               	<slash dir="\" mode="&lt;"/>
                <atomcat type="np">
									<fs id="1"/>
								</atomcat>
            	</complexcat>
						</xsl:when>
					</xsl:choose>
    		</complexcat>
			</Leafnode>
			<xsl:apply-templates select="*[3]"/>
		</Treenode>
		<Leafnode cat="punct[,]_1" lexeme="," pos1="PUNCT_COMMA" pos="," cat0=",">
    	<atomcat type="punct">
      	<fs id="1">
					<feat attr="lex" val=","/>
      	</fs>
      </atomcat>
    </Leafnode>
	</Treenode>
</xsl:template>

<xsl:template match="Treenode[*[3]/@pos1='PUNCT_NP_MOD_BAL']">
	<xsl:apply-templates select="*[2]"/>
</xsl:template>

<!--Alloting correct ids to the category of s[dcl]\s[dcl]-->
<xsl:template match="fs" mode="arg1Id">
  <xsl:variable name="id" select="java:getglobalId($obj)"/>
  <fs id="{$id}">
		<xsl:apply-templates mode="arg1Id"/>
		<xsl:if test="$id='1'">
		</xsl:if>
  </fs>
</xsl:template>

<!--arg1 copy rule-->
<xsl:template match="@*|node()" mode="arg1Id">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" mode="arg1Id"/>
  </xsl:copy>
</xsl:template>

<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>
