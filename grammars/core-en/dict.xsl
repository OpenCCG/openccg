<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.23 $, $Date: 2005/11/11 22:01:20 $ 

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

  
  <!-- ***** Core Entries ***** -->
  <xsl:template name="add-entries">

  <!-- Dummy entry, for establishing features for a category -->
  <entry stem="*dummy*" pos="Dummy">
    <member-of family="Dummy"/>
  </entry>

  <!-- Generic entries for dates, times, numbers, amounts, durations -->
  <entry stem="[*DATE*]" pos="NNP" class="date"/>
  <entry stem="[*TIME*]" pos="NNP" class="time"/>
  <entry stem="[*NUM*]" pos="Num" class="num" macros="@pl-2"/>
  <entry stem="[*AMT*]" pos="NNP" class="amt"/>
  <entry stem="[*DUR*]" pos="NNP" class="dur"/>
  
  <!-- Boundary Tones -->
  <entry stem="L" pos="BT">
    <member-of family="BoundaryTone-L-LL%"/>
  </entry>
  <entry stem="LL%" pos="BT">
    <member-of family="BoundaryTone-L-LL%"/>
  </entry>
  <entry stem="LH%" pos="BT">
    <member-of family="BoundaryTone-LH%"/>
  </entry>
  <entry stem="HH%" pos="BT">
    <member-of family="BoundaryTone-HH%"/>
  </entry>
  
  <!-- Punctuation -->
  <entry stem="," pos=",">
    <member-of family="Comma"/>
    <member-of family="Comma-Elab" pred="elab-rel"/>
  </entry>
  <entry stem="." pos=".">
    <member-of family="FullStop"/>
  </entry>
  <entry stem="?" pos="?">
    <member-of family="QuestionMark"/>
  </entry>

  <!-- Complementizer -->
  <entry stem="that" pos="Comp">
    <member-of family="Comp"/>
  </entry>

  <!-- Relative Pronoun -->
  <entry stem="that" pos="RelPro">
    <member-of family="RelPro"/>
    <member-of family="RelPro-Appos" pred="elab-rel"/>
  </entry>

  <!-- Personal Pronouns, including possessives -->
  <entry stem="pro1" pos="Pro" class="animate-being" macros="@1st">
    <member-of family="ProNP"/>
    <word form="I" macros="@sg @nom"/>
    <word form="me" macros="@sg @acc"/>
    <word form="we" macros="@pl @nom"/>
    <word form="us" macros="@pl @acc"/>
  </entry>
  <entry stem="pro1" pos="PossPro" class="animate-being">
    <member-of family="PossPro"/>
    <word form="my" macros="@sg-P"/>
    <word form="our" macros="@pl-P"/>
  </entry>
  <entry word="you" stem="pro2" pos="Pro" class="animate-being" macros="@2nd">
    <member-of family="ProNP"/>
  </entry>
  <entry word="your" stem="pro2" pos="PossPro" class="animate-being">
    <member-of family="PossPro"/>
  </entry>
  <entry stem="pro3f" pos="Pro" macros="@3rd">
    <member-of family="ProNP"/>
    <word form="she" macros="@sg @nom"/>
    <word form="her" macros="@sg @acc"/>
  </entry>
  <entry word="her" stem="pro3f" pos="PossPro" macros="@sg-P">
    <member-of family="PossPro"/>
  </entry>
  <entry stem="pro3m" pos="Pro" macros="@3rd">
    <member-of family="ProNP"/>
    <word form="he" macros="@sg @nom"/>
    <word form="him" macros="@sg @acc"/>
  </entry>
  <entry word="his" stem="pro3m" pos="PossPro" macros="@sg-P">
    <member-of family="PossPro"/>
  </entry>
  <entry stem="pro3n" pos="Pro" macros="@3rd">
    <member-of family="ProNP"/>
    <word form="it" macros="@sg"/>
    <word form="they" macros="@pl @nom"/>
    <word form="them" macros="@pl @acc"/>
  </entry>
  <entry stem="pro3n" pos="PossPro">
    <member-of family="PossPro"/>
    <word form="its" macros="@sg-P"/>
    <word form="their" macros="@pl-P"/>
  </entry>

  <!-- Demonstrative Pronouns -->
  <entry stem="this" pos="Pro" macros="@3rd">
    <member-of family="ProNP"/>
    <word form="this" macros="@sg"/>
    <word form="these" macros="@pl"/>
  </entry>
  
  <entry stem="that" pos="Pro" macros="@3rd">
    <member-of family="ProNP"/>
    <word form="that" macros="@sg"/>
    <word form="those" macros="@pl"/>
  </entry>
  
  <!-- Pro-One -->
  <entry stem="pro_one" pos="N">
    <word form="one" macros="@sg"/>
    <word form="ones" macros="@pl"/>
  </entry>
  
  <!-- Expletive NPs -->
  <entry stem="it" pos="Expl">
    <member-of family="ExplNP"/>
  </entry>
  <entry stem="there" pos="Expl">
    <member-of family="ExplNP"/>
  </entry>
  <entry stem="here" pos="Expl">
    <member-of family="ExplNP"/>
  </entry>
  
  <!-- Conjunctions -->
  <entry stem="and" pos="Conj">
    <member-of family="Conj"/>
    <stem-for family="Conj" entry="NP-Collective"/>
    <stem-for family="Conj" entry="NP-Distributive-and"/>
    <member-of family="Subconj-Initial-Only"/>
  </entry>
  <entry stem="or" pos="Conj">
    <member-of family="Conj"/>
    <stem-for family="Conj" entry="NP-Distributive-or"/>
    <member-of family="Subconj-Initial-Only"/>
  </entry>
  <entry stem="but" pos="Conj">
    <member-of family="Conj-Sentential-Binary"/>
    <member-of family="Subconj-Initial-Only"/>
    <member-of family="Subconj-Medial"/>
  </entry>
  <entry stem="although" pos="Conj">
    <member-of family="Subconj-Initial"/>
  </entry>
  <entry stem="if" pos="Conj">
    <member-of family="Subconj-Initial"/>
  </entry>
  <entry stem="while" pos="Conj">
    <member-of family="Subconj-Medial"/>
  </entry>

  <!-- Quantifiers -->
  <entry stem="everyone" pred="every" pos="QNP" macros="@sg-2">
    <member-of family="QuantNP"/>
  </entry>
  <entry stem="both" pos="QNP" macros="@pl-2">
    <member-of family="QuantNP"/>
  </entry>
  <entry stem="neither" pos="QNP" macros="@sg-2">
    <member-of family="QuantNP"/>
  </entry>
  <entry stem="none" pos="QNP" macros="@pl-2">
    <member-of family="QuantNP"/>
  </entry>
  <entry stem="all" pos="QNP" macros="@pl-or-mass-2">
    <member-of family="QuantNP"/>
  </entry>

  <!-- Wh words -->
  <entry stem="what" pos="WhNP">
    <member-of family="WhNP"/>
  </entry>
  <entry stem="where" pos="WhNP" macros="@X-location">
    <member-of family="WhNP"/>
  </entry>
  <entry stem="who" pos="WhNP" macros="@X-person">
    <member-of family="WhNP"/>
  </entry>

  <!-- Simple determiners -->
  <entry stem="a" pos="Det" macros="@sg-2">
    <member-of family="Det"/>
    <word form="a"/>
    <word form="an"/>
  </entry>
  <entry stem="some" pos="Det">
    <member-of family="Det"/>
  </entry>
  <entry stem="the" pos="Det">
    <member-of family="Det"/>
  </entry>

  <!-- Predicational determiners -->
  <entry stem="another" pos="PDet" macros="@sg-2">
    <member-of family="PDet"/>
  </entry>
  <entry stem="this" pos="PDet">
    <member-of family="PDet"/>
    <word form="this" macros="@sg-or-mass-2"/>
    <word form="these" macros="@pl-2"/>
  </entry>
  <entry stem="that" pos="PDet">
    <member-of family="PDet"/>
    <word form="that" macros="@sg-or-mass-2"/>
    <word form="those" macros="@pl-2"/>
  </entry>
  
  <!-- Possessive 's -->
  <entry stem="'s" pos="PossS">
    <member-of family="PossS"/>
  </entry>

  <!-- Quant dets -->
  <entry stem="all" pos="QDet" macros="@pl-or-mass-2">
    <member-of family="QuantDet"/>
  </entry>
  <entry stem="both" pos="QDet" macros="@pl-2">
    <member-of family="QuantDet"/>
  </entry>
  <entry stem="every" pos="QDet" macros="@sg-2">
    <member-of family="QuantDet"/>
  </entry>
  <entry stem="neither" pos="QDet" macros="@sg-2">
    <member-of family="QuantDet"/>
  </entry>
  <entry stem="no" pos="QDet">
    <member-of family="QuantDet"/>
  </entry>

  <!-- Wh dets -->
  <entry stem="what" pos="Wh">
    <member-of family="WhDet"/>
  </entry>
  <entry stem="which" pos="Wh">
    <member-of family="WhDet"/>
  </entry>

  <!-- Adverbs -->
  <entry stem="also" pos="Adv">
    <member-of family="Adverb"/>
  </entry>
  <entry stem="either" pos="Adv">
    <member-of family="Adverb"/>
  </entry>
  <entry stem="though" pos="Adv">
    <member-of family="Adverb"/>
  </entry>
  <entry stem="too" pos="Adv">
    <member-of family="Adverb"/>
  </entry>

  <!-- Prepositions -->
  <entry stem="with" pos="Prep">
    <member-of family="Prep-Nom"/>
    <member-of family="With-Poss"/>
  </entry>
  
  <!-- Modal verbs -->
  <entry stem="can" pos="V">
    <member-of family="Modal"/>
  </entry>
  <entry stem="may" pos="V">
    <member-of family="Modal"/>
  </entry>
  <entry stem="should" pos="V">
    <member-of family="Modal"/>
  </entry>
  <entry stem="would" pos="V">
    <member-of family="Modal"/>
    <word form="would"/>
    <word form="'d"/>
  </entry>
  <entry stem="will" pos="V">
    <member-of family="Modal"/>
    <word form="will"/>
    <word form="'ll"/>
  </entry>

  <!-- Not -->
  <entry stem="not" pos="V">
    <member-of family="Negation"/>
    <word form="not" macros="@base @base6"/>
    <word form="n't" macros="@base @base6"/>
    <word form="not" macros="@adj @adj6"/>
    <word form="n't" macros="@adj @adj6"/>
  </entry>
  
  <!-- Do -->
  <entry stem="do" pos="V">
    <member-of family="Modal"/>
    <member-of family="Do-Support"/>
    <word form="do" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="does" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="do" macros="@pres @pl-agr"/>
    <word form="did" macros="@past"/>
  </entry>

  <!-- Be -->
  <entry stem="be" pos="V">
    <member-of family="Copula"/>
    <member-of family="Progressive" pred="prog"/>
    <member-of family="ThereExistential" pred="there-be"/>
    <member-of family="HereExistential" pred="here-be"/>
    <word form="be" macros="@base" excluded="Inverted"/>
    <word form="am" macros="@pres @sg-agr @1st-agr"/>
    <word form="'m" macros="@pres @sg-agr @1st-agr"/>
    <word form="are" macros="@pres @sg-agr @2nd-agr"/>
    <word form="'re" macros="@pres @sg-agr @2nd-agr"/>
    <word form="is" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="'s" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="are" macros="@pres @pl-agr"/>
    <word form="'re" macros="@pres @pl-agr"/>
    <word form="was" macros="@past @sg-agr @1st-agr"/>
    <word form="were" macros="@past @sg-agr @2nd-agr"/>
    <word form="was" macros="@past @sg-or-mass-agr @3rd-agr"/>
    <word form="were" macros="@past @pl-agr"/>
  </entry>

  <!-- Have (aux use tbd) -->
  <entry stem="have" pos="V">
    <member-of family="Possession"/>
    <word form="have" macros="@base" excluded="Inverted"/>
    <word form="have" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="has" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="have" macros="@pres @pl-agr"/>
    <word form="had" macros="@past"/>
  </entry>

  <!-- Like -->
  <entry stem="like" pos="V">
    <member-of family="Experiencer-Subj"/>
    <word form="like" macros="@base"/>
    <word form="liking" macros="@ng"/>
    <word form="like" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="likes" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="like" macros="@pres @pl-agr"/>
    <word form="liked" macros="@past"/>
  </entry>

  <!-- Make -->
  <entry stem="make" pos="V">
    <member-of family="Agentive-Causing"/>
    <word form="make" macros="@base"/>
    <word form="making" macros="@ng"/>
    <word form="make" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="makes" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="make" macros="@pres @pl-agr"/>
    <word form="made" macros="@past"/>
  </entry>

  <!-- Mention -->
  <entry stem="mention" pos="V">
    <member-of family="Statement"/>
    <word form="mention" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="mentions" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="mention" macros="@pres @pl-agr"/>
    <word form="mentioned" macros="@past"/>
  </entry>

  <!-- Say -->
  <entry stem="say" pos="V">
    <member-of family="Statement"/>
    <word form="say" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="says" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="say" macros="@pres @pl-agr"/>
    <word form="said" macros="@past"/>
  </entry>

  <!-- Specific numbers -->
  <entry stem="one" pos="Num" class="num" macros="@sg-2">
    <member-of family="One-Det"/>
  </entry>
  <entry stem="1" pos="Num" class="num" macros="@sg-2">
    <member-of family="One-Det"/>
  </entry>
  <entry stem="several" pos="Num" class="num" macros="@pl-2"/>
  <entry stem="two" pos="Num" class="num" macros="@pl-2"/>
  <entry stem="three" pos="Num" class="num" macros="@pl-2"/>
  <entry stem="four" pos="Num" class="num" macros="@pl-2"/>
  <entry stem="five" pos="Num" class="num" macros="@pl-2"/>
  <entry stem="six" pos="Num" class="num" macros="@pl-2"/>
  <entry stem="seven" pos="Num" class="num" macros="@pl-2"/>
  <entry stem="eight" pos="Num" class="num" macros="@pl-2"/>
  <entry stem="nine" pos="Num" class="num" macros="@pl-2"/>
  <entry stem="ten" pos="Num" class="num" macros="@pl-2"/>
  
  </xsl:template>


  <!-- ***** Core Macros ***** -->
  <xsl:template name="add-macros">
  
  <macro name="@mass">
    <fs id="2" attr="num" val="mass"/>
  </macro>

  <macro name="@sg">
    <fs id="2" attr="num" val="sg"/>
    <lf>
      <satop nomvar="X">
        <diamond mode="num"><prop name="sg"/></diamond>
      </satop>
    </lf>
  </macro>

  <macro name="@sg-agr">
    <fs id="1" attr="num" val="sg"/>
    <fs id="2" attr="num" val="sg"/>
  </macro>

  <macro name="@sg-or-mass-agr">
    <fs id="1">
      <feat attr="num"><featvar name="NUM:sg-or-mass"/></feat>
    </fs>
    <fs id="2">
      <feat attr="num"><featvar name="NUM:sg-or-mass"/></feat>
    </fs>
  </macro>

  <macro name="@sg-2">
    <fs id="2" attr="num" val="sg"/>
  </macro>

  <macro name="@sg-or-mass-2">
    <fs id="2" attr="num" val="sg-or-mass"/>
  </macro>

  <macro name="@sg-P">
    <lf>
      <satop nomvar="P">
        <diamond mode="num"><prop name="sg"/></diamond>
      </satop>
    </lf>
  </macro>

  <macro name="@pl">
    <fs id="2" attr="num" val="pl"/>
    <lf>
      <satop nomvar="X">
        <diamond mode="num"><prop name="pl"/></diamond>
      </satop>
    </lf>
  </macro>

  <macro name="@pl-agr">
    <fs id="1" attr="num" val="pl"/>
    <fs id="2" attr="num" val="pl"/>
  </macro>

  <macro name="@pl-2">
    <fs id="2" attr="num" val="pl"/>
  </macro>

  <macro name="@pl-or-mass-2">
    <fs id="2" attr="num" val="pl-or-mass"/>
  </macro>

  <macro name="@pl-P">
    <lf>
      <satop nomvar="P">
        <diamond mode="num"><prop name="pl"/></diamond>
      </satop>
    </lf>
  </macro>

  <macro name="@1st">
    <fs id="2" attr="pers" val="1st"/>
  </macro>

  <macro name="@1st-agr">
    <fs id="1" attr="pers" val="1st"/>
    <fs id="2" attr="pers" val="1st"/>
  </macro>

  <macro name="@2nd">
    <fs id="2" attr="pers" val="2nd"/>
  </macro>

  <macro name="@2nd-agr">
    <fs id="1" attr="pers" val="2nd"/>
    <fs id="2" attr="pers" val="2nd"/>
  </macro>

  <macro name="@non-3rd-agr">
    <fs id="1">
      <feat attr="pers"><featvar name="PERS:non-3rd"/></feat>
    </fs>
    <fs id="2">
      <feat attr="pers"><featvar name="PERS:non-3rd"/></feat>
    </fs>
  </macro>

  <macro name="@3rd">
    <fs id="2" attr="pers" val="3rd"/>
  </macro>

  <macro name="@3rd-agr">
    <fs id="1" attr="pers" val="3rd"/>
    <fs id="2" attr="pers" val="3rd"/>
  </macro>

  <macro name="@nom">
    <fs id="2" attr="case" val="nom"/>
  </macro>

  <macro name="@acc">
    <fs id="2" attr="case" val="acc"/>
  </macro>

  <macro name="@pp-from">
    <fs id="3" attr="lex" val="from"/>
  </macro>

  <macro name="@pp-to">
    <fs id="3" attr="lex" val="to"/>
  </macro>

  <macro name="@prt-up">
    <fs id="4" attr="lex" val="up"/>
  </macro>

  <macro name="@prt-with">
    <fs id="4" attr="lex" val="with"/>
  </macro>

  <macro name="@ng">
    <fs id="1" attr="form" val="ng"/>
  </macro>

  <macro name="@base">
    <fs id="1" attr="form" val="base"/>
  </macro>

  <macro name="@base6">
    <fs id="6" attr="form" val="base"/>
  </macro>

  <macro name="@dcl">
    <fs id="1">
      <feat attr="form" val="dcl"/>
    </fs>
  </macro>

  <macro name="@adj">
    <fs id="1" attr="form" val="adj"/>
  </macro>

  <macro name="@adj6">
    <fs id="6" attr="form" val="adj"/>
  </macro>

  <macro name="@X-location">
    <fs id="2">
      <feat attr="index"><lf><nomvar name="X:location"/></lf></feat> 
    </fs>
  </macro>

  <macro name="@X-person">
    <fs id="2">
      <feat attr="index"><lf><nomvar name="X:person"/></lf></feat> 
    </fs>
  </macro>

  <macro name="@past">
    <lf>
      <satop nomvar="E">
        <diamond mode="tense"><prop name="past"/></diamond>
      </satop>
    </lf>
  </macro>

  <macro name="@pres">
    <lf>
      <satop nomvar="E">
        <diamond mode="tense"><prop name="pres"/></diamond>
      </satop>
    </lf>
  </macro>

  </xsl:template>

</xsl:transform>


