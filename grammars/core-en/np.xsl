<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.10 $, $Date: 2004/11/21 12:06:50 $ 

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

  <!-- Noun -->
  <xsl:variable name="X.Default">
    <lf>
      <satop nomvar="X:sem-obj">
        <prop name="[*DEFAULT*]"/>
      </satop>
    </lf>
  </xsl:variable>


  <!-- NP -->
  <xsl:variable name="qnp.rightward-TR">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S"/>
      <slash dir="/" varmodality="M" ability="active"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E"/>
        <slash dir="\" varmodality="M" ability="active"/>
        <xsl:copy-of select="$np.2.X.NUM.3rd.CASE"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="qnp.leftward-TR">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S"/>
      <slash/>
      <dollar name="1"/>
      <slash dir="\" varmodality="M" ability="active"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E"/>
        <slash/>
        <dollar name="1"/>
        <slash dir="/" varmodality="M" ability="active"/>
        <xsl:copy-of select="$np.2.X.NUM.3rd.CASE"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="S.Default.Restr.X.Body.E">  
    <lf>
      <satop nomvar="S:quantification">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Restr">
          <nomvar name="X:sem-obj"/>
        </diamond>
        <diamond mode="Body"><nomvar name="E:situation"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="wh-np.subj">
    <complexcat>
      <xsl:copy-of select="$s.from-1.Q.wh"/>
      <slash dir="/" mode="^"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E"/>
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.2.X.NUM.3rd.CASE"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="wh-np.obj">
    <complexcat>
      <xsl:copy-of select="$s.from-1.Q.wh"/>
      <slash dir="/" mode="^"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E.q"/>
        <slash dir="/"/>
        <xsl:copy-of select="$np.2.X.NUM.3rd.CASE"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="wh-np.leftward-TR">
    <complexcat>
      <xsl:copy-of select="$s.from-1.Q.wh"/>
      <slash/>
      <dollar name="1"/>
      <slash dir="\" varmodality="M" ability="active"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E"/>
        <slash/>
        <dollar name="1"/>
        <slash dir="/" varmodality="M" ability="active"/>
        <xsl:copy-of select="$np.2.X.NUM.3rd.CASE"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="Q.Default.Restr.X.Body.E">  
    <lf>
      <satop nomvar="Q:quantification">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Restr">
          <nomvar name="X:sem-obj"/>
        </diamond>
        <diamond mode="Body"><nomvar name="E:situation"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  
  <!-- ***** NP Families ***** -->
  <xsl:template name="add-np-families">

  <!-- Noun -->
  <family name="Noun" pos="N">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($n.2.X.default)/*"/>
        <xsl:with-param name="ext" select="$X.Default"/>
      </xsl:call-template>
    </entry>
  </family>

  
  <!-- Num -->
  <family name="Num" pos="Num">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($num.2.X.default)/*"/>
        <xsl:with-param name="ext">
          <lf>
            <satop nomvar="X:num">
              <prop name="[*DEFAULT*]"/>
            </satop>
          </lf>
        </xsl:with-param>
      </xsl:call-template>
    </entry>
  </family>
  
  
  <!-- NP -->
  <family name="ProNP" pos="Pro" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($np.2.X.default)/*"/>
        <xsl:with-param name="ext" select="$X.Default"/>
      </xsl:call-template>
    </entry>
  </family>

  <family name="Name" pos="NNP">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($np.2.X.3rd)/*"/>
        <xsl:with-param name="ext" select="$X.Default"/>
      </xsl:call-template>
    </entry>
  </family>


  <xsl:variable name="np_expl.lex-Default.rightward-TR">
    <complexcat>
      <xsl:copy-of select="$s.1"/>
      <slash dir="/" varmodality="M" ability="active"/>
      <complexcat>
        <xsl:copy-of select="$s.1"/>
        <slash dir="\" varmodality="M" ability="active"/>
        <xsl:copy-of select="$np_expl.lex-Default"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <family name="ExplNP" pos="Expl" closed="true" indexRel="*NoSem*">
    <entry name="Primary">
      <xsl:copy-of select="$np_expl.lex-Default"/>
    </entry>
    <entry name="RightwardTypeRaised">
      <xsl:copy-of select="$np_expl.lex-Default.rightward-TR"/>
    </entry>
  </family>

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
    <entry name="ObjectInSitu">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($wh-np.leftward-TR)/*"/>
        <xsl:with-param name="ext" select="$Q.Default.Restr.X.Body.E"/>
      </xsl:call-template>
    </entry>
  </family>

  </xsl:template>

</xsl:transform>


