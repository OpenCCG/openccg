<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 Jason Baldridge and University of Edinburgh (Michael White)
$Revision: 1.23 $, $Date: 2004/10/05 11:01:48 $ 

This grammar is a version of Jason's English fragment from his dissertation 
that has been upgraded to work with the new approach to semantic construction 
and grammar definition, and now also includes a new top-level category 
(for distinguishing declaratives from interrogatives), y/n questions, 
distributive NP coordination and gapping. 
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="xalan">

  <xsl:output indent="yes"/> 
  <xsl:strip-space elements="*"/>

  
  <!-- ***** Start Output Here ***** -->
  <xsl:template match="/">
  <ccg-lexicon name="worldcup"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation="../lexicon.xsd"
  >

  <!-- ***** Feature Declarations ******  -->

  <licensing-features>
    <feat attr="vform" val="inf" location="target-only"/>
    <feat attr="marking" val="marked" license-empty-cats="false" location="target-only"/>
    <feat attr="inv" val="+" license-marked-cats="true"/>
  </licensing-features>
  
  <!-- ***** Relation Sorting ******  -->
  <relation-sorting order=
    "BoundVar First
     Restr Body Pred 
     Actor Patient Recipient Situation
     Item Item1 Item2
     * 
     Direction Modifier Ref 
     GenRel"/>
    
    
  <!-- ***** Base Categories ******  -->
  
  <xsl:variable name="n.2.X.default">
    <atomcat type="n">
      <fs id="2">
        <feat attr="num"><featvar name="NUM"/></feat>
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="n.2.X">
    <atomcat type="n">
      <fs id="2">
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="X.Default">
    <lf>
      <satop nomvar="X">
        <prop name="[*DEFAULT*]"/>
      </satop>
    </lf>
  </xsl:variable>

  <!-- NB: not dealing with case info -->  
  <xsl:variable name="np.2.X.default">
    <atomcat type="np">
      <fs id="2">
        <feat attr="num"><featvar name="NUM"/></feat>
        <feat attr="3rd"><featvar name="3RD"/></feat>
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.2.X.3rd">
    <atomcat type="np">
      <fs id="2">
        <feat attr="3rd" val="+"/>
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.2.X">
    <atomcat type="np">
      <fs id="2">
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.3.Y">
    <atomcat type="np">
      <fs id="3">
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="np.4.Z">
    <atomcat type="np">
      <fs id="4">
        <feat attr="index"><lf><nomvar name="Z"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="np.X0.pl">
    <atomcat type="np">
      <fs>
        <feat attr="num" val="pl"/>
        <feat attr="index"><lf><nomvar name="X0"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.X1">
    <atomcat type="np">
      <fs>
        <feat attr="index"><lf><nomvar name="X1"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.X2">
    <atomcat type="np">
      <fs>
        <feat attr="index"><lf><nomvar name="X2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.Y1">
    <atomcat type="np">
      <fs>
        <feat attr="index"><lf><nomvar name="Y1"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.Y2">
    <atomcat type="np">
      <fs>
        <feat attr="index"><lf><nomvar name="Y2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>


  <xsl:variable name="pp.from-2.lex-Default">
    <atomcat type="pp">
      <fs inheritsFrom="2">
        <feat attr="lex" val="[*DEFAULT*]"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="pp.3.Y">
    <atomcat type="pp">
      <fs id="3">
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>


  <xsl:variable name="s.1.E.default">
    <atomcat type="s">
      <fs id="1">
        <feat attr="num"><featvar name="NUM"/></feat>
        <feat attr="3rd"><featvar name="3RD"/></feat>
        <feat attr="vform" val="fin"/>
        <feat attr="marking" val="none"/>
        <feat attr="inv" val="-"/>
        <feat attr="quant" val="-"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.1.E.QUANT.default">
    <atomcat type="s">
      <fs id="1">
        <feat attr="num"><featvar name="NUM"/></feat>
        <feat attr="3rd"><featvar name="3RD"/></feat>
        <feat attr="vform" val="fin"/>
        <feat attr="marking" val="none"/>
        <feat attr="inv" val="-"/>
        <feat attr="quant"><featvar name="QUANT"/></feat>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.1.E.inv.QUANT.default">
    <atomcat type="s">
      <fs id="1">
        <feat attr="num"><featvar name="NUM"/></feat>
        <feat attr="3rd"><featvar name="3RD"/></feat>
        <feat attr="vform" val="fin"/>
        <feat attr="marking" val="none"/>
        <feat attr="inv" val="+"/>
        <feat attr="quant"><featvar name="QUANT"/></feat>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.1.S.quant.default">
    <atomcat type="s">
      <fs id="1">
        <feat attr="num"><featvar name="NUM"/></feat>
        <feat attr="3rd"><featvar name="3RD"/></feat>
        <feat attr="vform" val="fin"/>
        <feat attr="marking" val="none"/>
        <feat attr="inv" val="-"/>
        <feat attr="quant" val="+"/>
        <feat attr="index"><lf><nomvar name="S"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.1.E.unmarked">
    <atomcat type="s">
      <fs id="1">
        <feat attr="marking" val="none"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.6.E.unmarked.-quant">
    <atomcat type="s">
      <fs id="6">
        <feat attr="marking" val="none"/>
        <feat attr="quant" val="-"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.1.E.inv">
    <atomcat type="s">
      <fs id="1">
        <feat attr="inv" val="+"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.1.E.non-fin">
    <atomcat type="s">
      <fs id="1">
        <feat attr="vform" val="non-fin"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.1.E">
    <atomcat type="s">
      <fs id="1">
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.1.E.fin.unmarked.-inv">
    <atomcat type="s">
      <fs id="1">
        <feat attr="vform" val="fin"/>
        <feat attr="marking" val="none"/>
        <feat attr="inv" val="-"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.E.non-fin.unmarked.QUANT">
    <atomcat type="s">
      <fs>
        <feat attr="vform" val="non-fin"/>
        <feat attr="marking" val="none"/>
        <feat attr="quant"><featvar name="QUANT"/></feat>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.E2.ger">
    <atomcat type="s">
      <fs>
        <feat attr="vform" val="ger"/>
        <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.E2.inf.unmarked">
    <atomcat type="s">
      <fs>
        <feat attr="vform" val="inf"/>
        <feat attr="marking" val="none"/>
        <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.E2.-inv">
    <atomcat type="s">
      <fs>
        <feat attr="inv" val="-"/>
        <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.E2.non-fin.unmarked.QUANT">
    <atomcat type="s">
      <fs>
        <feat attr="vform" val="non-fin"/>
        <feat attr="marking" val="none"/>
        <feat attr="quant"><featvar name="QUANT"/></feat>
        <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.from-1.marked">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="marking" val="marked"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.from-1.fronted">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="marking" val="fronted"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.from-1.inf">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="vform" val="inf"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.from-1.S.quant">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="quant" val="+"/>
        <feat attr="index"><lf><nomvar name="S"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.from-6.S.quant">
    <atomcat type="s">
      <fs inheritsFrom="6">
        <feat attr="quant" val="+"/>
        <feat attr="index"><lf><nomvar name="S"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.from-1.Q.wh">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="marking" val="wh"/>
        <feat attr="index"><lf><nomvar name="Q"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.from-1.E0">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="index"><lf><nomvar name="E0"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.from-1.E1">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="index"><lf><nomvar name="E1"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.1.E2">
    <atomcat type="s">
      <fs id="1">
        <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.E.unmarked.-inv">
    <atomcat type="s">
      <fs>
        <feat attr="marking" val="none"/>
        <feat attr="inv" val="-"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.E.fronted.-inv">
    <atomcat type="s">
      <fs>
        <feat attr="marking" val="fronted"/>
        <feat attr="inv" val="-"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.E.inv">
    <atomcat type="s">
      <fs>
        <feat attr="inv" val="+"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.E.wh">
    <atomcat type="s">
      <fs>
        <feat attr="marking" val="wh"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>


  <xsl:variable name="sent.E">
    <atomcat type="sent">
      <fs>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  
  <!-- ***** Derived Categories and Families ***** -->
  
  <family name="Noun" pos="N">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($n.2.X.default)/*"/>
        <xsl:with-param name="ext" select="$X.Default"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- NP -->
  <xsl:variable name="np.fronted">    
    <complexcat>
      <xsl:copy-of select="$s.from-1.fronted"/>
      <slash dir="/" mode="^"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E.default"/>
        <slash dir="/"/>
        <xsl:copy-of select="$np.2.X.3rd"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <family name="Name" pos="NNP">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($np.2.X.3rd)/*"/>
        <xsl:with-param name="ext" select="$X.Default"/>
      </xsl:call-template>
    </entry>
    <entry name="Fronted">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($np.fronted)/*"/>
        <xsl:with-param name="ext" select="$X.Default"/>
      </xsl:call-template>
    </entry>
  </family>

  <xsl:variable name="qnp.rightward-TR">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S.quant"/>
      <slash dir="/" varmodality="m1" ability="active"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E.unmarked"/>
        <slash dir="\" varmodality="m1" ability="active"/>
        <xsl:copy-of select="$np.2.X.3rd"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="qnp.leftward-TR">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S.quant"/>
      <slash/>
      <dollar name="1"/>
      <slash dir="\" varmodality="m1" ability="active"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E.unmarked"/>
        <slash/>
        <dollar name="1"/>
        <slash dir="/" varmodality="m1" ability="active"/>
        <xsl:copy-of select="$np.2.X.3rd"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="S.Default.Restr.X.Body.E">  
    <lf>
      <satop nomvar="S">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Restr">
          <nomvar name="X"/>
        </diamond>
        <diamond mode="Body"><nomvar name="E"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <family name="QuantNP" pos="QNP" closed="true">
    <entry name="RightwardTypeRaised">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($qnp.rightward-TR)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Restr.X.Body.E"/>
      </xsl:call-template>
    </entry>
    <entry name="LeftwardTypeRaised">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($qnp.leftward-TR)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Restr.X.Body.E"/>
      </xsl:call-template>
    </entry>
  </family>

  <xsl:variable name="wh-np.subj">
    <complexcat>
      <xsl:copy-of select="$s.from-1.Q.wh"/>
      <slash dir="/" mode="*"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E.unmarked"/>
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.2.X.3rd"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="wh-np.obj">
    <complexcat>
      <xsl:copy-of select="$s.from-1.Q.wh"/>
      <slash dir="/" mode="*"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E.inv"/>
        <slash dir="/"/>
        <xsl:copy-of select="$np.2.X.3rd"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="Q.Default.Restr.X.Body.E">  
    <lf>
      <satop nomvar="Q">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Restr">
          <nomvar name="X"/>
        </diamond>
        <diamond mode="Body"><nomvar name="E"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <family name="WhNP" pos="WhNP" closed="true">
    <entry name="SubjectExtraction">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($wh-np.subj)/*"/>
        <xsl:with-param name="ext" select="$Q.Default.Restr.X.Body.E"/>
      </xsl:call-template>
    </entry>
    <entry name="ObjectExtraction">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($wh-np.obj)/*"/>
        <xsl:with-param name="ext" select="$Q.Default.Restr.X.Body.E"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Det -->
  <xsl:variable name="fslash-n.2.X">
    <slash dir="/" mode="^" />
    <xsl:copy-of select="$n.2.X"/>
  </xsl:variable>
  
  <xsl:variable name="det">
    <complexcat>
      <xsl:copy-of select="$np.2.X.3rd"/>
      <xsl:copy-of select="$fslash-n.2.X"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="det.fronted">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($np.fronted)/*"/>
      <xsl:with-param name="ext" select="$fslash-n.2.X"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="X.det.Default">
    <lf>
      <satop nomvar="X">
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
    <entry name="Fronted">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($det.fronted)/*"/>
        <xsl:with-param name="ext" select="$X.det.Default"/>
      </xsl:call-template>
    </entry>
  </family>

  <xsl:variable name="qdet.rightward-TR">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($qnp.rightward-TR)/*"/>
      <xsl:with-param name="ext" select="$fslash-n.2.X"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="qdet.leftward-TR">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($qnp.leftward-TR)/*"/>
      <xsl:with-param name="ext" select="$fslash-n.2.X"/>
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

  <xsl:variable name="wh-det.subj">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($wh-np.subj)/*"/>
      <xsl:with-param name="ext" select="$fslash-n.2.X"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="wh-det.obj">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($wh-np.obj)/*"/>
      <xsl:with-param name="ext" select="$fslash-n.2.X"/>
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
  </family>

  <!-- Adj -->
  <xsl:variable name="adj">
    <complexcat>
      <xsl:copy-of select="$n.2.X"/>
      <xsl:copy-of select="$fslash-n.2.X"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="X.Modifier.P.Default">
    <lf>
      <satop nomvar="X">
        <diamond mode="Modifier">
          <nomvar name="P"/>
          <prop name="[*DEFAULT*]"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Adjective" pos="Adj">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adj)/*"/>
        <xsl:with-param name="ext" select="$X.Modifier.P.Default"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- VP (ie S\NP) -->
  <xsl:variable name="vp.1.E.2.X">
    <complexcat>
      <xsl:copy-of select="$s.1.E"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np.2.X"/>
    </complexcat>
  </xsl:variable>
  
  <!-- Adv -->
  <xsl:variable name="adv.initial">
    <complexcat>
      <xsl:copy-of select="$s.from-1.fronted"/>
      <slash dir="/" mode="^"/>
      <xsl:copy-of select="$s.1.E.unmarked"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="adv.forward">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($vp.1.E.2.X)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="^"/>
        <xsl:copy-of select="$vp.1.E.2.X"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <!-- 
    NB: This category doesn't allow "John said yesterday that Bill smiled" 
        go through, because "John said yesterday" ends up with an inert slash. 
        Another possibility would be to use s$\s$ instead of backwards crossed
        composition, but this would then mistakenly allow "*the referee gave today ..." 
        and "*Joao traveled to skeptically ...".
  -->  
  <xsl:variable name="adv.backward">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($vp.1.E.2.X)/*"/>
      <xsl:with-param name="ext">
        <slash dir="\"/>
        <xsl:copy-of select="$vp.1.E.2.X"/>
      </xsl:with-param>
    </xsl:call-template>
    <!-- 
    <complexcat>
      <xsl:copy-of select="$s.1.E"/>
      <slash/>
      <dollar name="1"/>
      <slash dir="\" mode="*"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E"/>
        <slash/>
        <dollar name="1"/>
      </complexcat>
    </complexcat>
    -->
  </xsl:variable>
  
  <xsl:variable name="E.Modifier.M.Default">
    <lf>
      <satop nomvar="E">
        <diamond mode="Modifier">
          <nomvar name="M"/>
          <prop name="[*DEFAULT*]"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Adverb" pos="Adv">
    <entry name="Initial">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adv.initial)/*"/>
        <xsl:with-param name="ext" select="$E.Modifier.M.Default"/>
      </xsl:call-template>
    </entry>
    <entry name="Forward">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adv.forward)/*"/>
        <xsl:with-param name="ext" select="$E.Modifier.M.Default"/>
      </xsl:call-template>
    </entry>
    <entry name="Backward">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adv.backward)/*"/>
        <xsl:with-param name="ext" select="$E.Modifier.M.Default"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Particles and Prepositions -->
  <family name="Particle" pos="Prep" closed="true" indexRel="*NoSem*">
    <entry name="Primary">
      <atomcat type="prt">
        <fs><feat attr="lex" val="[*DEFAULT*]"/></fs>
      </atomcat>
    </entry>
  </family>
  
  <family name="PrepNom" pos="Prep" closed="true" indexRel="*NoSem*">
    <entry name="Nominal" active="true">
      <complexcat>
        <xsl:copy-of select="$pp.from-2.lex-Default"/>
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$np.2.X"/>
      </complexcat>
    </entry>
  </family>
  
  <family name="PrepAdj" pos="Prep"  closed="false">
    <entry name="NP-Modifier">
      <complexcat>
        <xsl:copy-of select="$n.2.X"/>
        <setarg>
          <slash dir="\" mode="*"/>
          <xsl:copy-of select="$n.2.X"/>
          <slash dir="/" mode="&lt;"/>
          <xsl:copy-of select="$np.3.Y"/>
        </setarg>
        <lf>
          <satop nomvar="X">
            <diamond mode="Modifier">
              <nomvar name="M"/>
              <prop name="[*DEFAULT*]"/>
              <diamond mode="Ref">
                <nomvar name="Y"/>
              </diamond>
            </diamond>
          </satop>
        </lf>
      </complexcat>
    </entry>
  </family>
  
  <family name="PrepAdv" pos="Prep" closed="true">
    <entry name="Backward">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adv.backward)/*"/>
        <xsl:with-param name="ext">
          <slash dir="/" mode="&gt;"/>
          <complexcat>
            <xsl:copy-of select="$s.E2.ger"/>
            <slash dir="\"/>
            <xsl:copy-of select="$np.2.X"/>
          </complexcat>
          <lf>
            <satop nomvar="E">
              <diamond mode="Modifier">
                <nomvar name="M"/>
                <prop name="[*DEFAULT*]"/>
                <diamond mode="Situation">
                  <nomvar name="E2"/>
                </diamond>
              </diamond>
            </satop>
          </lf>
        </xsl:with-param>
      </xsl:call-template>
    </entry>
  </family>

  <!-- IV -->
  <xsl:variable name="iv">    
    <complexcat>
      <xsl:copy-of select="$s.1.E.default"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np.2.X.default"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="E.Default.Actor.X">  
    <lf>
      <satop nomvar="E">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Actor"><nomvar name="X"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <family name="IV" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Actor.X"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- TV (lifted variant is experimental) -->
  <!-- NB: Lifting is only necessary to compose into the object; 
           this is presumably too rare to bother with beyond this
           test suite. -->
  <xsl:variable name="tv">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$np.3.Y"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="tv.lifted">
    <complexcat>
      <xsl:copy-of select="$s.1.S.quant.default"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np.2.X.default"/>
      <slash dir="/" mode="&gt;"/>
      <complexcat>
        <xsl:copy-of select="$s.from-6.S.quant"/>
        <slash dir="/"/>
        <complexcat>
          <xsl:copy-of select="$s.6.E.unmarked.-quant"/>
          <slash dir="\"/>
          <xsl:copy-of select="$np.3.Y"/>
        </complexcat>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="E.Default.Actor.X.Patient.Y">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Actor.X)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E">
          <diamond mode="Patient"><nomvar name="Y"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="TV" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Actor.X.Patient.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="Lifted">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.lifted)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Actor.X.Patient.Y"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- TV Motion -->
  <xsl:variable name="tv.motion">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pp.3.Y"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="E.Default.Actor.X.Direction.Y">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Actor.X)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E">
          <diamond mode="Direction"><nomvar name="Y"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="TV-Motion" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.motion)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Actor.X.Direction.Y"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- TV Scomp -->
  <xsl:variable name="tv.scomp">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$s.E2.-inv"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="E.Default.Actor.X.Situation.E2">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Actor.X)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E">
          <diamond mode="Situation"><nomvar name="E2"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="TV-Scomp" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.scomp)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Actor.X.Situation.E2"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- TV Phrasal -->
  <xsl:variable name="tv.phrasal">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <setarg>
          <slash dir="/" mode="&gt;"/>
          <xsl:copy-of select="$np.3.Y"/>
          <slash dir="/" mode="*"/>
          <atomcat type="prt">
            <fs id="4"/>
          </atomcat>
        </setarg>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="TV-Phrasal" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.phrasal)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Actor.X.Patient.Y"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- TV Prep Complement -->
  <xsl:variable name="tv.prep-complement">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$np.3.Y"/>
        <slash dir="/" mode="*"/>
        <atomcat type="prt">
          <fs id="4"/>
        </atomcat>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="TV-PrepComplement" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.prep-complement)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Actor.X.Patient.Y"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- DTV -->
  <xsl:variable name="dtv">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$np.4.Z"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="E.Default.Actor.X.Patient.Y.Recipient.Z">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Actor.X.Patient.Y)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E">
          <diamond mode="Recipient"><nomvar name="Z"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="DTV" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($dtv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Actor.X.Patient.Y.Recipient.Z"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Object Control -->
  <xsl:variable name="object-control">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <complexcat>
          <xsl:copy-of select="$s.E2.inf.unmarked"/>
          <slash dir="\"/>
          <xsl:copy-of select="$np.3.Y"/>
        </complexcat>
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$np.3.Y"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="object-control.lifted">
    <complexcat>
      <xsl:copy-of select="$s.1.S.quant.default"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np.2.X.default"/>
      <slash dir="/" mode="&gt;"/>
      <complexcat>
        <xsl:copy-of select="$s.E2.inf.unmarked"/>
        <slash dir="\"/>
        <xsl:copy-of select="$np.3.Y"/>
      </complexcat>
      <slash dir="/" mode="&lt;"/>
      <complexcat>
        <xsl:copy-of select="$s.from-6.S.quant"/>
        <slash dir="/"/>
        <complexcat>
          <xsl:copy-of select="$s.6.E.unmarked.-quant"/>
          <slash dir="\"/>
          <xsl:copy-of select="$np.3.Y"/>
        </complexcat>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="E.Default.Actor.X.Patient.Y.Situation.E2">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Actor.X.Patient.Y)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E">
          <diamond mode="Situation"><nomvar name="E2"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="ObjectControl" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($object-control)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Actor.X.Patient.Y.Situation.E2"/>
      </xsl:call-template>
    </entry>
    <entry name="Lifted">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($object-control.lifted)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Actor.X.Patient.Y.Situation.E2"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- To Infinitive -->
  <family name="To-Infinitive" pos="Prep" closed="true" indexRel="*NoSem*">
    <entry name="Basic">
      <complexcat>
        <xsl:copy-of select="$s.from-1.inf"/>
        <slash dir="\" mode="&lt;" ability="inert"/>
        <xsl:copy-of select="$np.2.X"/>
        <slash dir="/" mode="^"/>
        <complexcat>
          <xsl:copy-of select="$s.1.E.non-fin"/>
          <slash dir="\" mode="&lt;"/>
          <xsl:copy-of select="$np.2.X"/>
        </complexcat>
      </complexcat>
    </entry>
  </family>

  <!-- Modal -->
  <xsl:variable name="modal">
    <complexcat>
      <xsl:copy-of select="$s.1.E.QUANT.default"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np.2.X.default"/>
      <slash dir="/" mode="^"/>
      <complexcat>
        <xsl:copy-of select="$s.E2.non-fin.unmarked.QUANT"/>
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.2.X.default"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="modal.inverted">
    <complexcat>
      <xsl:copy-of select="$s.1.E.inv.QUANT.default"/>
      <slash dir="/" mode="^"/>
      <xsl:copy-of select="$s.E2.non-fin.unmarked.QUANT"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="E.Default.Body.E2">
    <lf>
      <satop nomvar="E">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Body"><nomvar name="E2"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Modal" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($modal)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Body.E2"/>
      </xsl:call-template>
    </entry>
    <entry name="Inverted">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($modal.inverted)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Body.E2"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Do Support (experimental) -->
  <!-- NB: While it's good that this treatment of 'do' enables wh-questions 
           extracting from subjects and objects to be treated the same, 
           in declaratives it would probably be better to introduce 'do' 
           only to indicate positive polarity, with the possibility of 
           lexical emphasis. -->
  <xsl:variable name="do-support">
    <complexcat>
      <xsl:copy-of select="$s.1.E.QUANT.default"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np.2.X.default"/>
      <slash dir="/" mode="^"/>
      <complexcat>
        <xsl:copy-of select="$s.E.non-fin.unmarked.QUANT"/>
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.2.X.default"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="do-support.inverted">
    <complexcat>
      <xsl:copy-of select="$s.1.E.inv.QUANT.default"/>
      <slash dir="/" mode="^"/>
      <xsl:copy-of select="$s.E.non-fin.unmarked.QUANT"/>
    </complexcat>
  </xsl:variable>
  
  <family name="Do-Support" pos="V" closed="true" indexRel="tense">
    <entry name="Primary">
      <xsl:copy-of select="$do-support"/>
    </entry>
    <entry name="Inverted">
      <xsl:copy-of select="$do-support.inverted"/>
    </entry>
  </family>
  
  <!-- Rel -->
  <xsl:variable name="rel">
    <complexcat>
      <xsl:copy-of select="$n.2.X"/>
      <setarg>
        <slash dir="\" mode="*"/>
        <xsl:copy-of select="$n.2.X"/>
        <slash dir="/" mode="*"/>
        <complexcat>
          <xsl:copy-of select="$s.1.E.fin.unmarked.-inv"/>
          <slash dir="|"/>
          <xsl:copy-of select="$np.2.X.3rd"/>
        </complexcat>
      </setarg>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="X.GenRel.E">
    <lf>
      <satop nomvar="X">
        <diamond mode="GenRel"><nomvar name="E"/></diamond>
      </satop>
    </lf>
  </xsl:variable>  

  <family name="Rel" pos="Pron" closed="true" indexRel="GenRel">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($rel)/*"/>
        <xsl:with-param name="ext" select="$X.GenRel.E"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Comp -->
  <xsl:variable name="comp">
    <complexcat>
      <xsl:copy-of select="$s.from-1.marked"/>
      <slash dir="/" mode="^"/>
      <xsl:copy-of select="$s.1.E.fin.unmarked.-inv"/>
    </complexcat>
  </xsl:variable>

  <family name="Comp" pos="Pron" closed="true" indexRel="*NoSem*">
    <entry name="Primary">
      <xsl:copy-of select="$comp"/>
    </entry>
  </family>

  <!-- Conj -->
  <!-- NP conjunction with 'and' sets num to pl; with 'or', num is unconstrained -->
  <xsl:variable name="bw.np.X1.fw.np.X2">
    <slash dir="\" mode="*"/>
    <xsl:copy-of select="$np.X1"/>
    <slash dir="/" mode="*"/>
    <xsl:copy-of select="$np.X2"/>
  </xsl:variable>
  
  <xsl:variable name="conj.np.collective">
    <complexcat>
      <xsl:copy-of select="$np.X0.pl"/>
      <xsl:copy-of select="$bw.np.X1.fw.np.X2"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="X0.and.First.L1.elem.Item.X1.Next.L2.elem.Item.X2">
    <lf>
      <satop nomvar="X0">
        <prop name="and"/>
        <diamond mode="First">
          <nomvar name="L1"/>
          <prop name="elem"/>
          <diamond mode="Item"><nomvar name="X1"/></diamond>
          <diamond mode="Next">
            <nomvar name="L2"/>
            <prop name="elem"/>
            <diamond mode="Item"><nomvar name="X2"/></diamond>
          </diamond>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="qnp.pl.rightward-TR">
    <xsl:apply-templates mode="add-np-pl" select="xalan:nodeset($qnp.rightward-TR)/*"/>
  </xsl:variable>
  
  <xsl:variable name="qnp.pl.leftward-TR">
    <xsl:apply-templates mode="add-np-pl" select="xalan:nodeset($qnp.leftward-TR)/*"/>
  </xsl:variable>
  
  <xsl:variable name="conj.np.and.distributive.rightward-TR">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($qnp.pl.rightward-TR)/*"/>
      <xsl:with-param name="ext" select="$bw.np.X1.fw.np.X2"/>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="conj.np.and.distributive.leftward-TR">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($qnp.pl.leftward-TR)/*"/>
      <xsl:with-param name="ext" select="$bw.np.X1.fw.np.X2"/>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="conj.np.or.distributive.rightward-TR">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($qnp.rightward-TR)/*"/>
      <xsl:with-param name="ext" select="$bw.np.X1.fw.np.X2"/>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="conj.np.or.distributive.leftward-TR">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($qnp.leftward-TR)/*"/>
      <xsl:with-param name="ext" select="$bw.np.X1.fw.np.X2"/>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="S.Default.First.L1.elem.Item.X1.Next.L2.elem.Item.X2.BoundVar.X.Pred.E">
    <lf>
      <satop nomvar="S">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="First">
          <nomvar name="L1"/>
          <prop name="elem"/>
          <diamond mode="Item"><nomvar name="X1"/></diamond>
          <diamond mode="Next">
            <nomvar name="L2"/>
            <prop name="elem"/>
            <diamond mode="Item"><nomvar name="X2"/></diamond>
          </diamond>
        </diamond>
        <diamond mode="BoundVar"><nomvar name="X"/></diamond>
        <diamond mode="Pred"><nomvar name="E"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="conj.sentential">
    <complexcat>
      <xsl:copy-of select="$s.from-1.E0"/>
      <slash/>
      <dollar name="1"/>
      <slash dir="\" mode="*"/>
      <complexcat>
        <xsl:copy-of select="$s.from-1.E1"/>
        <slash/>
        <dollar name="1"/>
      </complexcat>
      <slash dir="/" mode="*"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E2"/>
        <slash/>
        <dollar name="1"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="E0.Default.First.L1.elem.Item.E1.Next.L2.elem.Item.E2">
    <lf>
      <satop nomvar="E0">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="First">
          <nomvar name="L1"/>
          <prop name="elem"/>
          <diamond mode="Item"><nomvar name="E1"/></diamond>
          <diamond mode="Next">
            <nomvar name="L2"/>
            <prop name="elem"/>
            <diamond mode="Item"><nomvar name="E2"/></diamond>
          </diamond>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="np.Y1.np.X1.leftward-TR">
    <complexcat>
      <atomcat type="s"/>
      <slash/>
      <dollar name="1"/>
      <slash dir="\"/>
      <complexcat>
        <atomcat type="s"/>
        <slash/>
        <dollar name="1"/>
        <slash dir="/"/>
        <xsl:copy-of select="$np.Y1"/>
        <slash dir="/"/>
        <xsl:copy-of select="$np.X1"/>
      </complexcat>
    </complexcat>
  </xsl:variable>  
  
  <xsl:variable name="np.Y2.np.X2.leftward-TR">
    <complexcat>
      <atomcat type="s"/>
      <slash/>
      <dollar name="1"/>
      <slash dir="\"/>
      <complexcat>
        <atomcat type="s"/>
        <slash/>
        <dollar name="1"/>
        <slash dir="/"/>
        <xsl:copy-of select="$np.Y2"/>
        <slash dir="/"/>
        <xsl:copy-of select="$np.X2"/>
      </complexcat>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="conj.argcluster">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S.quant"/>
      <slash/>
      <dollar name="1"/>
      <slash dir="\"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E.unmarked"/>
        <slash/>
        <dollar name="1"/>
        <slash dir="/"/>
        <xsl:copy-of select="$np.3.Y"/>
        <slash dir="/"/>
        <xsl:copy-of select="$np.2.X"/>
      </complexcat>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$np.Y1.np.X1.leftward-TR"/>
      <slash dir="/" mode="*"/>
      <xsl:copy-of select="$np.Y2.np.X2.leftward-TR"/>
    </complexcat>
  </xsl:variable>

  <!-- NB: should theoretically use s\(s\np/np) here ... -->
  <xsl:variable name="conj.gapping">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S.quant"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$np.X1"/>
      <slash dir="\" mode="*"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E.unmarked"/>
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.2.X"/>
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$np.3.Y"/>
      </complexcat>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$np.Y1"/>
      <slash dir="/" mode="*"/>
      <xsl:copy-of select="$np.Y2.np.X2.leftward-TR"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="argcluster.sem">
    <lf>
      <satop nomvar="S">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="First">
          <nomvar name="L1"/>
          <prop name="elem"/>
          <diamond mode="Item">
            <nomvar name="T1"/>
            <prop name="tup"/>
            <diamond mode="Item1"><nomvar name="X1"/></diamond>
            <diamond mode="Item2"><nomvar name="Y1"/></diamond>
          </diamond>
          <diamond mode="Next">
            <nomvar name="L2"/>
            <prop name="elem"/>
            <diamond mode="Item">
              <nomvar name="T2"/>
              <prop name="tup"/>
              <diamond mode="Item1"><nomvar name="X2"/></diamond>
              <diamond mode="Item2"><nomvar name="Y2"/></diamond>
            </diamond>
          </diamond>
        </diamond>
        <diamond mode="BoundVar">
          <nomvar name="T"/>
          <prop name="tup"/>
          <diamond mode="Item1"><nomvar name="X"/></diamond>
          <diamond mode="Item2"><nomvar name="Y"/></diamond>
        </diamond>
        <diamond mode="Pred"><nomvar name="E"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Conj" pos="Conj" closed="true">
    <entry name="NP-Collective"> 
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($conj.np.collective)/*"/>
        <xsl:with-param name="ext" select="$X0.and.First.L1.elem.Item.X1.Next.L2.elem.Item.X2"/>
      </xsl:call-template>
    </entry>
    <entry name="NP-Distributive-and-Rightward-TR">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($conj.np.and.distributive.rightward-TR)/*"/>
        <xsl:with-param name="ext" select="$S.Default.First.L1.elem.Item.X1.Next.L2.elem.Item.X2.BoundVar.X.Pred.E"/>
      </xsl:call-template>
    </entry>
    <entry name="NP-Distributive-and-Leftward-TR">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($conj.np.and.distributive.leftward-TR)/*"/>
        <xsl:with-param name="ext" select="$S.Default.First.L1.elem.Item.X1.Next.L2.elem.Item.X2.BoundVar.X.Pred.E"/>
      </xsl:call-template>
    </entry>
    <entry name="NP-Distributive-or-Rightward-TR">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($conj.np.or.distributive.rightward-TR)/*"/>
        <xsl:with-param name="ext" select="$S.Default.First.L1.elem.Item.X1.Next.L2.elem.Item.X2.BoundVar.X.Pred.E"/>
      </xsl:call-template>
    </entry>
    <entry name="NP-Distributive-or-Leftward-TR">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($conj.np.or.distributive.leftward-TR)/*"/>
        <xsl:with-param name="ext" select="$S.Default.First.L1.elem.Item.X1.Next.L2.elem.Item.X2.BoundVar.X.Pred.E"/>
      </xsl:call-template>
    </entry>
    <entry name="Sentential">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($conj.sentential)/*"/>
        <xsl:with-param name="ext" select="$E0.Default.First.L1.elem.Item.E1.Next.L2.elem.Item.E2"/>
      </xsl:call-template>
    </entry>
    <entry name="ArgCluster">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($conj.argcluster)/*"/>
        <xsl:with-param name="ext" select="$argcluster.sem"/>
      </xsl:call-template>
    </entry>
    <entry name="Gapping">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($conj.gapping)/*"/>
        <xsl:with-param name="ext" select="$argcluster.sem"/>
      </xsl:call-template>
    </entry>
  </family>

  <xsl:variable name="fullstop.decl.unmarked">
    <complexcat>
      <xsl:copy-of select="$sent.E"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$s.E.unmarked.-inv"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="fullstop.decl.fronted">
    <complexcat>
      <xsl:copy-of select="$sent.E"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$s.E.fronted.-inv"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="E.mood.dcl">
    <lf>
      <satop nomvar="E">
        <diamond mode="mood"><prop name="dcl"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <xsl:variable name="S.declare.Prop.E">
    <lf>
      <satop nomvar="S">
        <prop name="declare"/>
        <diamond mode="Prop">
          <nomvar name="E"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="FullStop" pos="." closed="true" indexRel="mood">
    <entry name="Declare-Unmarked">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($fullstop.decl.unmarked)/*"/>
        <xsl:with-param name="ext" select="$E.mood.dcl"/>
      </xsl:call-template>
    </entry>
    <entry name="Declare-Fronted">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($fullstop.decl.fronted)/*"/>
        <xsl:with-param name="ext" select="$E.mood.dcl"/>
      </xsl:call-template>
    </entry>
  </family>

  <xsl:variable name="qmark.ask.yn">
    <complexcat>
      <xsl:copy-of select="$sent.E"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$s.E.inv"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="qmark.ask.wh">
    <complexcat>
      <xsl:copy-of select="$sent.E"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$s.E.wh"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="E.mood.int">
    <lf>
      <satop nomvar="E">
        <diamond mode="mood"><prop name="int"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <xsl:variable name="S.ask.Prop.E">
    <lf>
      <satop nomvar="S">
        <prop name="ask"/>
        <diamond mode="Prop">
          <nomvar name="E"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="QuestionMark" pos="?" closed="true" indexRel="mood">
    <entry name="Ask-YN">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($qmark.ask.yn)/*"/>
        <xsl:with-param name="ext" select="$E.mood.int"/>
      </xsl:call-template>
    </entry>
    <entry name="Ask-Wh">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($qmark.ask.wh)/*"/>
        <xsl:with-param name="ext" select="$E.mood.int"/>
      </xsl:call-template>
    </entry>
  </family>

  </ccg-lexicon>
  </xsl:template>

  
  <!-- ***** Extend ***** -->
  <xsl:template name="extend">
    <xsl:param name="elt"/>
    <xsl:param name="ext"/>
    <xsl:element name="{name($elt)}">
      <xsl:copy-of select="$elt/@*"/>
      <xsl:copy-of select="$elt/node()"/>
      <xsl:copy-of select="$ext"/>
    </xsl:element>
  </xsl:template>

  
  <!-- ***** Modification modes ***** -->
  <xsl:template match="atomcat[@type='np']/fs" mode="add-np-pl">
    <xsl:copy>
      <xsl:apply-templates select="@*" mode="add-np-pl"/>
      <feat attr="num" val="pl"/>
      <xsl:apply-templates select="node()[@attr != 'num']" mode="add-np-pl"/>
    </xsl:copy>
  </xsl:template>

  
  <!-- ***** Copy ***** -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*|node()" mode="add-np-pl">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="add-np-pl"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>


