<?xml version="1.0"?>
<!-- 
Copyright (C) 20011 Michael White
$Revision: 1.1 $, $Date: 2011/08/12 03:14:03 $ 

This transformation takes openccg nbest output and transforms it into 
the required output format for the Generation Challenges 2011 shared task.
-->
<xsl:transform
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xalan xalan2 java">

  <xsl:output method="xml" indent="yes" xalan2:indent-amount="2"/>
  <xsl:strip-space elements="*"/>

  <!-- Helper class for adjusting roles -->
  <xsl:variable name="obj" select="java:opennlp.ccgbank.convert.GenChal11Adjuster.new()"/>

  
  <!-- start -->
  <xsl:template match="/nbest">
    <tstset trglang="en" setid="genchal2011.sr.deep" sysid="OSU">
      <xsl:apply-templates/>
    </tstset>
  </xsl:template>

  <!-- seg: adjust id, grab top 5 outputs -->
  <xsl:template match="seg">
    <seg id="{substring-after(@id,'=')}">
      <xsl:apply-templates select="best|next[position() &lt;= 4]"/>
    </seg>
  </xsl:template>

  <!-- best|next: adjust text string with java call -->
  <xsl:template match="best|next">
    <xsl:copy>
      <xsl:value-of select="java:getAdjustedString($obj,text())"/>
    </xsl:copy>
  </xsl:template>


  <!-- Copy -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>

