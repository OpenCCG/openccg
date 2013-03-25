<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.9 $, $Date: 2005/07/22 20:44:22 $ 

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

  <xsl:variable name="adj">
    <complexcat>
      <xsl:copy-of select="$n.2.X"/>
      <slash dir="/" mode="^" />
      <xsl:copy-of select="$n.2.X"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="adj-np">
    <complexcat>
      <xsl:copy-of select="$np.2.X"/>
      <slash dir="/" mode="^" />
      <xsl:copy-of select="$np.2.X"/>
    </complexcat>
  </xsl:variable>
  
  
  <!-- ***** Adjective Families ***** -->
  <xsl:template name="add-adj-families">

  <xsl:variable name="X.HasProp.P.Default">
    <lf>
      <satop nomvar="X:sem-obj">
        <diamond mode="HasProp">
          <nomvar name="P:proposition"/>
          <prop name="[*DEFAULT*]"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <xsl:variable name="P.Default.Of.X">  
    <lf>
      <satop nomvar="P:proposition">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Of"><nomvar name="X:sem-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <family name="Adjective" pos="Adj" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adj)/*"/>
        <xsl:with-param name="ext" select="$X.HasProp.P.Default"/>
      </xsl:call-template>
    </entry>
    <entry name="Predicative">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($pred.adj)/*"/>
        <xsl:with-param name="ext" select="$P.Default.Of.X"/>
      </xsl:call-template>
    </entry>
  </family>

  <xsl:variable name="X-measure.HasProp.P.Default">
    <lf>
      <satop nomvar="X:measure">
        <diamond mode="HasProp">
          <nomvar name="P:proposition"/>
          <prop name="[*DEFAULT*]"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Adjective-Measure" pos="Adj" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adj-np)/*"/>
        <xsl:with-param name="ext" select="$X-measure.HasProp.P.Default"/>
      </xsl:call-template>
    </entry>
  </family>

  </xsl:template>

</xsl:transform>


