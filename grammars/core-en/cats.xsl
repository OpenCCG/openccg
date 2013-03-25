<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.25 $, $Date: 2008/12/11 17:42:31 $ 

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

  
  <!-- ***** Feature Declarations ******  -->

  <xsl:template name="add-feature-declarations">
    <distributive-features attrs="info owner"/>
    <licensing-features>
      <feat attr="form" val="emb" location="target-only"/>
      <feat attr="form" val="inf" location="target-only"/>
      <feat attr="form" val="adj" location="target-only"/>
      <feat attr="form" val="wh" also-licensed-by="q-base" license-marked-cats="true"/>
      <feat attr="form" val="q" also-licensed-by="q-base" license-marked-cats="true"/>
      <feat attr="form" val="q" also-licensed-by="q-base"/>
      <feat attr="owner" instantiate="false" location="args-only"/>
    </licensing-features>
  </xsl:template>

  
  <!-- ***** Base Categories ******  -->
  
  <!-- NB: The distributive attrs (info and owner) need only appear once per atomic category. -->

  <!-- n -->
  <xsl:variable name="n.2.X.default">
    <atomcat type="n">
      <fs id="2">
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="n.2.X.NUM">
    <atomcat type="n">
      <fs id="2">
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
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

  <xsl:variable name="n.2.W">
    <atomcat type="n">
      <fs id="2">
        <feat attr="index"><lf><nomvar name="W"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="n.1.from-2.X">
    <atomcat type="n">
      <fs id="1" inheritsFrom="2">
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="n.from-2.X.CASE">
    <atomcat type="n">
      <fs inheritsFrom="2">
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
        <feat attr="case"><featvar name="CASE"/></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  

  <!-- np -->
  <xsl:variable name="np.2.X.default">
    <atomcat type="np">
      <fs id="2">
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
        <feat attr="pers"><featvar name="PERS:pers-vals"/></feat>
        <feat attr="case"><featvar name="CASE"/></feat>
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.2.X.nom.default">
    <atomcat type="np">
      <fs id="2">
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
        <feat attr="pers"><featvar name="PERS:pers-vals"/></feat>
        <feat attr="case" val="nom"/>
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.2.X.NUM.3rd.CASE">
    <atomcat type="np">
      <fs id="2">
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
        <feat attr="pers" val="3rd"/>
        <feat attr="case"><featvar name="CASE"/></feat>
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.2.X.3rd">
    <atomcat type="np">
      <fs id="2">
        <feat attr="pers" val="3rd"/>
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.2.W.3rd">
    <atomcat type="np">
      <fs id="2">
        <feat attr="pers" val="3rd"/>
        <feat attr="index"><lf><nomvar name="W"/></lf></feat>
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

  <xsl:variable name="np.X">
    <atomcat type="np">
      <fs>
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.3.Y.acc">
    <atomcat type="np">
      <fs id="3">
        <feat attr="case" val="acc"/>
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.3.Y.gen">
    <atomcat type="np">
      <fs id="3">
        <feat attr="case" val="gen"/>
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.3.Y.3rd">
    <atomcat type="np">
      <fs id="3">
        <feat attr="pers" val="3rd"/>
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.Y">
    <atomcat type="np">
      <fs>
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="np.4.Z.acc">
    <atomcat type="np">
      <fs id="4">
        <feat attr="case" val="acc"/>
        <feat attr="index"><lf><nomvar name="Z"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="np.Z">
    <atomcat type="np">
      <fs>
        <feat attr="index"><lf><nomvar name="Z"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="np.X.CASE">
    <atomcat type="np">
      <fs>
        <feat attr="case"><featvar name="CASE"/></feat>
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.X1.CASE">
    <atomcat type="np">
      <fs>
        <feat attr="case"><featvar name="CASE"/></feat>
        <feat attr="index"><lf><nomvar name="X1"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.X2.CASE">
    <atomcat type="np">
      <fs>
        <feat attr="case"><featvar name="CASE"/></feat>
        <feat attr="index"><lf><nomvar name="X2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.Y.CASE_Y">
    <atomcat type="np">
      <fs>
        <feat attr="case"><featvar name="CASE_Y"/></feat>
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.Y1.CASE_Y">
    <atomcat type="np">
      <fs>
        <feat attr="case"><featvar name="CASE_Y"/></feat>
        <feat attr="index"><lf><nomvar name="Y1"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.Y2.CASE_Y">
    <atomcat type="np">
      <fs>
        <feat attr="case"><featvar name="CASE_Y"/></feat>
        <feat attr="index"><lf><nomvar name="Y2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np.from-1.S">
    <atomcat type="np">
      <fs inheritsFrom="1">
        <feat attr="index"><lf><nomvar name="S"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  
  <xsl:variable name="np_conj.default">
    <atomcat type="np_conj">
      <fs>
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
        <feat attr="pers"><featvar name="PERS:pers-vals"/></feat>
        <feat attr="case"><featvar name="CASE"/></feat>
        <feat attr="type"><featvar name="TYPE"/></feat>
        <feat attr="index"><lf><nomvar name="INDEX"/></lf></feat>
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np_conj.1.L1.op-index-S.pl.CASE.coll">
    <atomcat type="np_conj">
      <fs id="1">
        <feat attr="num" val="pl"/>
        <feat attr="case"><featvar name="CASE"/></feat>
        <feat attr="type" val="coll"/>
        <feat attr="index"><lf><nomvar name="L1"/></lf></feat>
        <feat attr="op-index"><lf><nomvar name="S"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np_conj.1.L1.op-index-S.pl.CASE.distr">
    <atomcat type="np_conj">
      <fs id="1">
        <feat attr="num" val="pl"/>
        <feat attr="case"><featvar name="CASE"/></feat>
        <feat attr="type" val="distr"/>
        <feat attr="index"><lf><nomvar name="L1"/></lf></feat>
        <feat attr="op-index"><lf><nomvar name="S"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np_conj.1.L1.op-index-S.NUM.CASE.distr">
    <atomcat type="np_conj">
      <fs id="1">
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
        <feat attr="case"><featvar name="CASE"/></feat>
        <feat attr="type" val="distr"/>
        <feat attr="index"><lf><nomvar name="L1"/></lf></feat>
        <feat attr="op-index"><lf><nomvar name="S"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np_conj.L1.op-index-S.NUM.CASE.distr.INFO.OWNER">
    <atomcat type="np_conj">
      <fs>
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
        <feat attr="case"><featvar name="CASE"/></feat>
        <feat attr="type" val="distr"/>
        <feat attr="index"><lf><nomvar name="L1"/></lf></feat>
        <feat attr="op-index"><lf><nomvar name="S"/></lf></feat>
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np_conj.from-1.L1.op-index-S">
    <atomcat type="np_conj">
      <fs inheritsFrom="1">
        <feat attr="index"><lf><nomvar name="L1"/></lf></feat>
        <feat attr="op-index"><lf><nomvar name="S"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="np_conj.1.L2.op-index-S.INFO.OWNER">
    <atomcat type="np_conj">
      <fs id="1">
        <feat attr="index"><lf><nomvar name="L2"/></lf></feat>
        <feat attr="op-index"><lf><nomvar name="S"/></lf></feat>
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  
  <xsl:variable name="np_expl.lex-Default">
    <atomcat type="np_expl">
      <fs>
        <feat attr="lex" val="[*DEFAULT*]"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np_expl.there">
    <atomcat type="np_expl">
      <fs>
        <feat attr="lex" val="there"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np_expl.here">
    <atomcat type="np_expl">
      <fs>
        <feat attr="lex" val="here"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="np_expl.it">
    <atomcat type="np_expl">
      <fs>
        <feat attr="lex" val="it"/>
      </fs>
    </atomcat>
  </xsl:variable>

  
  <!-- num -->
  <xsl:variable name="num.2.X.default">
    <atomcat type="num">
      <fs id="2">
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
        <feat attr="index"><lf><nomvar name="X"/></lf></feat>
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  
  <!-- p -->
  <xsl:variable name="pp.from-3.lex-Default">
    <atomcat type="pp">
      <fs inheritsFrom="3">
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

  <xsl:variable name="pp.3.Y.about">
    <atomcat type="pp">
      <fs id="3">
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
        <feat attr="lex" val="about"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="pp.3.Y.at">
    <atomcat type="pp">
      <fs id="3">
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
        <feat attr="lex" val="at"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="pp.3.Y.for">
    <atomcat type="pp">
      <fs id="3">
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
        <feat attr="lex" val="for"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="pp.3.Y.from">
    <atomcat type="pp">
      <fs id="3">
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
        <feat attr="lex" val="from"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="pp.3.Y.in">
    <atomcat type="pp">
      <fs id="3">
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
        <feat attr="lex" val="in"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="pp.3.Y.of">
    <atomcat type="pp">
      <fs id="3">
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
        <feat attr="lex" val="of"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="pp.3.Y.on">
    <atomcat type="pp">
      <fs id="3">
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
        <feat attr="lex" val="on"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="pp.3.Y.to">
    <atomcat type="pp">
      <fs id="3">
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
        <feat attr="lex" val="to"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="pp.3.Y.with">
    <atomcat type="pp">
      <fs id="3">
        <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
        <feat attr="lex" val="with"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="pp.4.Z.in">
    <atomcat type="pp">
      <fs id="4">
        <feat attr="index"><lf><nomvar name="Z"/></lf></feat>
        <feat attr="lex" val="in"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="pp.4.Z.on">
    <atomcat type="pp">
      <fs id="4">
        <feat attr="index"><lf><nomvar name="Z"/></lf></feat>
        <feat attr="lex" val="on"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="pp.4.Z.to">
    <atomcat type="pp">
      <fs id="4">
        <feat attr="index"><lf><nomvar name="Z"/></lf></feat>
        <feat attr="lex" val="to"/>
      </fs>
    </atomcat>
  </xsl:variable>

  
  <!-- punct -->
  <xsl:variable name="punct.comma">
    <atomcat type="punct">
      <fs>
        <feat attr="lex" val=","/>
      </fs>
    </atomcat>
  </xsl:variable>
  

  <xsl:variable name="bt.LH">
    <atomcat type="bt">
      <fs>
        <feat attr="lex" val="LH%"/>
      </fs>
    </atomcat>
  </xsl:variable>
  

  <!-- s -->
  <xsl:variable name="s.1.E.default">
    <atomcat type="s">
      <fs id="1">
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
        <feat attr="pers"><featvar name="PERS:pers-vals"/></feat>
        <feat attr="form"><featvar name="FORM:form-vals"/></feat>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.1.E.dcl.default">
    <atomcat type="s">
      <fs id="1">
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
        <feat attr="pers"><featvar name="PERS:pers-vals"/></feat>
        <feat attr="form" val="dcl"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.1.E.q.default">
    <atomcat type="s">
      <fs id="1">
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
        <feat attr="pers"><featvar name="PERS:pers-vals"/></feat>
        <feat attr="form" val="q"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.1.E.dcl">
    <atomcat type="s">
      <fs id="1">
        <feat attr="form" val="dcl"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.1.E.dcl-base.phr">
    <atomcat type="s">
      <fs id="1">
        <feat attr="form" val="dcl-base"/>
        <feat attr="info" val="phr"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.1.E.ng.phr">
    <atomcat type="s">
      <fs id="1">
        <feat attr="form" val="ng"/>
        <feat attr="info" val="phr"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.1.E.base">
    <atomcat type="s">
      <fs id="1">
        <feat attr="form" val="base"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.1.E.wh">
    <atomcat type="s">
      <fs id="1">
        <feat attr="form" val="wh"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.1.E.q">
    <atomcat type="s">
      <fs id="1">
        <feat attr="form" val="q"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.1.E.q-base.phr">
    <atomcat type="s">
      <fs id="1">
        <feat attr="form" val="q-base"/>
        <feat attr="info" val="phr"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.1.E.wh.phr">
    <atomcat type="s">
      <fs id="1">
        <feat attr="form" val="wh"/>
        <feat attr="info" val="phr"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.1.from-2.E.q">
    <atomcat type="s">
      <fs id="1" inheritsFrom="2">
        <feat attr="form" val="q"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.1.from-6.E">
    <atomcat type="s">
      <fs id="1" inheritsFrom="6">
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

  <xsl:variable name="s.1.E1">
    <atomcat type="s">
      <fs id="1">
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
  
  <xsl:variable name="s.1.INFO.s">
    <atomcat type="s">
      <fs id="1">
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
        <feat attr="owner"><lf><prop name="s"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.1.INFO.h">
    <atomcat type="s">
      <fs id="1">
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
        <feat attr="owner"><lf><prop name="h"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.1.q.INFO.h">
    <atomcat type="s">
      <fs id="1">
        <feat attr="form" val="q"/>
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
        <feat attr="owner"><lf><prop name="h"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.1">
    <atomcat type="s">
      <fs id="1"/>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.2.E.base">
    <atomcat type="s">
      <fs id="2">
        <feat attr="form" val="base"/>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.2.E2">
    <atomcat type="s">
      <fs id="2">
        <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.2.E2.base">
    <atomcat type="s">
      <fs id="2">
        <feat attr="form" val="base"/>
        <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.2.E2.ng">
    <atomcat type="s">
      <fs id="2">
        <feat attr="form" val="ng"/>
        <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.2.P.adj">
    <atomcat type="s">
      <fs id="2">
        <feat attr="form" val="adj"/>
        <feat attr="index"><lf><nomvar name="P"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.6.E2">
    <atomcat type="s">
      <fs id="6">
        <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.E2.dcl">
    <atomcat type="s">
      <fs>
        <feat attr="form" val="dcl"/>
        <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.E2.emb">
    <atomcat type="s">
      <fs>
        <feat attr="form" val="emb"/>
        <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.E2.base">
    <atomcat type="s">
      <fs>
        <feat attr="form" val="base"/>
        <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.E2.ng">
    <atomcat type="s">
      <fs>
        <feat attr="form" val="ng"/>
        <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.E2.inf">
    <atomcat type="s">
      <fs>
        <feat attr="form" val="inf"/>
        <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.P.adj">
    <atomcat type="s">
      <fs>
        <feat attr="form" val="adj"/>
        <feat attr="index"><lf><nomvar name="P"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.P.ng">
    <atomcat type="s">
      <fs>
        <feat attr="form" val="ng"/>
        <feat attr="index"><lf><nomvar name="P"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.P2.adj">
    <atomcat type="s">
      <fs>
        <feat attr="form" val="adj"/>
        <feat attr="index"><lf><nomvar name="P2"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>
  
  <xsl:variable name="s.from-1.adj">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="form" val="adj"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.from-1.emb">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="form" val="emb"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.from-1.fronted">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="form" val="fronted"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.from-1.inf">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="form" val="inf"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.from-1.S">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="index"><lf><nomvar name="S"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.from-1.Q.wh">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="form" val="wh"/>
        <feat attr="index"><lf><nomvar name="Q"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.from-1.E1.NUM.PERS">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
        <feat attr="pers"><featvar name="PERS:pers-vals"/></feat>
        <feat attr="index"><lf><nomvar name="E1"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s.from-1.phr.nil">
    <atomcat type="s">
      <fs inheritsFrom="1">
        <feat attr="info" val="phr"/>
        <feat attr="owner" val="nil"/>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s_conj.default">
    <atomcat type="s_conj">
      <fs>
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
        <feat attr="pers"><featvar name="PERS:pers-vals"/></feat>
        <feat attr="form"><featvar name="FORM:form-vals"/></feat>
        <feat attr="index"><lf><nomvar name="INDEX"/></lf></feat>
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
        <feat attr="op-index"><lf><nomvar name="OP-INDEX"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s_conj.from-1.L1.op-index-S.NUM.PERS">
    <atomcat type="s_conj">
      <fs inheritsFrom="1">
        <feat attr="num"><featvar name="NUM:num-vals"/></feat>
        <feat attr="pers"><featvar name="PERS:pers-vals"/></feat>
        <feat attr="index"><lf><nomvar name="L1"/></lf></feat>
        <feat attr="op-index"><lf><nomvar name="S"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s_conj.1.L1.op-index-S">
    <atomcat type="s_conj">
      <fs id="1">
        <feat attr="index"><lf><nomvar name="L1"/></lf></feat>
        <feat attr="op-index"><lf><nomvar name="S"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s_conj.1.L1.op-index-S.adj">
    <atomcat type="s_conj">
      <fs id="1">
        <feat attr="form" val="adj"/>
        <feat attr="index"><lf><nomvar name="L1"/></lf></feat>
        <feat attr="op-index"><lf><nomvar name="S"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  <xsl:variable name="s_conj.1.L2.op-index-S">
    <atomcat type="s_conj">
      <fs id="1">
        <feat attr="index"><lf><nomvar name="L2"/></lf></feat>
        <feat attr="op-index"><lf><nomvar name="S"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  
  <!-- sent -->
  <xsl:variable name="sent.E">
    <atomcat type="sent">
      <fs>
        <feat attr="index"><lf><nomvar name="E"/></lf></feat>
      </fs>
    </atomcat>
  </xsl:variable>

  
  <!-- VP (ie S\NP) -->
  <xsl:variable name="vp.1.E.2.X">
    <complexcat>
      <xsl:copy-of select="$s.1.E"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np.2.X"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="vp.E2.inf">
    <complexcat>
      <xsl:copy-of select="$s.E2.inf"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np.2.X"/>
    </complexcat>
  </xsl:variable>

  <!-- Predicative forms -->
  <xsl:variable name="pred.adj">    
    <complexcat>
      <xsl:copy-of select="$s.P.adj"/>
      <slash dir="\" mode="&lt;" ability="inert"/>
      <xsl:copy-of select="$np.2.X"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="pred.adj.Y">    
    <complexcat>
      <xsl:copy-of select="$s.P.adj"/>
      <slash dir="\" mode="&lt;" ability="inert"/>
      <xsl:copy-of select="$np.Y"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="pred.adj.P2.Y">    
    <complexcat>
      <xsl:copy-of select="$s.P2.adj"/>
      <slash dir="\" mode="&lt;" ability="inert"/>
      <xsl:copy-of select="$np.Y"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="pred.ng">    
    <complexcat>
      <xsl:copy-of select="$s.P.ng"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np.2.X"/>
    </complexcat>
  </xsl:variable>


  
  <!-- ***** Shared LFs ******  -->
  
  <!-- Rhetorical Relations -->
  <xsl:variable name="S.Default.Core.E.Trib.E2">
    <lf>
      <satop nomvar="S:proposition">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="info"><var name="INFO"/></diamond>
        <diamond mode="owner"><var name="OWNER"/></diamond>
        <diamond mode="kon"><prop name="-"/></diamond>
        <diamond mode="Core"><nomvar name="E:situation"/></diamond>
        <diamond mode="Trib"><nomvar name="E2:situation"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="S.purpose-rel.Core.E.Trib.E2">
    <lf>
      <satop nomvar="S:proposition">
        <prop name="purpose-rel"/>
        <diamond mode="info"><var name="INFO"/></diamond>
        <diamond mode="owner"><var name="OWNER"/></diamond>
        <diamond mode="kon"><prop name="-"/></diamond>
        <diamond mode="Core"><nomvar name="E:situation"/></diamond>
        <diamond mode="Trib"><nomvar name="E2:situation"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="S.Default.Core.E.Trib.P">
    <lf>
      <satop nomvar="S:proposition">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="info"><var name="INFO"/></diamond>
        <diamond mode="owner"><var name="OWNER"/></diamond>
        <diamond mode="kon"><prop name="-"/></diamond>
        <diamond mode="Core"><nomvar name="E:situation"/></diamond>
        <diamond mode="Trib"><nomvar name="P:situation"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="S.elab-rel.Core.E.Trib.P">
    <lf>
      <satop nomvar="S:proposition">
        <prop name="elab-rel"/>
        <diamond mode="info"><var name="INFO"/></diamond>
        <diamond mode="owner"><var name="OWNER"/></diamond>
        <diamond mode="kon"><prop name="-"/></diamond>
        <diamond mode="Core"><nomvar name="E:situation"/></diamond>
        <diamond mode="Trib"><nomvar name="P:situation"/></diamond>
      </satop>
    </lf>
  </xsl:variable>


  
  <!-- ***** Dummy family, for establishing features for a category ******  -->

  <xsl:template name="add-dummy-family">
    <family name="Dummy" pos="Dummy" closed="true">
      <entry name="np_conj">
        <xsl:copy-of select="$np_conj.default"/>
      </entry>
      <entry name="s_conj">
        <xsl:copy-of select="$s_conj.default"/>
      </entry>
    </family>
  </xsl:template>
  
</xsl:transform>


