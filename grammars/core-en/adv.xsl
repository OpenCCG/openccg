<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.4 $, $Date: 2004/11/21 12:06:50 $ 

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

  
  <!-- ***** Categories ***** -->

  <xsl:variable name="adv.initial">
    <complexcat>
      <xsl:copy-of select="$s.from-1.fronted"/>
      <slash dir="/" mode="^"/>
      <xsl:copy-of select="$s.1.E.dcl"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="adv.transitional">
    <complexcat>
      <xsl:copy-of select="$s.from-1.fronted"/>
      <slash dir="/" mode="^"/>
      <xsl:copy-of select="$s.1.E"/>
      <slash dir="/" mode="*"/>
      <xsl:copy-of select="$punct.comma"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="adv.forward">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($vp.1.E.2.X)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="^"/>
        <xsl:copy-of select="$vp.1.E.2.X"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <!--
    NB: Using s\s here to allow: 
          [there is]s/np [also]s\s [a United flight]np
        Not sure if this overgenerates much.
  -->
  <xsl:variable name="adv.backward">
    <complexcat>
      <xsl:copy-of select="$s.1.E"/>
      <slash dir="\"/>
      <xsl:copy-of select="$s.1.E"/>
    </complexcat>
  </xsl:variable>
  
   
  <!-- ***** Adverb Families ***** -->
  <xsl:template name="add-adv-families">

  <xsl:variable name="E.HasProp.P.Default">
    <lf>
      <satop nomvar="E:situation">
        <diamond mode="HasProp">
          <nomvar name="P:proposition"/>
          <prop name="[*DEFAULT*]"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Adverb" pos="Adv" closed="true">
    <entry name="Initial">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adv.initial)/*"/>
        <xsl:with-param name="ext" select="$E.HasProp.P.Default"/>
      </xsl:call-template>
    </entry>
    <entry name="Forward">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adv.forward)/*"/>
        <xsl:with-param name="ext" select="$E.HasProp.P.Default"/>
      </xsl:call-template>
    </entry>
    <entry name="Backward">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adv.backward)/*"/>
        <xsl:with-param name="ext" select="$E.HasProp.P.Default"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Locative Adverbs -->
  <xsl:variable name="E.Location.P.Default">
    <lf>
      <satop nomvar="E:situation">
        <diamond mode="Location">
          <nomvar name="P:location"/>
          <prop name="[*DEFAULT*]"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Loc-Adverb" pos="Adv" closed="true">
    <entry name="Initial">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adv.initial)/*"/>
        <xsl:with-param name="ext" select="$E.Location.P.Default"/>
      </xsl:call-template>
    </entry>
    <entry name="Forward">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adv.forward)/*"/>
        <xsl:with-param name="ext" select="$E.Location.P.Default"/>
      </xsl:call-template>
    </entry>
    <entry name="Backward">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adv.backward)/*"/>
        <xsl:with-param name="ext" select="$E.Location.P.Default"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Transitional Adverbs -->
  <family name="Transitional-Adverb" pos="Adv" closed="true">
    <entry name="Initial">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adv.transitional)/*"/>
        <xsl:with-param name="ext" select="$E.HasProp.P.Default"/>
      </xsl:call-template>
    </entry>
  </family>
  
  </xsl:template>

</xsl:transform>


