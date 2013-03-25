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

  <xsl:output method="xml" indent="yes" xalan2:indent-amount="2"/>
  <xsl:strip-space elements="*"/>
  
  
  <!-- Transform for adding stems to leaf nodes in the derivations -->
  
  <!-- Helper class for looking up stems -->
  <xsl:variable name="obj" select="java:opennlp.ccgbank.convert.MorphLookup.new()"/>

  <!-- Add stems for plural nouns and verbs -->
  <!-- Also add PERF, PROG and PASS as rels -->
  <xsl:template match="Leafnode[@pos='NNS' or starts-with(@pos,'VB')]">
    <xsl:copy>
      <!-- copy attrs -->
      <xsl:apply-templates select="@*"/>
      <!-- add stem -->
      <xsl:variable name="stem" select="java:getStem($obj,@lexeme,@pos)"/>
      <xsl:if test="$stem">
        <xsl:attribute name="stem"><xsl:value-of select="$stem"/></xsl:attribute>
      </xsl:if>
      <!-- warnings 
      <xsl:if test="not($stem)">
        <xsl:message>addStems: no stem for <xsl:value-of select="@lexeme"/>_<xsl:value-of select="@pos"/></xsl:message>
      </xsl:if>-->
      <!-- 
      <xsl:if test="@rel and not(starts-with(@rel,$stem))">
        <xsl:message>addStems: mismatch between rel and stem: <xsl:value-of select="@rel"/>, <xsl:value-of select="$stem"/></xsl:message>
      </xsl:if>
      -->
      <!-- add PERF rel for perfective 'have' -->
      <xsl:if test="$stem='have' and not(@rel) and complexcat/complexcat/atomcat[1][@type='s']/fs/feat[@val='pt']">
        <xsl:attribute name="rel">PERF</xsl:attribute>
      </xsl:if>
      <!-- add PROG rel for progressive 'be' -->
      <xsl:if test="$stem='be' and not(@rel) and complexcat/complexcat/atomcat[1][@type='s']/fs/feat[@val='ng']">
        <xsl:attribute name="rel">PROG</xsl:attribute>
      </xsl:if>
      <!-- add PASS rel for passive 'be' -->
      <xsl:if test="$stem='be' and not(@rel) and complexcat/complexcat/atomcat[1][@type='s']/fs/feat[@val='pss']">
        <xsl:attribute name="rel">PASS</xsl:attribute>
      </xsl:if>
      <!-- copy rest -->
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>

  
  <!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>


