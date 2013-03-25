<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.14 $, $Date: 2004/11/21 12:06:50 $ 

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

  
  <!-- ***** Punctuation Families ***** -->
  <xsl:template name="add-punct-families">
  
  <!-- Boundary Tones -->
  <!-- NB: The owner feature is changed to nil to avoid repeated application 
           of these semantically null categories. -->
  <!-- NB: A (temp?) hack: for FLIGHTS, the LL-as-LH family allows LL boundaries to appear in the middle of 
           lists, as if they were LH boundaries.  It would probably be nicer to use 
           underspecification, but this would require extensions to the feature-based licensing 
           code. -->
  <family name="BoundaryTone-L-LL%" pos="BT" closed="true" indexRel="*NoSem*">
    <entry name="S">
      <complexcat>
        <xsl:copy-of select="$s.from-1.phr.nil"/>
        <slash/>
        <dollar name="1"/>
        <slash dir="\" mode="*"/>
        <complexcat>
          <xsl:copy-of select="$s.1.INFO.s"/>
          <slash/>
          <dollar name="1"/>
        </complexcat>
      </complexcat>
    </entry>
  </family>
  
  <family name="BoundaryTone-LL%-as-LH%" pos="BT" closed="true" indexRel="*NoSem*">
    <entry name="BT">
      <xsl:copy-of select="$bt.LH"/>
    </entry>
  </family>
  
  <family name="BoundaryTone-LH%" pos="BT" closed="true" indexRel="*NoSem*">
    <entry name="S">
      <complexcat>
        <xsl:copy-of select="$s.from-1.phr.nil"/>
        <slash/>
        <dollar name="1"/>
        <slash dir="\" mode="*"/>
        <complexcat>
          <xsl:copy-of select="$s.1.INFO.h"/>
          <slash/>
          <dollar name="1"/>
        </complexcat>
      </complexcat>
    </entry>
    <entry name="BT">
      <xsl:copy-of select="$bt.LH"/>
    </entry>
  </family>

  <family name="BoundaryTone-HH%" pos="BT" closed="true" indexRel="*NoSem*">
    <entry name="S-q">
      <complexcat>
        <xsl:copy-of select="$s.from-1.phr.nil"/>
        <slash dir="\" mode="*"/>
        <xsl:copy-of select="$s.1.q.INFO.h"/>
      </complexcat>
    </entry>
  </family>

  
  <!-- List Comma -->  
  <xsl:variable name="comma.conj.sentential">
    <complexcat>
      <xsl:copy-of select="$conj.sentential.result"/>
      <xsl:copy-of select="$conj.sentential.left-arg"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$bt.LH"/>
      <slash dir="/" mode="*"/>
      <complexcat>
        <xsl:copy-of select="$s_conj.1.L2.op-index-S"/>
        <slash/>
        <dollar name="1"/>
      </complexcat>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="L1.elem.Item.E1.Next.L2">
    <lf>
      <satop nomvar="L1:struct">
        <prop name="elem"/>
        <diamond mode="Item"><nomvar name="E1:situation"/></diamond>
        <diamond mode="Next"><nomvar name="L2:struct"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="comma.conj.np">
    <complexcat>
      <xsl:copy-of select="$np_conj.from-1.L1.op-index-S"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$np.X1.CASE"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$bt.LH"/>
      <slash dir="/" mode="*"/>
      <xsl:copy-of select="$np_conj.1.L2.op-index-S.INFO.OWNER"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="L1.elem.Item.X1.Next.L2">
    <lf>
      <satop nomvar="L1:struct">
        <prop name="elem"/>
        <diamond mode="Item"><nomvar name="X1:sem-obj"/></diamond>
        <diamond mode="Next"><nomvar name="L2:struct"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <!-- Appositive Comma -->  
  <xsl:variable name="comma.vp.ng">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$s.1.E.default"/>
      <slash dir="/" mode="*"/>
      <complexcat>
        <xsl:copy-of select="$s.E2.ng"/>
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.2.X"/>
      </complexcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="comma.pred.adj">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$s.1.E.default"/>
      <slash dir="/" mode="*"/>
      <xsl:copy-of select="$pred.adj"/>
    </complexcat>
  </xsl:variable>

  <family name="Comma" pos="," closed="true">
    <entry name="Primary" indexRel="*NoSem*">
      <xsl:copy-of select="$punct.comma"/>
    </entry>
    <entry name="Sentential" indexRel="Next">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($comma.conj.sentential)/*"/>
        <xsl:with-param name="ext" select="$L1.elem.Item.E1.Next.L2"/>
      </xsl:call-template>
    </entry>
    <entry name="NP" indexRel="Next">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($comma.conj.np)/*"/>
        <xsl:with-param name="ext" select="$L1.elem.Item.X1.Next.L2"/>
      </xsl:call-template>
    </entry>
  </family>

  <family name="Comma-Elab" pos="," closed="true">
    <entry name="VP-ng">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($comma.vp.ng)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Core.E.Trib.E2"/>
      </xsl:call-template>
    </entry>
    <entry name="Pred-Adj">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($comma.pred.adj)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Core.E.Trib.P"/>
      </xsl:call-template>
    </entry>
  </family>
  
  
  <!-- End Punctuation -->  
  <xsl:variable name="fullstop.dcl-base">
    <complexcat>
      <xsl:copy-of select="$sent.E"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$s.1.E.dcl-base.phr"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="fullstop.vp.ng">
    <complexcat>
      <xsl:copy-of select="$sent.E"/>
      <slash dir="\" mode="*"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E.ng.phr"/>
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.X"/>
      </complexcat>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="E.mood.dcl">
    <lf>
      <satop nomvar="E:sem-obj">
        <diamond mode="mood"><prop name="dcl"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="FullStop" pos="." closed="true" indexRel="mood">
    <entry name="Declare-Dcl-or-Fronted">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($fullstop.dcl-base)/*"/>
        <xsl:with-param name="ext" select="$E.mood.dcl"/>
      </xsl:call-template>
    </entry>
    <entry name="Declare-VP-Ng">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($fullstop.vp.ng)/*"/>
        <xsl:with-param name="ext" select="$E.mood.dcl"/>
      </xsl:call-template>
    </entry>
  </family>

  <xsl:variable name="qmark.q-base">
    <complexcat>
      <xsl:copy-of select="$sent.E"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$s.1.E.q-base.phr"/>
    </complexcat>
  </xsl:variable>
  
  <!-- NB: this doesn't require an -ing VP -->
  <xsl:variable name="qmark.vp.wh">
    <complexcat>
      <xsl:copy-of select="$sent.E"/>
      <slash dir="\" mode="*"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E.wh.phr"/>
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.X"/>
      </complexcat>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="E.mood.int">
    <lf>
      <satop nomvar="E:sem-obj">
        <diamond mode="mood"><prop name="int"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="QuestionMark" pos="?" closed="true" indexRel="mood">
    <entry name="Ask-Q-or-Wh">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($qmark.q-base)/*"/>
        <xsl:with-param name="ext" select="$E.mood.int"/>
      </xsl:call-template>
    </entry>
    <entry name="Ask-VP-Wh">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($qmark.vp.wh)/*"/>
        <xsl:with-param name="ext" select="$E.mood.int"/>
      </xsl:call-template>
    </entry>
  </family>

  </xsl:template>

</xsl:transform>


