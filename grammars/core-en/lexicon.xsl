<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.4 $, $Date: 2004/10/05 11:01:47 $ 

The principal sources for this grammar are Hockenmaier 03, Baldridge 02 
and Carpenter 92.  The intonation stuff is from Steedman LI 00. 
Semantic roles taken from FrameNet where possible.

Notes: 
 - The features s[base] and s[inf] are used instead of s[b] and s[to]  
   (cf Hockenmaier 03).
 - Boundary tones are handled differently from Steedman LI 00, namely with 
   distributed semantic features, in order to better allow for discontinuous 
   themes/rhemes.  Also, NP and S lists have LH% boundaries built in (though 
   binary conjunctions like 'but' don't).

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
  
  <xsl:include href="templates.xsl"/>
  <xsl:include href="cats.xsl"/>
  <xsl:include href="np.xsl"/>
  <xsl:include href="det.xsl"/>
  <xsl:include href="adj.xsl"/>
  <xsl:include href="adv.xsl"/>
  <xsl:include href="pp.xsl"/>
  <xsl:include href="v.xsl"/>
  <xsl:include href="auxv.xsl"/>
  <xsl:include href="conj.xsl"/>
  <xsl:include href="misc.xsl"/>
  <xsl:include href="punct.xsl"/>
  <xsl:include href="unary-rules.xsl"/>

  <xsl:template name="add-core-families">
    <xsl:call-template name="add-dummy-family"/>
    <xsl:call-template name="add-np-families"/>
    <xsl:call-template name="add-det-families"/>
    <xsl:call-template name="add-adj-families"/>
    <xsl:call-template name="add-adv-families"/>
    <xsl:call-template name="add-pp-families"/>
    <xsl:call-template name="add-v-families"/>
    <xsl:call-template name="add-aux-families"/>
    <xsl:call-template name="add-conj-families"/>
    <xsl:call-template name="add-misc-families"/>
    <xsl:call-template name="add-punct-families"/>
  </xsl:template>
  
</xsl:transform>


