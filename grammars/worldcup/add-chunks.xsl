<?xml version="1.0"?>
<!-- 
Copyright (C) 2003 University of Edinburgh (Michael White)
$Revision: 1.4 $, $Date: 2005/07/18 21:19:45 $ 

This transformation adds LF chunks (to be realized separately) to the 
HLDS representations.
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan2">

  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  <!-- use shared add-chunks.xsl, primarily -->
  <xsl:include href="../add-chunks.xsl"/>
  
  <!-- possibly: don't chunk when there's a rel clause, to allow for extraposition 
  <xsl:template match="*[diamond[@mode='GenRel']]">
    <xsl:call-template name="copy"/>
  </xsl:template>
  -->
  
</xsl:transform>

