<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White) 
$Revision: 1.3 $, $Date: 2005/07/18 21:19:41 $ 

This transformation flattens list structures to a simpler structure.
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan2">

  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  
  <!-- simplify lists: change First rel to List ... -->
  <xsl:template match="rel[@name='First']">
    <rel name="List">
      <xsl:apply-templates select="@*[name(.) != 'name'] | node()"/>
    </rel>
  </xsl:template>
  
  <!-- ... and drop EqL and Last nodes -->
  <xsl:template match="rel[@name='EqL' or @name='Last']"/>
  
  <!-- ... filter elem nodes, and Item and Next rels contained therein -->
  <xsl:template match="node[@pred='elem'] | node[@pred='elem']/rel[@name='Item' or @name='Next']">
    <xsl:apply-templates/>
  </xsl:template>
  
  
  <!-- Copy -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>

