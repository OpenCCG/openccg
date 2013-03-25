<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.5 $, $Date: 2004/11/21 12:06:50 $ 

NB: These namespace declarations seem to work with the version of Xalan 
    that comes with JDK 1.4.  With newer versions of Xalan, 
    different namespace declarations may be required. 
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan xalan2">

  
  <!-- ***** Categories ***** -->

  <xsl:variable name="fslash-n.2.X.NUM">
    <slash dir="/" mode="^" />
    <xsl:copy-of select="$n.2.X.NUM"/>
  </xsl:variable>
  
  
  <!-- ***** Determiner Families ***** -->
  <xsl:template name="add-det-families">

  <!-- Simple Determiners -->
  <xsl:variable name="det">
    <complexcat>
      <xsl:copy-of select="$np.2.X.3rd"/>
      <xsl:copy-of select="$fslash-n.2.X.NUM"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="X.det.Default">
    <lf>
      <satop nomvar="X:sem-obj">
        <diamond mode="det">
          <prop name="[*DEFAULT*]"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Det" pos="Det" closed="true" indexRel="det">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($det)/*"/>
        <xsl:with-param name="ext" select="$X.det.Default"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Predicational Determiners -->
  <xsl:variable name="X.Det.P.Default">
    <lf>
      <satop nomvar="X:sem-obj">
        <diamond mode="Det">
          <nomvar name="P:proposition"/>
          <prop name="[*DEFAULT*]"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="PDet" pos="PDet" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($det)/*"/>
        <xsl:with-param name="ext" select="$X.Det.P.Default"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- 'One' Determiner: essentially a special case of the card and bare plural rules --> 
  <family name="One-Det" pos="Num" closed="true">
    <entry name="Primary">
      <complexcat>
        <xsl:copy-of select="$np.2.W.3rd"/>
        <slash dir="/" mode="^" />
        <xsl:copy-of select="$n.2.W"/>
        <lf>
          <satop nomvar="W:sem-obj">
            <diamond mode="det"><prop name="nil"/></diamond>
            <diamond mode="Card"><nomvar name="X:num"/></diamond>
          </satop>
          <satop nomvar="X:num">
            <prop name="[*DEFAULT*]"/>
          </satop>
        </lf>
      </complexcat>
    </entry>
  </family>

  <!-- Possessive Pronouns -->
  <xsl:variable name="X.GenOwner.P.Default">
    <lf>
      <satop nomvar="X:sem-obj">
        <diamond mode="GenOwner">
          <nomvar name="P:sem-obj"/>
          <prop name="[*DEFAULT*]"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="PossPro" pos="PossPro" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($det)/*"/>
        <xsl:with-param name="ext" select="$X.GenOwner.P.Default"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- 's :- np[X]/n[X]\np[Y] :- @X(<GenOwner>Y) -->
  <xsl:variable name="poss.s">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($det)/*"/>
      <xsl:with-param name="ext">
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.3.Y.gen"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="X.GenOwner.Y">
    <lf>
      <satop nomvar="X:sem-obj">
        <diamond mode="GenOwner">
          <nomvar name="Y:sem-obj"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="PossS" pos="PossS" closed="true" indexRel="GenOwner">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($poss.s)/*"/>
        <xsl:with-param name="ext" select="$X.GenOwner.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Quantificational Determiners -->
  <xsl:variable name="qdet.rightward-TR">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($qnp.rightward-TR)/*"/>
      <xsl:with-param name="ext" select="$fslash-n.2.X.NUM"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="qdet.leftward-TR">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($qnp.leftward-TR)/*"/>
      <xsl:with-param name="ext" select="$fslash-n.2.X.NUM"/>
    </xsl:call-template>
  </xsl:variable>

  <family name="QuantDet" pos="QDet" closed="true">
    <entry name="RightwardTypeRaised">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($qdet.rightward-TR)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Restr.X.Body.E"/>
      </xsl:call-template>
    </entry>
    <entry name="LeftwardTypeRaised">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($qdet.leftward-TR)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Restr.X.Body.E"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Wh Determiners -->
  <xsl:variable name="wh-det.subj">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($wh-np.subj)/*"/>
      <xsl:with-param name="ext" select="$fslash-n.2.X.NUM"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="wh-det.obj">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($wh-np.obj)/*"/>
      <xsl:with-param name="ext" select="$fslash-n.2.X.NUM"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="wh-det.leftward-TR">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($wh-np.leftward-TR)/*"/>
      <xsl:with-param name="ext" select="$fslash-n.2.X.NUM"/>
    </xsl:call-template>
  </xsl:variable>

  <family name="WhDet" pos="Wh" closed="true">
    <entry name="SubjectExtraction">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($wh-det.subj)/*"/>
        <xsl:with-param name="ext" select="$Q.Default.Restr.X.Body.E"/>
      </xsl:call-template>
    </entry>
    <entry name="ObjectExtraction">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($wh-det.obj)/*"/>
        <xsl:with-param name="ext" select="$Q.Default.Restr.X.Body.E"/>
      </xsl:call-template>
    </entry>
    <entry name="ObjectInSitu">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($wh-det.leftward-TR)/*"/>
        <xsl:with-param name="ext" select="$Q.Default.Restr.X.Body.E"/>
      </xsl:call-template>
    </entry>
  </family>

  </xsl:template>

</xsl:transform>


