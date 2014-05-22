<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-7 University of Edinburgh (Michael White)
$Revision: 1.1 $, $Date: 2009/11/09 19:45:41 $ 

This transformation raises desired shared nodes up to a parallel location, 
so that the resulting subtrees can be more easily chunked into realizable
constituents.  The predicates are moved under a new satop, leaving behind 
just a nominal reference.

This is a modified version of the standard transformation that uses 
First/Next list structures, without elements or items.
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:set="xalan://java.util.HashSet"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="set xalan xalan2">

  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  <!-- tracks nodes which have been moved -->
  <xsl:variable name="moved" select="set:new()"/>
  
  
  <!-- raise coordinated nodes --> 
  <xsl:template match="*[diamond[@mode='First']]">
    <xsl:call-template name="raise-shared-nodes">
      <!-- check for shared nominals under different list items -->
      <xsl:with-param name="rel">Next</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  
  <!-- skip the predicates of moved nodes -->
  <xsl:template match="*[preceding-sibling::nom[set:contains($moved, string(@name))]]" priority="1.0"/>

  
  <!-- raises shared nodes --> 
  <xsl:template name="raise-shared-nodes">
    <xsl:param name="noms-root" select="."/>
    <xsl:param name="rel-root" select="$noms-root"/>
    <xsl:param name="rel"/>
    
    <!-- get all nominals with predicates under $noms-root, but not already moved -->
    <xsl:variable name="noms" 
      select="$noms-root//nom[not(set:contains($moved, string(@name))) and following-sibling::prop]"/>

    <!-- check each such nominal, gathering shared stuff to move -->
    <xsl:variable name="shared"> 
      <xsl:for-each select="$noms">
        <!-- check for the same nominal appearing under and not under $rel from $rel-root -->
        <xsl:variable name="nom" select="@name"/>
        <xsl:if test="$rel-root/diamond[@mode =  $rel]//nom[@name=$nom] and
                      $rel-root/diamond[@mode != $rel]//nom[@name=$nom]"
        >
          <!-- make new satop for preds on shared nominal -->
          <satop nom="{$nom}">
            <xsl:apply-templates select="following-sibling::node()"/>
          </satop>
          <!-- record the move -->
          <xsl:variable name="void" select="set:add($moved, string($nom))"/>
          <!-- nb: need to read result to ensure add operation happens! -->
          <xsl:variable name="void2" select="$void"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <!-- recurse, copying shared stuff at end -->
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:copy-of select="$shared"/>
    </xsl:copy>
  </xsl:template>
  
  
  <!-- Copy -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>

