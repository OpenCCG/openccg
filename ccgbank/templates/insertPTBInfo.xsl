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

<!--Transform to insert PTB info (SBJ, FN_TAG & TPC annotation) into the openccgbank-->

<xsl:template match="/">
  <xsl:apply-templates select="@*|node()"/>
</xsl:template>

<!--Confer PTB annotations (SBJ, FNTAG & TPC) onto Leafnodes-->
<xsl:template match="Leafnode">
	<xsl:variable name="sentId" select="ancestor-or-self ::*[@Header][1]/@Header"/>
	<xsl:variable name="sbj" select="java:getPTBInfo($obj,'SBJ',$sentId,@lexeme,@term_no)"/>
	<xsl:variable name="fntag" select="java:getPTBInfo($obj,'FNT',$sentId,@lexeme,@term_no)"/>
	<xsl:variable name="tpc" select="java:getPTBInfo($obj,'TPC',$sentId,@lexeme,@term_no)"/>
  <Leafnode>
		<xsl:if test="string-length($sbj) &gt; 0">
			<xsl:attribute name="sbj"><xsl:value-of select="$sbj"/></xsl:attribute>
		</xsl:if>
		<xsl:if test="string-length($fntag) &gt; 0">
			<xsl:attribute name="fntag"><xsl:value-of select="$fntag"/></xsl:attribute>
		</xsl:if>
		<xsl:if test="string-length($tpc) &gt; 0">
			<xsl:attribute name="tpc"><xsl:value-of select="$tpc"/></xsl:attribute>
		</xsl:if>
    <xsl:apply-templates select="@*"/>
		<xsl:apply-templates select="node()" mode="sbj"/>
  </Leafnode>
</xsl:template>

<!--Add sbj feature to result cat of relevant verbs-->
<xsl:template match="atomcat[position()=1 and (parent::Leafnode or parent::complexcat/parent::Leafnode)]" mode="sbj">

	<xsl:variable name="sentId" select="ancestor::*[@Header][1]/@Header"/>
	<xsl:variable name="sbj" select="java:getPTBInfo($obj,'SBJ',$sentId,ancestor::Leafnode[1]/@lexeme,ancestor::Leafnode[1]/@term_no)"/>
	<atomcat>
		<xsl:apply-templates select="@*" mode="sbj"/>
		<fs>
			<xsl:apply-templates select="./fs/@*|./fs/node()" mode="sbj"/>
			<xsl:if test="string-length($sbj) &gt; 0">
				<feat val="{$sbj}" attr="sbj"/>
			</xsl:if>
		</fs>
	</atomcat>
</xsl:template>

<!--Global copy rule for mode "sbj"-->

  <xsl:template match="@*|node()" mode="sbj">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="sbj"/>
    </xsl:copy>
  </xsl:template>

<!--Default global copy rule-->

   <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>











