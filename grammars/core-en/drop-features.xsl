<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White) 
$Revision: 1.6 $, $Date: 2005/07/18 21:19:44 $ 

This transformation drops any info and owner features that are derivable from 
their parent nodes, as well as any default no k-contrast features.
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan2">

  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  
  <!-- drop default no k-contrast features -->
  <xsl:template match="@kon[string(.)='-']"/>

  <!-- drop info or owner features when same as parent -->
  <xsl:template match="@info[string(.)=string(../ancestor::node[@info][1]/@info)]" priority="1.0"/>
  <xsl:template match="@owner[string(.)=string(../ancestor::node[@owner][1]/@owner)]" priority="1.0"/>
  
  <!-- drop info when missing -->
  <xsl:template match="@info[string(.)='']"/>

  <!-- drop owner when predictable from info or mood -->
  <xsl:template match="@owner[string(.)='h' and ../@info='th']"/>
  <xsl:template match="@owner[string(.)='h' and ../@mood='int']"/>
  <xsl:template match="@owner[string(.)='s' and not(../ancestor::node[@owner][1]/@owner) 
                              and (../@info='rh' or ../@info='') and not(../@mood='int')]"/>
  
  
  <!-- Copy -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>

