<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.8 $, $Date: 2004/11/21 12:06:50 $ 

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

  <xsl:variable name="prep.n-postmod">
    <complexcat>
      <xsl:copy-of select="$n.2.X"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$n.2.X"/>
      <slash dir="/" mode="&lt;"/>
      <xsl:copy-of select="$np.3.Y.acc"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="prep.n-postmod.plus.pred.Y">    
    <xsl:call-template name="insert-after-last-leftward-arg">
      <xsl:with-param name="elt" select="xalan:nodeset($prep.n-postmod)/*"/>
      <xsl:with-param name="ins">
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$pred.adj.Y"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="pred.prep">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($pred.adj)/*"/>
        <xsl:with-param name="ext">
          <slash dir="/" mode="&lt;"/>
          <xsl:copy-of select="$np.3.Y.acc"/>
        </xsl:with-param>
      </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="prep.appos">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$s.1.E.default"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$punct.comma"/>
      <slash dir="/" mode="*"/>
      <xsl:copy-of select="$np.3.Y.acc"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="prep.appos.plus.pred.P2.Y">    
    <xsl:call-template name="insert-after-last-leftward-arg">
      <xsl:with-param name="elt" select="xalan:nodeset($prep.appos)/*"/>
      <xsl:with-param name="ins">
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$pred.adj.P2.Y"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="prep.transitional">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($adv.transitional)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="*"/>
        <xsl:copy-of select="$np.3.Y.acc"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <!-- NB: has-rel is a dummy pred which serves as the semantic head -->
  <xsl:template name="make-has-rel-lf">
    <xsl:param name="rel"/>  
    <xsl:param name="sort-X" select="sem-obj"/>
    <xsl:param name="sort-Y" select="sem-obj"/>
    <lf>
      <satop nomvar="P:proposition">
        <prop name="has-rel"/>
        <diamond mode="info"><var name="INFO"/></diamond>
        <diamond mode="owner"><var name="OWNER"/></diamond>
        <diamond mode="kon"><prop name="-"/></diamond>
        <diamond mode="Of"><nomvar name="{concat('X:',$sort-X)}"/></diamond>
        <diamond mode="{$rel}"><nomvar name="{concat('Y:',$sort-Y)}"/></diamond>
      </satop>
    </lf>
  </xsl:template>

   
  <!-- ***** PP Families ***** -->
  <xsl:template name="add-pp-families">

  <family name="Particle" pos="Prep" closed="true" indexRel="*NoSem*">
    <entry name="Primary">
      <atomcat type="prt">
        <fs><feat attr="lex" val="[*DEFAULT*]"/></fs>
      </atomcat>
    </entry>
  </family>
  
  <family name="Prep-Nom" pos="Prep" closed="true" indexRel="*NoSem*">
    <entry name="Nominal">
      <complexcat>
        <xsl:copy-of select="$pp.from-3.lex-Default"/>
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$np.3.Y.acc"/>
      </complexcat>
    </entry>
  </family>
  
  <xsl:variable name="X.FigInv.P.Default.Ground.Y">
    <lf>
      <satop nomvar="X:sem-obj">
        <diamond mode="FigInv">
          <nomvar name="P:proposition"/>
          <prop name="[*DEFAULT*]"/>
          <diamond mode="Ground"><nomvar name="Y:sem-obj"/></diamond>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <xsl:variable name="P.Default.Fig.X.Ground.Y">  
    <lf>
      <satop nomvar="P:proposition">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Fig"><nomvar name="X:sem-obj"/></diamond>
        <diamond mode="Ground"><nomvar name="Y:sem-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <!-- NB: Locative prepositions have Adj as their part-of-speech so that 
           they receive the same treatment as adjectives wrt intonation info.
       NB: Not assigning a sort to nominal (Y) for Ground, to allow for 
           loose usage of locatives. -->
  <family name="Prep-Loc" pos="Adj" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($prep.n-postmod)/*"/>
        <xsl:with-param name="ext" select="$X.FigInv.P.Default.Ground.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="Predicative">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($pred.prep)/*"/>
        <xsl:with-param name="ext" select="$P.Default.Fig.X.Ground.Y"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- With-Poss: appositive builds in elab-rel; Where may optionally give a prop -->
  <xsl:variable name="X.Poss.Y">
    <lf>
      <satop nomvar="X:sem-obj">
        <diamond mode="Poss"><nomvar name="Y:sem-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="X.Poss.Y.Where.P">
    <lf>
      <satop nomvar="X:sem-obj">
        <diamond mode="Poss"><nomvar name="Y:sem-obj"/></diamond>
        <diamond mode="Where"><nomvar name="P:proposition"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="S.elab-rel.Core.E.Trib.P.has-rel.Of.X.Poss.Y">
    <xsl:variable name="has-rel-Poss-lf">
      <xsl:call-template name="make-has-rel-lf">
        <xsl:with-param name="rel">Poss</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($S.elab-rel.Core.E.Trib.P)/*"/>
      <xsl:with-param name="ext" select="xalan:nodeset($has-rel-Poss-lf)/lf/*"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="S.elab-rel.Core.E.Trib.P.has-rel.Of.X.Poss.Y.Where.P2">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($S.elab-rel.Core.E.Trib.P.has-rel.Of.X.Poss.Y)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="P">
          <diamond mode="Where"><nomvar name="P2:proposition"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <family name="With-Poss" pos="Prep" closed="true" indexRel="Poss">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($prep.n-postmod)/*"/>
        <xsl:with-param name="ext" select="$X.Poss.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="Plus-Pred-Y">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($prep.n-postmod.plus.pred.Y)/*"/>
        <xsl:with-param name="ext" select="$X.Poss.Y.Where.P"/>
      </xsl:call-template>
    </entry>
    <entry name="Appos">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($prep.appos)/*"/>
        <xsl:with-param name="ext" select="$S.elab-rel.Core.E.Trib.P.has-rel.Of.X.Poss.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="Appos-Plus-Pred-Y">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($prep.appos.plus.pred.P2.Y)/*"/>
        <xsl:with-param name="ext" select="$S.elab-rel.Core.E.Trib.P.has-rel.Of.X.Poss.Y.Where.P2"/>
      </xsl:call-template>
    </entry>
  </family>
  
  
  <xsl:variable name="E.HasProp.P.Default.Arg.Y">
    <lf>
      <satop nomvar="E:sem-obj">
        <diamond mode="HasProp">
          <nomvar name="P:proposition"/>
          <prop name="[*DEFAULT*]"/>
          <diamond mode="Arg">
            <nomvar name="Y:sem-obj"/>
          </diamond>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>

  <!-- NB: Transitional prepositions have Adv as their part-of-speech so that 
           they receive the same treatment as adverbs wrt intonation info -->
  <family name="Prep-Transitional" pos="Adv" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($prep.transitional)/*"/>
        <xsl:with-param name="ext" select="$E.HasProp.P.Default.Arg.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  </xsl:template>

</xsl:transform>


