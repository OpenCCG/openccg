<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.3 $, $Date: 2005/07/18 21:19:44 $ 
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
  
  <!-- nothing else currently ... --> 

</xsl:transform>

