<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.4 $, $Date: 2004/11/20 16:34:43 $ 

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

  
  <!-- ***** Aux Families ***** -->
  <xsl:template name="add-aux-families">

  <!-- Modal -->
  <!-- NB: form=dcl is specified here, rather than in morph file, 
           so that form=q is not mistakenly overridden with inverted aux form -->
  <xsl:variable name="aux">
    <complexcat>
      <xsl:copy-of select="$s.1.E.dcl.default"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np.2.X.nom.default"/>
      <slash dir="/" mode="^"/>
      <complexcat>
        <xsl:copy-of select="$s.E2.base"/>
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.2.X"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="aux.inverted">
    <complexcat>
      <xsl:copy-of select="$s.1.from-2.E.q"/>
      <slash dir="/" mode="^"/>
      <xsl:copy-of select="$s.2.E2.base"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="E.Default.Body.E2">
    <lf>
      <satop nomvar="E:state">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Body"><nomvar name="E2:situation"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Modal" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($aux)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Body.E2"/>
      </xsl:call-template>
    </entry>
    <entry name="Inverted">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($aux.inverted)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Body.E2"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Progressive -->
  <!-- NB: same as Modal except subcats for form=ng -->
  <xsl:variable name="prog">
    <complexcat>
      <xsl:copy-of select="$s.1.E.dcl.default"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np.2.X.nom.default"/>
      <slash dir="/" mode="^"/>
      <complexcat>
        <xsl:copy-of select="$s.E2.ng"/>
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.2.X"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="prog.inverted">
    <complexcat>
      <xsl:copy-of select="$s.1.from-2.E.q"/>
      <slash dir="/" mode="^"/>
      <xsl:copy-of select="$s.2.E2.ng"/>
    </complexcat>
  </xsl:variable>

  <family name="Progressive" pos="V" closed="true" indexRel="prog">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($prog)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Body.E2"/>
      </xsl:call-template>
    </entry>
    <entry name="Inverted">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($prog.inverted)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Body.E2"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Negation -->
  <!-- NB: form agreement in morph file -->
  <xsl:variable name="neg">
    <complexcat>
      <xsl:copy-of select="$s.1.from-6.E"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np.2.X"/>
      <slash dir="/" mode="^"/>
      <complexcat>
        <xsl:copy-of select="$s.6.E2"/>
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.2.X"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <family name="Negation" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($neg)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Body.E2"/>
      </xsl:call-template>
    </entry>
  </family>
  
  
  <!-- Do Support -->
  <!-- This treatment of 'do' enables wh-questions 
       extracting from subjects and objects to be treated the same;  
       to indicate positive polarity, 'do' can be treated as a modal, 
       with the possibility of lexical emphasis. -->
  <xsl:variable name="do-support.inverted">
    <complexcat>
      <xsl:copy-of select="$s.1.from-2.E.q"/>
      <slash dir="/" mode="^"/>
      <xsl:copy-of select="$s.2.E.base"/>
    </complexcat>
  </xsl:variable>

  <family name="Do-Support" pos="V" closed="true" indexRel="tense">
    <entry name="Inverted">
      <xsl:copy-of select="$do-support.inverted"/>
    </entry>
  </family>
  
  </xsl:template>

</xsl:transform>


