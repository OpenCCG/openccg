<?xml version="1.0"?>
<!-- 
Copyright (C) 2003 University of Edinburgh (Michael White) 
$Revision: 1.3 $, $Date: 2004/11/19 10:58:58 $ 

This transformation appends the children of the root element 
of the specified file to the root element of the input file.
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan">

  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  
  <!-- Name of the file to append -->
  <xsl:param name="file-to-append"/>
  
  
  <!-- Append to root element, overwriting root attributes -->
  <xsl:template match="/*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="document($file-to-append)/*/@*"/>
      <xsl:apply-templates select="node()"/>
      <xsl:apply-templates select="document($file-to-append)/*/node()"/>
    </xsl:copy>
  </xsl:template>

  
  <!-- Copy -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>

