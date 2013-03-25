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
<!--Transform which detects & ensures balancing of appositive place nps-->

<xsl:template match="/">
 <xsl:apply-templates/>
</xsl:template>

<!--Mark balancing comma of place name appositive-->
<xsl:template match="Leafnode[@pos1=',' and preceding::Leafnode[2][starts-with(@pos1,'PUNCT_APPOS_PLACE')]]">

	<Leafnode cat="{@cat}" lexeme="{@lexeme}" pos1="PUNCT_APPOS_PLACE_BAL" cat0="{@cat0}">
		<xsl:apply-templates select="@*[not(name()='pos1')]"/>
  	<atomcat type=",">
    	<fs id="1"/>
    </atomcat>
  </Leafnode>
</xsl:template>

<!--Default global copy rule-->
 <xsl:template match="@*|node()">
   <xsl:copy>
     <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
 </xsl:template>

</xsl:transform>
