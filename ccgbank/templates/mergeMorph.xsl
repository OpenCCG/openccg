<?xml version="1.0"?>
<xsl:transform
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xalan xalan2 java">

  <xsl:output method="xml" indent="yes" xalan2:indent-amount="2"/>
  <xsl:strip-space elements="*"/>
  
  <!-- Transform which mergies entries from a second ('new') morph file -->
  
  <!-- root and member key, for efficient checks of seen items -->
  <xsl:variable name="morphroot" select="/morph"/>
  <xsl:key name="entrykey" match="entry" use="concat(@word,'_',@stem,'_',@pos,'_',@class)"/>
  
  <!-- new morph file -->
  <xsl:param name="newmorphfile">morph.xml</xsl:param>
  <xsl:variable name="newmorphroot" select="document($newmorphfile)/morph"/>
  
  <!-- start here -->
  <xsl:template match="/morph">
    <morph>
      <!-- copy existing entries -->
      <xsl:apply-templates select="@*|node()"/>
      <!-- add new ones -->
      <xsl:for-each select="$newmorphroot/entry">
	<xsl:variable name="newkey" select="concat(@word,'_',@stem,'_',@pos,'_',@class)"/>
	<!-- ensure word not already there -->
	<xsl:if test="not($morphroot[key('entrykey',$newkey)])">
	  <xsl:copy-of select="."/>
	</xsl:if>
      </xsl:for-each>
    </morph>
  </xsl:template>
  
  
  <!-- Copy Rule -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>
