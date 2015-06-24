<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-5 University of Edinburgh (Michael White)
$Revision: 1.1 $, $Date: 2009/11/09 19:45:41 $ 

This transformation converts a node-rel graph to a hybrid logic 
dependency semantics representation.
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan2">

  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  
  
  <!-- check for non-empty node content of node elts with id's -->
  <xsl:template match="node[@id and (@shared='true' or count(@*[name(.)!='id'] | *) = 0)]" priority="1.5">
    <xsl:message terminate="yes">Error: node with id = <xsl:value-of select="@id"/> should be a reference (with idref)</xsl:message>
  </xsl:template>
  
    
  <!-- convert stand-alone nodes to satops --> 
  <xsl:template match="node[@id and not(parent::rel or parent::one-of[parent::rel])]" priority="1.0">
    <satop nom="{@id}">
      <xsl:call-template name="convert-feats-and-children"/>
    </satop>
  </xsl:template>
  
  <!-- convert dependent nodes using an initial nominal --> 
  <xsl:template match="node[@id]">
    <!-- add conj op under one-of -->
    <xsl:choose>
      <xsl:when test="parent::one-of">
        <op name="conj">
          <nom name="{@id}"/>
          <xsl:call-template name="convert-feats-and-children"/>
        </op>
      </xsl:when>
      <xsl:otherwise>
        <nom name="{@id}"/>
        <xsl:call-template name="convert-feats-and-children"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- convert stand-alone node references with children or attributes to satops (keeping shared attribute, if present) --> 
  <xsl:template match="node[@idref and count(@*[name(.)!='idref' and name(.)!='shared'] | *) &gt; 0 and not(parent::rel or parent::one-of[parent::rel])]" priority="1.0">
    <satop nom="{@idref}">
      <xsl:copy-of select="@shared"/>
      <xsl:call-template name="convert-feats-and-children"/>
    </satop>
  </xsl:template>
  
  <!-- convert dependent node references with children or attributes using an initial nominal (keeping shared attribute, if present) --> 
  <xsl:template match="node[@idref and count(@*[name(.)!='idref' and name(.)!='shared'] | *) &gt; 0]">
    <!-- add conj op under one-of -->
    <xsl:choose>
      <xsl:when test="parent::one-of">
        <op name="conj">
          <nom name="{@idref}"> <xsl:copy-of select="@shared"/> </nom>
          <xsl:call-template name="convert-feats-and-children"/>
        </op>
      </xsl:when>
      <xsl:otherwise>
        <nom name="{@idref}"> <xsl:copy-of select="@shared"/> </nom>
        <xsl:call-template name="convert-feats-and-children"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- convert empty node references (with no attributes) to nominals (keeping shared attribute, if present) --> 
  <xsl:template match="node[@idref and count(@*[name(.)!='idref' and name(.)!='shared'] | *) = 0]">
    <nom name="{@idref}"> <xsl:copy-of select="@shared"/> </nom>
  </xsl:template>

    
  <!-- convert rels -->
  <xsl:template match="rel">
    <diamond mode="{@name}">
      <xsl:apply-templates/>
    </diamond>
  </xsl:template>
  
  
  <!-- convert one-of -->
  <xsl:template match="one-of">
    <op name="xor">
      <xsl:apply-templates/>
    </op>
  </xsl:template>

  <!-- convert opt -->
  <xsl:template match="opt">
    <op name="opt">
      <xsl:apply-templates/>
    </op>
  </xsl:template>

  
  <!-- convert atts -->
  <!-- nb: atts can include rels, and can be used to group them too -->
  <xsl:template match="atts">
    <!-- add conj op if multiple preds -->
    <xsl:choose>
      <xsl:when test="count(@*) &gt; 1 or *">
        <op name="conj">
          <xsl:call-template name="convert-feats-and-children"/>
        </op>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="convert-feats-and-children"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  
  <!-- convert sem features of current node and children -->
  <xsl:template name="convert-feats-and-children">
    <!-- convert sem features -->
    <xsl:call-template name="convert-feats"/>
    <!-- do rest -->
    <xsl:apply-templates/>
  </xsl:template>
  
  <!-- convert sem features of current node -->
  <xsl:template name="convert-feats">
    <!-- add pred (if any) -->
    <xsl:if test="@pred">
      <prop name="{@pred}"/>
    </xsl:if>
    <!-- do rest -->
    <xsl:for-each select="@*[name(.) != 'id' and name(.) != 'idref' and name(.) != 'pred' and name(.) != 'shared']">
      <diamond mode="{name(.)}"><prop name="{.}"/></diamond>
    </xsl:for-each>
  </xsl:template>
  
  
  <!-- Copy -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>

