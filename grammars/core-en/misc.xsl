<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.5 $, $Date: 2004/11/21 12:06:50 $ 

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
  <xsl:variable name="X.GenRel.E">
    <lf>
      <satop nomvar="X:sem-obj">
        <diamond mode="GenRel"><nomvar name="E:situation"/></diamond>
      </satop>
    </lf>
  </xsl:variable>  
  
  
  <!-- ***** Misc Families ***** -->
  <xsl:template name="add-misc-families">

  <!-- Relativizer -->
  <xsl:variable name="rel">
    <complexcat>
      <xsl:copy-of select="$n.from-2.X.CASE"/>
      <setarg>
        <slash dir="\" mode="*"/>
        <xsl:copy-of select="$n.from-2.X.CASE"/>
        <slash dir="/" mode="*"/>
        <complexcat>
          <xsl:copy-of select="$s.1.E.dcl"/>
          <slash dir="|"/>
          <xsl:copy-of select="$np.2.X.3rd"/>
        </complexcat>
      </setarg>
    </complexcat>
  </xsl:variable>

  <!-- Appositive Relative Clauses -->  
  <xsl:variable name="rel.appos">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$s.1.E.default"/>
      <slash dir="/" mode="*"/>
      <complexcat>
        <xsl:copy-of select="$s.E2.dcl"/>
        <slash dir="|"/>
        <xsl:copy-of select="$np.2.X.3rd"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <family name="RelPro" pos="RelPro" closed="true" indexRel="GenRel">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($rel)/*"/>
        <xsl:with-param name="ext" select="$X.GenRel.E"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <family name="RelPro-Appos" pos="RelPro" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($rel.appos)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Core.E.Trib.E2"/>
      </xsl:call-template>
    </entry>
  </family>
  
  
  <!-- Complementizer -->
  <xsl:variable name="comp">
    <complexcat>
      <xsl:copy-of select="$s.from-1.emb"/>
      <slash dir="/" mode="^"/>
      <xsl:copy-of select="$s.1.E.dcl"/>
    </complexcat>
  </xsl:variable>

  <family name="Comp" pos="Comp" closed="true" indexRel="*NoSem*">
    <entry name="Primary">
      <xsl:copy-of select="$comp"/>
    </entry>
  </family>

  </xsl:template>

</xsl:transform>


