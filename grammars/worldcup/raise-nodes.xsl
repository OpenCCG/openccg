<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.4 $, $Date: 2009/06/16 19:40:53 $ 

This transformation raises desired shared nodes up to a parallel location, 
so that the resulting subtrees can be more easily chunked into realizable
constituents.  The predicates are moved under a new satop, leaving behind 
just a nominal reference.
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="java xalan2">

  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  <!-- use shared raise-nodes.xsl, primarily -->
  <xsl:include href="../raise-nodes.xsl"/>

  
  <!-- raises nodes nested in quantifier's restriction and shared in body --> 
  <xsl:template match="*[diamond[@mode='Restr']]">
    <xsl:call-template name="raise-shared-nodes">
      <!-- check for shared nominals in and out of the restriction -->
      <xsl:with-param name="noms-root" select="."/>
      <xsl:with-param name="rel">Restr</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:transform>

