<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.3 $, $Date: 2004/10/05 11:01:47 $ 

NB: These namespace declarations seem to work with the version of Xalan 
    that comes with JDK 1.4.  With newer versions of Xalan, 
    different namespace declarations may be required. 
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan xalan2">

  
  <!-- ***** Extend ***** -->
  <xsl:template name="extend">
    <xsl:param name="elt"/>
    <xsl:param name="ext"/>
    <xsl:element name="{name($elt)}">
      <xsl:copy-of select="$elt/@*"/>
      <xsl:copy-of select="$elt/node()"/>
      <xsl:copy-of select="$ext"/>
    </xsl:element>
  </xsl:template>

  
  <!-- ***** Insert ***** -->
  <xsl:template name="insert-after">
    <xsl:param name="elt"/>
    <xsl:param name="ins"/>
    <xsl:param name="pos"/>
    <xsl:element name="{name($elt)}">
      <xsl:copy-of select="$elt/@*"/>
      <xsl:copy-of select="$elt/*[position() &lt;= $pos]"/>
      <xsl:copy-of select="$ins"/>
      <xsl:copy-of select="$elt/*[position() &gt; $pos]"/>
    </xsl:element>
  </xsl:template>

  <xsl:template name="insert-after-last-leftward-arg">
    <xsl:param name="elt"/>
    <xsl:param name="ins"/>
    <xsl:variable name="num-nodes-before-backslash" 
      select="count($elt/*[following-sibling::slash[@dir='\']])"/>
    <xsl:variable name="pos">
      <xsl:choose>
        <xsl:when test="$num-nodes-before-backslash = 0">
          <xsl:value-of select="0"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$num-nodes-before-backslash + 2"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:call-template name="insert-after">
      <xsl:with-param name="elt" select="$elt"/>
      <xsl:with-param name="ins" select="$ins"/>
      <xsl:with-param name="pos" select="$pos"/>
    </xsl:call-template>
  </xsl:template>

  
  <!-- ***** Modification modes ***** -->
  <!-- 
  <xsl:template match="atomcat[@type='s']/fs" mode="make-s-phr">
    <xsl:copy>
      <xsl:apply-templates select="@*" mode="make-s-phr"/>
      <feat attr="info" val="phr"/>
      <xsl:apply-templates select="node()[@attr != 'info']" mode="make-s-phr"/>
    </xsl:copy>
  </xsl:template>
  -->
  
  <!-- ***** Copy ***** -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- 
  <xsl:template match="@*|node()" mode="make-s-phr">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="make-s-phr"/>
    </xsl:copy>
  </xsl:template>
  -->

</xsl:transform>


