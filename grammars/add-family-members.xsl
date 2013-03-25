<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.8 $, $Date: 2005/07/18 21:19:41 $ 

This transformation adds family members to an input lexicon 
from a dictionary file given as a parameter. 
It also adds particular stems for a family entry.
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan2">

  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>
  
  
  <!-- Name of the dictionary file with the family members to add -->
  <xsl:param name="dict-file"/>
  
  <!-- Key to find members of a family -->
  <xsl:key name="member-of-key" match="member-of" use="@family"/>

  <!-- Key to find particular stems for a family entry -->
  <xsl:key name="stem-for-key" match="stem-for" use="concat(@family,'.',@entry)"/>
  
  
  <!-- Start with xref check on member-of and stem-for elements in dict file -->
  <xsl:template match="/">
    <!-- Change context to dict-file -->
    <xsl:variable name="root" select="."/>
    <xsl:for-each select="document($dict-file)">
      <!-- Check member-of elements -->
      <xsl:for-each select="//member-of">
        <xsl:variable name="family" select="@family"/>
        <xsl:if test="not($root//family[@name=$family])">
          <xsl:message>
            <xsl:text>Warning, no family with name '</xsl:text>
            <xsl:value-of select="$family"/>
            <xsl:text>' found for entry with stem '</xsl:text>
            <xsl:value-of select="../@stem"/>
            <xsl:text>'. </xsl:text>
          </xsl:message>
        </xsl:if>
      </xsl:for-each>
      <!-- Check stem-for elements -->
      <xsl:for-each select="//stem-for">
        <xsl:variable name="family" select="@family"/>
        <xsl:variable name="entry" select="@entry"/>
        <xsl:if test="not($root//family[@name=$family]/entry[@name=$entry])">
          <xsl:message>
            <xsl:text>Warning, no entry with name '</xsl:text>
            <xsl:value-of select="$entry"/>
            <xsl:text>' found in family with name '</xsl:text>
            <xsl:value-of select="$family"/>
            <xsl:text>' for entry with stem '</xsl:text>
            <xsl:value-of select="../@stem"/>
            <xsl:text>'. </xsl:text>
          </xsl:message>
        </xsl:if>
      </xsl:for-each>
    </xsl:for-each>
    <!-- Continue with lexicon transformation -->
    <xsl:apply-templates/>
  </xsl:template>
  
  
  <!-- Add family members to a family -->
  <xsl:template match="family">
    <xsl:copy>
      <!-- Copy attributes and entries -->
      <xsl:apply-templates select="@*|node()"/>
      <!-- Add members ... -->
      <xsl:variable name="family" select="@name"/>
      <!-- Change context to dict-file -->
      <xsl:for-each select="document($dict-file)">
      <!-- Find member-of elements for this family -->
      <xsl:for-each select="key('member-of-key', $family)">
        <!-- Add a member for the entry's stem -->
        <member stem="{../@stem}">
          <!-- Add @pred, if any (with preference given to local specification) -->
          <xsl:if test="../@pred">
            <xsl:attribute name="pred"><xsl:value-of select="../@pred"/></xsl:attribute>
          </xsl:if>
          <xsl:if test="@pred">
            <xsl:attribute name="pred"><xsl:value-of select="@pred"/></xsl:attribute>
          </xsl:if>
        </member>
      </xsl:for-each>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>
  
  
  <!-- Add particular stem for a family entry, if any -->
  <xsl:template match="entry">
    <xsl:copy>
      <!-- Copy attributes -->
      <xsl:apply-templates select="@*"/>
      <!-- Look for particular stem ... -->
      <xsl:variable name="family-dot-entry" select="concat(../@name,'.',@name)"/>
      <!-- Change context to dict-file -->
      <xsl:for-each select="document($dict-file)">
      <xsl:variable name="stem-for" select="key('stem-for-key', $family-dot-entry)"/>
      <!-- Add @stem if a particular stem found for this entry -->
      <xsl:if test="$stem-for">
        <xsl:attribute name="stem"><xsl:value-of select="$stem-for/../@stem"/></xsl:attribute>
      </xsl:if>
      </xsl:for-each>
      <!-- Copy rest -->
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

