<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White) 
$Revision: 1.3 $, $Date: 2005/07/18 21:19:41 $ 

This utility transformation converts old Coord lists into simple, flat ones.
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan2">

  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  
  <!-- skip Coord in normal traversal -->
  <xsl:template match="rel[@name='Coord']"/>
  
  <!-- flatten nodes under List rel -->
  <xsl:template match="rel[@name='List']">
    <rel name="List">
      <xsl:call-template name="flatten-list">
        <xsl:with-param name="node" select="node"/>
      </xsl:call-template>
    </rel>
  </xsl:template>
  
  <!-- flatten list -->
  <xsl:template name="flatten-list">
    <xsl:param name="node"/>
    <xsl:apply-templates select="$node"/>
    <xsl:variable name="coord" select="$node/rel[@name='Coord']"/>
    <xsl:if test="$coord">
      <xsl:call-template name="flatten-list">
        <xsl:with-param name="node" select="$coord/node"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  
  <!-- Copy -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>

