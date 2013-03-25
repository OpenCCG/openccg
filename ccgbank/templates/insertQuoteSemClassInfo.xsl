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

<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.InfoHelper.new()"/>

<!--Transform to insert BBN tag info into OpenCCGbank-->

<xsl:template match="/">
  <xsl:apply-templates select="@*|node()"/>
</xsl:template>

<!--Retrieve semantic classes of Treenodes dominating bbn tagged phrases-->
<xsl:template match="Treenode">

	<xsl:variable name="header" select="ancestor-or-self::*[@Header]/@Header"/>

	<xsl:variable name="desc" select="descendant::Leafnode"/>

	<!--Get words in phrase to check with BBN annotation stored in the bkgrnd class-->
	<xsl:for-each select="$desc">
		<xsl:variable name="void" select="java:collapse($obj,@lexeme0,'1')"/>
	</xsl:for-each>
	<xsl:variable name="words" select="java:collapse($obj,'','2')"/>

	<xsl:variable name="leaf1" select="$desc[position()=1]/@term_no"/>
	<xsl:variable name="leaf2" select="$desc[position()=last()]/@term_no"/>
	<xsl:variable name="span"> 
		<xsl:value-of select="concat(concat($leaf1,','),$leaf2)"/>
	</xsl:variable> 

	<xsl:variable name="bbn-info" select="java:getBBNInfo($obj,$header,$span,$words)"/>
	<xsl:variable name="quote-info" select="java:getQuoteInfo($obj,$header,number($leaf1),number($leaf2),$words)"/>

	<Treenode>
		 
		<!-- Confer NEs info on treenodes -->
    <xsl:choose>
			<!-- Alloting bbn-info -->
			 <xsl:when test="string-length($bbn-info)>0">
				 <xsl:attribute name="bbn-info"><xsl:value-of select="$bbn-info"/></xsl:attribute>
       </xsl:when>
			 <!-- Alloting ne-info for shared task -->
       <xsl:otherwise>
				 <xsl:variable name="pless_ind1" select="$desc[position()=1]/@pless_ind"/>
				 <xsl:variable name="pless_ind2" select="$desc[position()=last()]/@pless_ind"/>
				 <xsl:variable name="pless_span"> 
					 <xsl:value-of select="concat(concat($pless_ind1,','),$pless_ind2)"/>
				 </xsl:variable> 
				 <xsl:variable name="ne-info" select="java:getBBNInfo($obj,$header,$pless_span,$words)"/>
         <xsl:if test="string-length($ne-info)>0">
					 <xsl:attribute name="bbn-info"><xsl:value-of select="$ne-info"/></xsl:attribute>
				 </xsl:if>
       </xsl:otherwise>
    </xsl:choose>

		<xsl:if test="string-length($quote-info)>0 and not(@Header)">
			<xsl:attribute name="quote-info"><xsl:value-of select="$quote-info"/></xsl:attribute>
    </xsl:if>   
		<xsl:apply-templates select="@*|node()"/>
	</Treenode>

</xsl:template>

<!--Retrieve semantic classes of Leafnodes from java bkgrnd class-->
<xsl:template match="Leafnode">
  <Leafnode>

		<xsl:variable name="span"> 
			<xsl:value-of select="concat(concat(@term_no,','),@term_no)"/>
		</xsl:variable> 
		<xsl:variable name="header" select="ancestor-or-self::*[@Header]/@Header"/>
		<xsl:variable name="sem-class" select="java:getBBNClass($obj,$header,@lexeme0,@pos,@cat,number(@term_no))"/>
		<xsl:variable name="quote-info" select="java:getQuoteInfo($obj,$header,number(@term_no),number(@term_no),@lexeme)"/>
		
		<xsl:choose>
			<!-- Alloting bbn-info -->
			<xsl:when test="string-length($sem-class)>0 and not(contains(@pos1,'PUNCT'))">			<xsl:attribute name="class"><xsl:value-of select="$sem-class"/></xsl:attribute>
			</xsl:when>
			<xsl:when test="starts-with(@pos,'NNP')"><xsl:attribute name="class">NAME</xsl:attribute>
			</xsl:when>
			<!-- Alloting ne-info for shared task -->
			<xsl:otherwise>
				<xsl:variable name="ne-info" select="java:getBBNClass($obj,$header,@lexeme0,@pos,@cat,number(@pless_ind))"/>
				<xsl:if test="string-length($ne-info)>0">
					<xsl:attribute name="ne-info"><xsl:value-of select="$ne-info"/></xsl:attribute>
				</xsl:if>
			</xsl:otherwise>
			
		</xsl:choose>
		<xsl:if test="string-length($quote-info)>0">      
        <xsl:attribute name="quote-info"><xsl:value-of select="$quote-info"/></xsl:attribute>
    </xsl:if>
    <xsl:apply-templates select="@*|node()"/>
  </Leafnode>
</xsl:template>


<!--Default global copy rule-->

   <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>











