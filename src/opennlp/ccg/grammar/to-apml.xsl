<?xml version="1.0"?>
<!-- 
Copyright (C) 2004 Michael White 
$Revision: 1.8 $, $Date: 2004/12/12 12:06:11 $ 
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan xalan2">
  
  <xsl:output doctype-system="apml.dtd"/>
  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  <xsl:variable name="apostrophe">'</xsl:variable>
  <xsl:variable name="n-apostrophe-t">n't</xsl:variable>
  
  <!-- ***** Start Output Here ***** -->
  <xsl:template match="/">
    <apml>
      <performative>
        <xsl:apply-templates/>
      </performative>
    </apml>
  </xsl:template>

  
  <!-- filter seg, span, multiword elements -->  
  <xsl:template match="seg|span|multiword">
    <xsl:apply-templates/>
  </xsl:template>

  
  <!-- remove word element and add space after each word, 
       unless it's the last word under a pitchaccent -->  
  <xsl:template match="word">
    <xsl:apply-templates/>
    <xsl:if test="not(ancestor::pitchaccent and position()=last())">
      <xsl:call-template name="add-space"/>
    </xsl:if>
  </xsl:template>

  
  <!-- change pitchaccent to emphasis -->  
  <xsl:template match="pitchaccent">
    <!-- spell out type -->
    <xsl:variable name="type">
      <xsl:choose>
        <xsl:when test="@type='H*'">Hstar</xsl:when>
        <xsl:when test="@type='L*'">Lstar</xsl:when>
        <xsl:when test="@type='L+H*'">LplusHstar</xsl:when>
        <xsl:when test="@type='L*+H'">LstarplusH</xsl:when>
        <xsl:when test="@type='H*+L'">HstarplusL</xsl:when>
        <xsl:when test="@type='H+L*'">HplusLstar</xsl:when>
      </xsl:choose>
    </xsl:variable>
    <!-- add emphasis element -->
    <xsl:choose>
      <!-- split around 'and', to avoid accenting 'and' in amounts etc  -->
      <xsl:when test="multiword/word[text()='and' and preceding-sibling::word]">
        <xsl:variable name="and-pos" 
          select="count(multiword/*[following-sibling::word[text()='and']]) + 1"/>
        <emphasis x-pitchaccent="{$type}">
          <xsl:apply-templates select="multiword/*[position() &lt; $and-pos]"/>
        </emphasis>
        <xsl:text> </xsl:text>
        <xsl:apply-templates select="multiword/*[position() = $and-pos]"/>
        <xsl:text> </xsl:text>
        <emphasis x-pitchaccent="{$type}">
          <xsl:apply-templates select="multiword/*[position() &gt; $and-pos]"/>
        </emphasis>
      </xsl:when>
      <!-- otherwise leave it around all words -->
      <xsl:otherwise>
        <emphasis x-pitchaccent="{$type}">
          <xsl:apply-templates/>
        </emphasis>
      </xsl:otherwise>
    </xsl:choose>
    <!-- add space -->
    <xsl:call-template name="add-space"/>
  </xsl:template>
  
  
  <!-- change boundary type -->  
  <xsl:template match="boundary">
    <!-- remove % from type -->
    <xsl:variable name="type">
      <xsl:choose>
        <xsl:when test="contains(@type,'%')">
          <xsl:value-of select="substring-before(@type,'%')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@type"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <boundary type="{$type}">
      <xsl:apply-templates/>
    </boundary>
    <xsl:call-template name="add-space"/>
  </xsl:template>

  
  <!-- change AM/PM to aamm/ppmm, to avoid APML tokenization problems -->
  <xsl:template match="word/text()[string(.)='AM']">
    <xsl:text>aamm</xsl:text>
  </xsl:template>
  <xsl:template match="word/text()[string(.)='PM']">
    <xsl:text>ppmm</xsl:text>
  </xsl:template>

  <!-- strip out apostrophes, to avoid APML tokenization problems -->
  <xsl:template match="word/text()[contains(string(.),$apostrophe)]">
    <xsl:value-of select="translate(string(.),$apostrophe,'')"/>
  </xsl:template>
  
  <!-- adds space if following word does not begin with ' or equal n't -->
  <xsl:template name="add-space">
    <xsl:if test="not(following::word[1][starts-with(text(),$apostrophe) or text()=$n-apostrophe-t])">
      <xsl:text> </xsl:text>
    </xsl:if>
  </xsl:template>  
  
  
  <!-- ***** Copy ***** -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>

