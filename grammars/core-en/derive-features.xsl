<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White) 
$Revision: 1.7 $, $Date: 2005/10/26 17:12:28 $ 

This transformation propagates the info and owner features downwards  
and adds a default no k-contrast feature.
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan2">

  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  
  <!-- add derived features to nodes (but not node references) --> 
  <xsl:template match="node[not(@idref)]">
    <xsl:copy>
    
      <!-- copy attrs -->
      <xsl:apply-templates select="@*"/>
      
      <!-- inherit nearest @info, or default to rheme, if none -->
      <xsl:if test="not(@info)">
        <xsl:variable name="nearest-info" select="ancestor::node[@info][1]/@info"/>
        <xsl:choose>
          <!-- inherit nearest @info -->
          <xsl:when test="$nearest-info">
            <xsl:attribute name="info"><xsl:value-of select="$nearest-info"/></xsl:attribute>
          </xsl:when>
          <!-- otherwise info=rh -->
          <xsl:otherwise>
            <xsl:attribute name="info">rh</xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <!-- inherit nearest @owner, or set by info/mood, if none -->
      <xsl:if test="not(@owner)">
        <xsl:variable name="nearest-owner" select="ancestor-or-self::node[@owner][1]/@owner"/>
        <xsl:variable name="nearest-other-node" select="ancestor-or-self::node[@info or @mood][1]"/>
        <xsl:choose>
          <!-- inherit nearest @owner, if any -->
          <xsl:when test="$nearest-owner">
            <xsl:attribute name="owner"><xsl:value-of select="$nearest-owner"/></xsl:attribute>
          </xsl:when>
          <!-- use owner=h if nearest info=th or mood=int -->
          <xsl:when test="$nearest-other-node/@info='th' or $nearest-other-node/@mood='int'">
            <xsl:attribute name="owner">h</xsl:attribute>
          </xsl:when>
          <!-- otherwise owner=s -->
          <xsl:otherwise>
            <xsl:attribute name="owner">s</xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <!-- add no k-contrast feature, if none on node or one-of/atts -->
      <xsl:if test="not(@kon) and not(one-of/atts[@kon])">
        <xsl:attribute name="kon">-</xsl:attribute>
      </xsl:if>
      
      <!-- copy rest -->      
      <xsl:apply-templates select="node()"/>
      
    </xsl:copy>
  </xsl:template>
  
  
  <!-- Copy -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>

