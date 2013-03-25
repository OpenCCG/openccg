<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White) 
$Revision: 1.4 $, $Date: 2005/07/18 21:19:41 $ 

This transformation "treeifies" lists into hierarchical structures 
using Item and Next rels.
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan2">

  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  
  <!-- change List rel to First and Last, treeify items -->
  <xsl:template match="rel[@name='List']">
    <!-- treeify list under First -->
    <rel name='First'>
      <xsl:call-template name="treeify-items"/>
    </rel>
    <!-- add Last rel to alias of last element -->
    <xsl:variable name="last" select="node[last()]"/>
    <xsl:variable name="alias">
      <xsl:call-template name="make-elem-id">
        <xsl:with-param name="item-id" select="$last/@id"/>
        <xsl:with-param name="ext" select="'_el_EqL'"/>
      </xsl:call-template>
    </xsl:variable>
    <rel name="Last">
      <node idref="{$alias}"/>
    </rel>
  </xsl:template>
  
  <!-- treeify child nodes -->
  <xsl:template name="treeify-items">
    <xsl:param name="pos" select="1"/>
    <xsl:choose>
      <xsl:when test="$pos &gt;= count(node)">
        <xsl:variable name="last" select="node[last()]"/>
        <xsl:variable name="elt-id">
          <xsl:call-template name="make-elem-id">
            <xsl:with-param name="item-id" select="$last/@id"/>
            <xsl:with-param name="ext" select="'_el'"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="alias">
          <xsl:call-template name="make-elem-id">
            <xsl:with-param name="item-id" select="$last/@id"/>
            <xsl:with-param name="ext" select="'_el_EqL'"/>
          </xsl:call-template>
        </xsl:variable>
        <node id="{$elt-id}" pred="elem">
          <!-- add EqL relation to final list element -->
          <rel name="EqL">
            <node idref="{$alias}"/>
          </rel>
          <rel name="Item">
            <xsl:apply-templates select="$last"/>
          </rel>
        </node>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="item" select="node[$pos]"/>
        <xsl:variable name="elt-id">
          <xsl:call-template name="make-elem-id">
            <xsl:with-param name="item-id" select="$item/@id"/>
            <xsl:with-param name="ext" select="'_el'"/>
          </xsl:call-template>
        </xsl:variable>
        <node id="{$elt-id}" pred="elem">
          <rel name="Item">
            <xsl:apply-templates select="$item"/>
          </rel>
          <rel name="Next">
            <xsl:call-template name="treeify-items">
              <xsl:with-param name="pos" select="$pos+1"/>
            </xsl:call-template>
          </rel>
        </node>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- make elem id -->
  <xsl:template name="make-elem-id">
    <xsl:param name="item-id"/>
    <xsl:param name="ext"/>
    <xsl:variable name="item-id-base">
      <xsl:choose>
        <xsl:when test="contains($item-id, ':')">
          <xsl:value-of select="substring-before($item-id, ':')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$item-id"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="concat($item-id-base, $ext, ':struct')"/>
  </xsl:template>
  
  
  <!-- Copy -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>

