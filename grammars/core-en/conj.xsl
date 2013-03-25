<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.9 $, $Date: 2008/12/11 17:42:31 $ 

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

  <!-- NP coord with 'and' sets num to pl; with 'or', num is unconstrained -->
  <xsl:variable name="bw.np.X1.CASE.bw.bt.fw.np.X2.CASE">
    <slash dir="\" mode="*"/>
    <xsl:copy-of select="$np.X1.CASE"/>
    <slash dir="\" mode="*"/>
    <xsl:copy-of select="$bt.LH"/>
    <slash dir="/" mode="*"/>
    <xsl:copy-of select="$np.X2.CASE"/>
  </xsl:variable>
  
  <xsl:variable name="conj.np.pl.coll">
    <complexcat>
      <xsl:copy-of select="$np_conj.1.L1.op-index-S.pl.CASE.coll"/>
      <xsl:copy-of select="$bw.np.X1.CASE.bw.bt.fw.np.X2.CASE"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="conj.np.pl.distr">
    <complexcat>
      <xsl:copy-of select="$np_conj.1.L1.op-index-S.pl.CASE.distr"/>
      <xsl:copy-of select="$bw.np.X1.CASE.bw.bt.fw.np.X2.CASE"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="conj.np.NUM.distr">
    <complexcat>
      <xsl:copy-of select="$np_conj.1.L1.op-index-S.NUM.CASE.distr"/>
      <xsl:copy-of select="$bw.np.X1.CASE.bw.bt.fw.np.X2.CASE"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="S.Default.Last.L_N.L1.elem.Item.X1.Next.L2.elem.Item.X2.EqL.L_N">
    <lf>
      <satop nomvar="S:sem-obj">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Last"><nomvar name="L_N:struct"/></diamond>
      </satop>
      <satop nomvar="L1:struct">
        <prop name="elem"/>
        <diamond mode="Item"><nomvar name="X1:sem-obj"/></diamond>
        <diamond mode="Next">
          <nomvar name="L2:struct"/>
          <prop name="elem"/>
          <diamond mode="Item"><nomvar name="X2:sem-obj"/></diamond>
          <diamond mode="EqL"><nomvar name="L_N:struct"/></diamond>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>

  <!-- sentential coord -->
  <xsl:variable name="conj.sentential.result">
    <xsl:copy-of select="$s_conj.from-1.L1.op-index-S.NUM.PERS"/>
    <slash/>
    <dollar name="1"/>
  </xsl:variable>
  
  <xsl:variable name="conj.sentential.left-arg">
    <slash dir="\" mode="*"/>
    <complexcat>
      <xsl:copy-of select="$s.from-1.E1.NUM.PERS"/>
      <slash/>
      <dollar name="1"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="conj.sentential.right-arg">
    <slash dir="/" mode="*"/>
    <complexcat>
      <xsl:copy-of select="$s.1.E2"/>
      <slash/>
      <dollar name="1"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="conj.sentential">
    <complexcat>
      <xsl:copy-of select="$conj.sentential.result"/>
      <xsl:copy-of select="$conj.sentential.left-arg"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$bt.LH"/>
      <xsl:copy-of select="$conj.sentential.right-arg"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="S.Default.Last.L_N.L1.elem.Item.E1.Next.L2.elem.Item.E2.EqL.L_N">
    <lf>
      <satop nomvar="S:situation">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Last"><nomvar name="L_N:struct"/></diamond>
      </satop>
      <satop nomvar="L1:struct">
        <prop name="elem"/>
        <diamond mode="Item"><nomvar name="E1:situation"/></diamond>
        <diamond mode="Next">
          <nomvar name="L2:struct"/>
          <prop name="elem"/>
          <diamond mode="Item"><nomvar name="E2:situation"/></diamond>
          <diamond mode="EqL"><nomvar name="L_N:struct"/></diamond>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>

  <!-- initial, subordinating conj only (this doesn't capture the true relational semantics) -->
  <xsl:variable name="subconj.initial.only">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S"/>
      <slash dir="/" mode="*"/>
      <xsl:copy-of select="$s.1.E2"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="subconj.initial.vp.only">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S"/>
      <slash dir="/" mode="*"/>
      <complexcat>
        <xsl:copy-of select="$s.1.E2"/>
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.X"/>
      </complexcat>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="S.Default.Arg.E2">
    <lf>
      <satop nomvar="S:situation">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Arg">
          <nomvar name="E2:situation"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>


  <!-- Sentential List completion -->
  <xsl:variable name="s-list">
    <typechanging name="s-list">
      <arg>
        <complexcat>
          <xsl:copy-of select="$s_conj.1.L1.op-index-S"/>
          <slash/>
          <dollar name="1"/>
        </complexcat>
      </arg>
      <result>
        <complexcat>
          <xsl:copy-of select="$s.from-1.S"/>
          <slash/>
          <dollar name="1"/>
          <lf>
            <satop nomvar="S:situation">
              <diamond mode="First">
                <nomvar name="L1:struct"/>
              </diamond>
            </satop>
          </lf>
        </complexcat>
      </result>
    </typechanging>
  </xsl:variable>
  
  <!-- Pred-Adj List completion and conversion to prenominal adj -->
  <!-- NB: This is too general; should use subtypes to restrict applicability -->
  <xsl:variable name="pred-adj-list-to-adj">
    <typechanging name="pred-adj-list-to-adj">
      <arg>
        <complexcat>
          <xsl:copy-of select="$s_conj.1.L1.op-index-S.adj"/>
          <slash dir="\"/>
          <xsl:copy-of select="$np.2.X"/>
        </complexcat>
      </arg>
      <result>
        <xsl:call-template name="extend">
          <xsl:with-param name="elt" select="xalan:nodeset($adj)/*"/>
          <xsl:with-param name="ext">
            <lf>
              <satop nomvar="X:sem-obj">
                <diamond mode="GenRel">
                  <nomvar name="S:proposition"/>
                  <diamond mode="First"><nomvar name="L1:struct"/></diamond>
                </diamond>
              </satop>
            </lf>
          </xsl:with-param>
        </xsl:call-template>
      </result>
    </typechanging>
  </xsl:variable>
  

  <!-- NP List completion -->
  <xsl:variable name="np-list-c">
    <typechanging name="np-list-c">
      <arg>
        <xsl:copy-of select="$np_conj.1.L1.op-index-S.pl.CASE.coll"/>
      </arg>
      <result>
        <xsl:call-template name="extend">
          <xsl:with-param name="elt" select="xalan:nodeset($np.from-1.S)/*"/>
          <xsl:with-param name="ext">
            <lf>
              <satop nomvar="S:sem-obj">
                <diamond mode="First">
                  <nomvar name="L1:struct"/>
                </diamond>
              </satop>
            </lf>
          </xsl:with-param>
        </xsl:call-template>
      </result>
    </typechanging>
  </xsl:variable>
  
  <xsl:variable name="S.First.L1.BoundVar.X.Pred.E">
    <lf>
      <satop nomvar="S:situation">
        <diamond mode="First">
          <nomvar name="L1:struct"/>
        </diamond>
        <diamond mode="BoundVar"><nomvar name="X:sem-obj"/></diamond>
        <diamond mode="Pred"><nomvar name="E:situation"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <xsl:variable name="np-list-d-rightward-TR">
    <typechanging name="np-list-d-&gt;T">
      <arg>
        <xsl:copy-of select="$np_conj.L1.op-index-S.NUM.CASE.distr.INFO.OWNER"/>
      </arg>
      <result>
        <xsl:call-template name="extend">
          <xsl:with-param name="elt" select="xalan:nodeset($qnp.rightward-TR)/*"/>
          <xsl:with-param name="ext" select="$S.First.L1.BoundVar.X.Pred.E"/>
        </xsl:call-template>
      </result>
    </typechanging>
  </xsl:variable>
  
  <xsl:variable name="np-list-d-leftward-TR">
    <typechanging name="np-list-d-&lt;T">
      <arg>
        <xsl:copy-of select="$np_conj.L1.op-index-S.NUM.CASE.distr.INFO.OWNER"/>
      </arg>
      <result>
        <xsl:call-template name="extend">
          <xsl:with-param name="elt" select="xalan:nodeset($qnp.leftward-TR)/*"/>
          <xsl:with-param name="ext" select="$S.First.L1.BoundVar.X.Pred.E"/>
        </xsl:call-template>
      </result>
    </typechanging>
  </xsl:variable>
  
  
  <!-- More sentential options -->
  <xsl:variable name="conj.sentential.binary">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S"/>
      <slash/>
      <dollar name="1"/>
      <xsl:copy-of select="$conj.sentential.left-arg"/>
      <xsl:copy-of select="$conj.sentential.right-arg"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="conj.sentential.s.vp">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$s.1.E1"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$punct.comma"/>
      <slash dir="/" mode="*"/>
      <complexcat>
        <xsl:copy-of select="$s.2.E2"/>
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.X"/>
      </complexcat>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="subconj.medial">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$s.from-1.E1.NUM.PERS"/>
      <slash dir="\" mode="*"/>
      <xsl:copy-of select="$punct.comma"/>
      <slash dir="/" mode="*"/>
      <xsl:copy-of select="$s.1.E2"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="subconj.initial">
    <complexcat>
      <xsl:copy-of select="$s.from-1.S"/>
      <slash dir="/" mode="*"/>
      <xsl:copy-of select="$s.1.E2"/>
      <slash dir="/" mode="*"/>
      <xsl:copy-of select="$punct.comma"/>
      <slash dir="/" mode="*"/>
      <xsl:copy-of select="$s.from-1.E1.NUM.PERS"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="S.Default.Arg1.E1.Arg2.E2">
    <lf>
      <satop nomvar="S:situation">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Arg1">
          <nomvar name="E1:situation"/>
        </diamond>
        <diamond mode="Arg2">
          <nomvar name="E2:situation"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="S.Default.Arg2.E2">
    <lf>
      <satop nomvar="S:situation">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Arg2">
          <nomvar name="E2:situation"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  
  <xsl:variable name="subconj.transitional">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($adv.transitional)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="*"/>
        <xsl:copy-of select="$s.E2.dcl"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="E.HasProp.S.Default.Arg.E2">
    <lf>
      <satop nomvar="E:situation">
        <diamond mode="HasProp">
          <nomvar name="S:situation"/>
          <prop name="[*DEFAULT*]"/>
          <diamond mode="Arg">
            <nomvar name="E2:situation"/>
          </diamond>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>

  
  <!-- ***** Conjunction Families ***** -->
  <xsl:template name="add-conj-families">

  <family name="Conj" pos="Conj" closed="true">
    <entry name="NP-Collective"> 
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($conj.np.pl.coll)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Last.L_N.L1.elem.Item.X1.Next.L2.elem.Item.X2.EqL.L_N"/>
      </xsl:call-template>
    </entry>
    <entry name="NP-Distributive-and">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($conj.np.pl.distr)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Last.L_N.L1.elem.Item.X1.Next.L2.elem.Item.X2.EqL.L_N"/>
      </xsl:call-template>
    </entry>
    <entry name="NP-Distributive-or">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($conj.np.NUM.distr)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Last.L_N.L1.elem.Item.X1.Next.L2.elem.Item.X2.EqL.L_N"/>
      </xsl:call-template>
    </entry>
    <entry name="Sentential">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($conj.sentential)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Last.L_N.L1.elem.Item.E1.Next.L2.elem.Item.E2.EqL.L_N"/>
      </xsl:call-template>
    </entry>
    <!-- deferring arg clusters and gapping for now -->
  </family>

  <family name="Conj-Sentential-Binary" pos="Conj" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($conj.sentential.binary)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Arg1.E1.Arg2.E2"/>
      </xsl:call-template>
    </entry>
    <entry name="S-VP">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($conj.sentential.s.vp)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Arg1.E1.Arg2.E2"/>
      </xsl:call-template>
    </entry>
  </family>

  <family name="Subconj-Initial-Only" pos="Conj" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($subconj.initial.only)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Arg2.E2"/>
      </xsl:call-template>
    </entry>
    <entry name="VP">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($subconj.initial.vp.only)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Arg2.E2"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <family name="Subconj-Medial" pos="Conj" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($subconj.medial)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Arg1.E1.Arg2.E2"/>
      </xsl:call-template>
    </entry>
  </family>

  <family name="Subconj-Initial" pos="Conj" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($subconj.initial)/*"/>
        <xsl:with-param name="ext" select="$S.Default.Arg1.E1.Arg2.E2"/>
      </xsl:call-template>
    </entry>
  </family>

  <family name="Subconj-Transitional" pos="Conj" closed="true">
    <entry name="Initial">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($subconj.transitional)/*"/>
        <xsl:with-param name="ext" select="$E.HasProp.S.Default.Arg.E2"/>
      </xsl:call-template>
    </entry>
  </family>

  </xsl:template>

</xsl:transform>


