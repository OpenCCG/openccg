<?xml version="1.0"?>
<!-- 
Copyright (C) 2003 University of Edinburgh (Michael White)
$Revision: 1.10 $, $Date: 2005/07/18 21:19:41 $ 

This transformation extracts the morph info from a dictionary 
into separate morph entries.
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan2">

  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  
  <!-- Change root dictionary node to morph -->
  <xsl:template match="dictionary">
    <morph
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:noNamespaceSchemaLocation="../morph.xsd"
    >
      <xsl:apply-templates select="@*[name(.) != 'xsi:noNamespaceSchemaLocation'] | node()"/>
    </morph>
  </xsl:template>
  
  <!-- Spell out multiple word forms into separate entries -->
  <xsl:template match="entry[word]">
    <xsl:apply-templates/>
  </xsl:template>
  
  <!-- For single entries, change @stem to @word, if @word not there -->
  <xsl:template match="entry[not(word) and not(@word)]/@stem">
    <xsl:attribute name="word"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>
  
  <!-- Make a word form into an entry -->
  <xsl:template match="word">
      <entry word="{@form}" pos="{../@pos}">
      <!-- Add @stem from parent if different from @form -->
      <xsl:if test="@form != ../@stem">
        <xsl:attribute name="stem"><xsl:value-of select="../@stem"/></xsl:attribute>
      </xsl:if>
      <!-- Add @class from parent, if present -->
      <xsl:apply-templates select="../@class"/>
      <!-- Add @macros from word and parent, if any -->
      <xsl:variable name="macros" select="normalize-space(concat(../@macros, ' ', @macros))"/>
      <xsl:if test="$macros">
        <xsl:attribute name="macros"><xsl:value-of select="$macros"/></xsl:attribute>
      </xsl:if>
      <!-- Add @excluded, if present -->
      <xsl:apply-templates select="@excluded"/>
    </entry>
  </xsl:template>
  
  <!-- Ignore @pred, member-of and stem-for elements -->
  <xsl:template match="@pred"/>
  <xsl:template match="member-of|stem-for"/>
  
  
  <!-- Copy -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>

