<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-5 University of Edinburgh (Michael White)
$Revision: 1.1 $, $Date: 2009/11/09 19:45:41 $ 

This transformation converts a hybrid logic dependency semantics representation
to a node-rel graph.
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan2">

  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  
  <!-- convert satops to nodes --> 
  <xsl:template match="satop">
    <node id="{@nom}">
      <xsl:apply-templates/>
    </node>
  </xsl:template>

  <!-- add pred -->
  <xsl:template match="prop">
    <xsl:attribute name="pred"><xsl:value-of select="@name"/></xsl:attribute>
  </xsl:template>  
  
  <!-- convert sem features -->
  <xsl:template match="diamond[not(nom)]">
    <xsl:attribute name="{@mode}"><xsl:value-of select="prop/@name"/></xsl:attribute>
  </xsl:template>
  
  <!-- convert modal rels to rels with nodes -->
  <xsl:template match="diamond[nom]">
    <rel name="{@mode}">
      <node>
        <xsl:apply-templates/>
      </node>
    </rel>
  </xsl:template>
  
  <!-- convert initial nominals to id's --> 
  <xsl:template match="nom[following-sibling::*]">
    <xsl:attribute name="id"><xsl:value-of select="@name"/></xsl:attribute>
  </xsl:template>
  
  <!-- convert solo nominals to idref's --> 
  <xsl:template match="nom[not(following-sibling::*)]">
    <xsl:attribute name="idref"><xsl:value-of select="@name"/></xsl:attribute>
  </xsl:template>


  <!-- convert xor to one-of -->
  <xsl:template match="op[@name='xor']">
    <one-of>
      <xsl:for-each select="*">
        <!-- add atts unless satop or diamond -->
        <xsl:choose>
          <xsl:when test="not(self::satop or self::diamond)">
            <atts><xsl:apply-templates/></atts>
          </xsl:when>
          <xsl:otherwise>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </one-of>
  </xsl:template>
  
  <!-- convert opts -->
  <xsl:template match="op[@name='opt']">
    <opt><xsl:apply-templates/></opt>
  </xsl:template>
    
  <!-- filter conj -->
  <xsl:template match="op[@name='conj']">
    <xsl:apply-templates/>
  </xsl:template>
    
  
  <!-- Copy -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>

