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
<!--Extraposed appositives-->

<!--As per CCGbank Pg 52, retain binary rule analysis as this rule is correct-->

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<!--[, np ==>s\s]-->
<!--[np , ==>s/s]-->
<!--[, np ==>s\np\(s\np)]-->

<xsl:template match="Leafnode[parent::Treenode[(@cat0='(S\NP)\(S\NP)' or @cat0='S\S' or @cat0='S/S') and ((*[2]/@pos1=',' and *[3]/@cat0='NP') or (*[3]/@pos1=',' and *[2]/@cat0='NP'))]]">

 <Leafnode pos1="PUNCT_EXTR-APPOS">
    <xsl:apply-templates select="@*[not(name()='pos1')]"/>
		<complexcat>
			<xsl:apply-templates select="../*[1]/*" mode="res"/>
			<xsl:choose>
				<xsl:when test="@term_no=../*[3]/@term_no">
				 <slash dir="\" mode="*"/>
				</xsl:when>
				<xsl:otherwise>
				 <slash dir="/" mode="*"/>
				</xsl:otherwise>
			</xsl:choose>
			<atomcat type="np">
      	<fs id="3"/>
       </atomcat>
		</complexcat>
 </Leafnode>
</xsl:template>

<xsl:template match="atomcat[@type='s']" mode="res">

	<xsl:variable name="id" select="java:getPOS($obj)"/>		

	<atomcat type="s">
		<xsl:choose>
			<xsl:when test="string-length($id) &gt; 0">
				<fs id="1"/>
				<xsl:variable name="void" select="java:initPOS($obj)"/>		
			</xsl:when>
			<xsl:otherwise>
				<fs inheritsFrom="1"/>
				<xsl:variable name="void" select="java:storePOS($obj,'1')"/>		
			</xsl:otherwise>
		</xsl:choose>
	</atomcat>
	
</xsl:template>
<xsl:template match="atomcat[@type='np']" mode="res">
	<atomcat type="np">
		<fs id="2"/>
	</atomcat>
</xsl:template>

<!-- Spl copy rule 1-->
  <xsl:template match="@*|node()" mode="res">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="res"/>
    </xsl:copy>
  </xsl:template>


<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>