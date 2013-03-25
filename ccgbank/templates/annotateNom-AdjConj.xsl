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

<xsl:template match="Treenode[*[2]/@pos1='PUNCT_NOM_CONJ']">

	<Treenode cat="" head="1" dtr="2" cat0="">
		<xsl:apply-templates select="@*"/>
		<complexcat>
			<atomcat type="n">
      	<fs id="1"/>
      </atomcat>
			<slash dir="\" mode="*"/>
			<complexcat>
    		<atomcat type="n">
      		<fs inheritsFrom="2"/>
      	</atomcat>
      	<slash dir="/" mode="&gt;"/>
      	<atomcat type="n">
      		<fs id="2"/>
      	</atomcat>
			</complexcat>
		</complexcat>

		<Leafnode cat="{@cat}" lexeme="," pos="," pos1="PUNCT_NOM_CONJ" cat0="{@cat0}">
			<xsl:apply-templates select="*[2]/@*[not(name()='pos1')]"/>

			<complexcat>
				<atomcat type="n">
      		<fs id="1"/>
      	</atomcat>
				<slash dir="\" mode="*"/>
				<complexcat>
    			<atomcat type="n">
      			<fs id="2"/>
      		</atomcat>
      		<slash dir="/" mode="&gt;"/>
      		<atomcat type="n">
      			<fs id="2"/>
      		</atomcat>
				</complexcat>
				<slash dir="/" mode="*"/>
				<atomcat type="n">
      		<fs id="3"/>
      	</atomcat>
    	</complexcat>
		</Leafnode>
		<xsl:apply-templates select="*[3]"/>
	</Treenode>

</xsl:template>

<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>
