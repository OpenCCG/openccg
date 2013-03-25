<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-5 University of Edinburgh (Michael White)
$Revision: 1.13 $, $Date: 2009/11/12 00:40:44 $ 

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

  
  
  <!-- default: chunk all non-trivial children when there is at least one relational child --> 
  <xsl:template match="*[(self::diamond or self::satop) and .//diamond[nom]]">
    <xsl:call-template name="chunk-all"/>
  </xsl:template>

  
  <!-- override: don't chunk w/in a disjunctive relation -->
  <xsl:template match="diamond[op[@name='xor']/satop]" priority="1.5">
    <xsl:call-template name="copy"/>
  </xsl:template>
  
  <!-- or with a continuation satop -->
  <xsl:template match="satop[@continuation] | satop[@continuation]/diamond | diamond[nom[@continuation]]" priority="1.45">
    <xsl:call-template name="copy"/>
  </xsl:template>

  
  <!-- or a node containing a (possibly optional) mood feature -->
  <xsl:template match="*[diamond[@mode='mood'] or op[@name='opt']/diamond[@mode='mood']]" priority="1.4">
    <xsl:call-template name="copy"/>
  </xsl:template>

  
  <!-- or w/in scopal body -->
  <xsl:template match="diamond[@mode='Body']" priority="1.3">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <xsl:template match="satop[parent::op[@name='xor' and parent::diamond[@mode='Body']]]" priority="1.3">
    <xsl:call-template name="copy"/>
  </xsl:template>


  <!-- or a tuple -->
  <xsl:template match="*[prop[@name='tup']]" priority="1.2">
    <xsl:call-template name="copy"/>
  </xsl:template>

  
  <!-- or under a First rel containing a tuple item (for gapping) -->
  <xsl:template match="diamond[@mode='First'and diamond[@mode='Item' and prop[@name='tup']]]" priority="1.1">
    <xsl:call-template name="copy"/>
  </xsl:template>
  


  <!-- filter @continuation -->
  <xsl:template match="@continuation"/>


  <!-- chunk all children -->
  <xsl:template name="chunk-all">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <chunk>
        <xsl:apply-templates select="node()"/>
      </chunk>
    </xsl:copy>
  </xsl:template>
  
  
  
  <!-- Copy -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>

