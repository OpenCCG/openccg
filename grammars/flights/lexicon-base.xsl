<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-5 University of Edinburgh (Michael White)
$Revision: 1.92 $, $Date: 2008/12/12 05:09:29 $ 

See ../core-en/lexicon.xsl for comments re grammar.

The semantic roles are taken from FrameNet where possible.

NB: These namespace declarations seem to work with the version of Xalan 
    that comes with JDK 1.4.  In Xalan 2.5, the redirect namespace is 
    supposed to be declared as http://xml.apache.org/xalan/redirect, 
    but giving the classname (magically) seems to work.  
    With newer versions of Xalan, different namespace declarations may be required. 
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  xmlns:redirect="org.apache.xalan.lib.Redirect" 
  extension-element-prefixes="redirect"
  exclude-result-prefixes="xalan xalan2">

  
  <!-- ***** Import Core Lexicon Definitions ***** -->
  <xsl:import href="../core-en/lexicon.xsl"/>
  
  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  
  <!-- ***** Start Output Here ***** -->
  <xsl:template match="/">
  <ccg-lexicon name="flights" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation="../lexicon.xsd"
  >

  <!-- ***** Feature Declarations ******  -->
  <xsl:call-template name="add-feature-declarations"/>
  
  
  <!-- ***** Relation Sorting ******  -->
  <relation-sorting order=
    "BoundVar PairedWith
     Restr Body 
     Det Card Num 
     Arg Arg1 Arg2 Of
     Core Trib
     First Last List EqL
     Agent Cognizer Experiencer Fig FigInv Owner Perceiver
     Container ContainerInv Item Theme Traveler Vehicle
     *
     Beneficiary Chosen Ground Poss Possib Pred Prop Situation Sought
     Airline Cargo Category Goods Asset Duty Var
     Source Goal Path
     Location Date Duration Time Time1 Time2 TimeFrame TimeRel AmPm
     HasProp GenOwner
     GenRel Next"/>

     
  <!-- ***** Derived Categories and Families ***** -->
  <xsl:call-template name="add-core-families"/>
  
  <!-- 'and' as punctuation -->
  <xsl:variable name="and-punct">
    <atomcat type="punct"><fs attr="lex" val="and"/></atomcat>
  </xsl:variable>
  
  <family name="And-Punct" pos="and" closed="true" indexRel="*NoSem*">
    <entry name="Primary"><xsl:copy-of select="$and-punct"/></entry>
  </family>
    
  <!-- Path Nouns ('connection', 'stopover') -->
  <!-- NB: Since the Path rel is used with both 'in' and 'at', the 
           type of the argument NP (city/airport) actually makes a difference 
           to realization (though n-grams can easily make the choice too, 
           when the type info is not available). -->
  <xsl:variable name="n.in">
    <complexcat>    
      <xsl:copy-of select="$n.2.X"/>
      <slash dir="/" mode="&gt;"/>
      <xsl:copy-of select="$pp.3.Y.in"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="n.at">
    <complexcat>    
      <xsl:copy-of select="$n.2.X"/>
      <slash dir="/" mode="&gt;"/>
      <xsl:copy-of select="$pp.3.Y.at"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="X.Default.Path.Y-city">
    <lf>
      <satop nomvar="X:change">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Path"><nomvar name="Y:city"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="X.Default.Path.Y-airport">
    <lf>
      <satop nomvar="X:change">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Path"><nomvar name="Y:airport"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <family name="Noun-Path" pos="N" closed="true">
    <entry name="In-Path">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($n.in)/*"/>
        <xsl:with-param name="ext" select="$X.Default.Path.Y-city"/>
      </xsl:call-template>
    </entry>
    <entry name="At-Path">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($n.at)/*"/>
        <xsl:with-param name="ext" select="$X.Default.Path.Y-airport"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Travel Nouns ('flight'); more cases tbd; not sure about type -->
  <xsl:variable name="n.from">
    <complexcat>    
      <xsl:copy-of select="$n.2.X"/>
      <slash dir="/" mode="&gt;"/>
      <xsl:copy-of select="$pp.3.Y.from"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="n.to">
    <complexcat>    
      <xsl:copy-of select="$n.2.X"/>
      <slash dir="/" mode="&gt;"/>
      <xsl:copy-of select="$pp.3.Y.to"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="n.from.to">
    <complexcat>    
      <xsl:copy-of select="$n.2.X"/>
      <slash dir="/" mode="&gt;"/>
      <xsl:copy-of select="$pp.4.Z.to"/>
      <slash dir="/" mode="&gt;"/>
      <xsl:copy-of select="$pp.3.Y.from"/>
    </complexcat>
  </xsl:variable>
  
  <xsl:variable name="X.Default.Source.Y">
    <lf>
      <satop nomvar="X:sem-obj">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Source"><nomvar name="Y:location"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="X.Default.Goal.Y">
    <lf>
      <satop nomvar="X:sem-obj">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Goal"><nomvar name="Y:location"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="X.Default.Source.Y.Goal.Z">
    <lf>
      <satop nomvar="X:sem-obj">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Source"><nomvar name="Y:location"/></diamond>
        <diamond mode="Goal"><nomvar name="Z:location"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <family name="Noun-Travel" pos="N" closed="true">
    <entry name="PP-Source">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($n.from)/*"/>
        <xsl:with-param name="ext" select="$X.Default.Source.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="PP-Goal">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($n.to)/*"/>
        <xsl:with-param name="ext" select="$X.Default.Goal.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="PP-Source-PP-Goal">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($n.from.to)/*"/>
        <xsl:with-param name="ext" select="$X.Default.Source.Y.Goal.Z"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Category Nouns ('availability')-->
  <xsl:variable name="X.Default.Category.Y">
    <lf>
      <satop nomvar="X:sem-obj">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Category"><nomvar name="Y:fareclass"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  <family name="Noun-Category" pos="N" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($n.in)/*"/>
        <xsl:with-param name="ext" select="$X.Default.Category.Y"/>
      </xsl:call-template>
    </entry>
  </family>

  
  <!-- Duration Nouns ('travel_time')-->
  <xsl:variable name="X.Default.Duration.Y">
    <lf>
      <satop nomvar="X:sem-obj">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Duration"><nomvar name="Y:dur"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <xsl:variable name="n.of">
    <complexcat>    
      <xsl:copy-of select="$n.2.X"/>
      <slash dir="/" mode="&gt;"/>
      <xsl:copy-of select="$pp.3.Y.of"/>
    </complexcat>
  </xsl:variable>
  
  <family name="Noun-Duration" pos="N" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($n.of)/*"/>
        <xsl:with-param name="ext" select="$X.Default.Duration.Y"/>
      </xsl:call-template>
    </entry>
  </family>

  
  <!-- Container Adj ('full') -->
  <xsl:variable name="X.ContainerInv.P.Default">
    <lf>
      <satop nomvar="X:sem-obj">
        <diamond mode="ContainerInv">
          <nomvar name="P:proposition"/>
          <prop name="[*DEFAULT*]"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <xsl:variable name="pred.adj.pp.in">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($pred.adj)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$pp.3.Y.in"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="P.Default.Container.X">  
    <lf>
      <satop nomvar="P:proposition">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Container"><nomvar name="X:sem-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <xsl:variable name="P.Default.Container.X.Category.Y">  
    <lf>
      <satop nomvar="P:proposition">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Container"><nomvar name="X:sem-obj"/></diamond>
        <diamond mode="Category"><nomvar name="Y:fareclass"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <family name="Adj-Full" pos="Adj" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adj)/*"/>
        <xsl:with-param name="ext" select="$X.ContainerInv.P.Default"/>
      </xsl:call-template>
    </entry>
    <entry name="Predicative-Container">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($pred.adj)/*"/>
        <xsl:with-param name="ext" select="$P.Default.Container.X"/>
      </xsl:call-template>
    </entry>
    <entry name="Predicative-Container-PP-Category">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($pred.adj.pp.in)/*"/>
        <xsl:with-param name="ext" select="$P.Default.Container.X.Category.Y"/>
      </xsl:call-template>
    </entry>
  </family>


  <!-- TimeFrame adj -->
  <xsl:variable name="X.TimeFrame.P.Default">
    <lf>
      <satop nomvar="X:sem-obj">
        <diamond mode="TimeFrame">
          <nomvar name="P:timeframe"/>
          <prop name="[*DEFAULT*]"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Adj-TimeFrame" pos="Adj" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adj)/*"/>
        <xsl:with-param name="ext" select="$X.TimeFrame.P.Default"/>
      </xsl:call-template>
    </entry>
  </family>
  
  
  <!-- Willing Adj ('willing to'); cf. Wanting -->
  <!-- NB: need to make P a state (rather than experience) to be compatible with proposition, 
           as required by the copula -->
  <xsl:variable name="P.Default.Experiencer.X.Situation.E2">  
    <lf>
      <satop nomvar="P:state">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Experiencer"><nomvar name="X:animate-being"/></diamond>
        <diamond mode="Situation"><nomvar name="E2:situation"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <!-- Subject Control -->
  <xsl:variable name="pred-adj-control">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($pred.adj)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <complexcat>
          <xsl:copy-of select="$s.E2.inf"/>
          <slash dir="\"/>
          <xsl:copy-of select="$np.X"/>
        </complexcat>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <family name="Adj-Willing" pos="Adj" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($pred-adj-control)/*"/>
        <xsl:with-param name="ext" select="$P.Default.Experiencer.X.Situation.E2"/>
      </xsl:call-template>
    </entry>
  </family>

  
  <!-- Airline Adj -->
  <xsl:variable name="X.Airline.P.Default">
    <lf>
      <satop nomvar="X:sem-obj">
        <diamond mode="Airline">
          <nomvar name="P:airline"/>
          <prop name="[*DEFAULT*]"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Adj-Airline" pos="Adj" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adj)/*"/>
        <xsl:with-param name="ext" select="$X.Airline.P.Default"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- On-Airline -->
  <xsl:variable name="X.Airline.Y">
    <lf>
      <satop nomvar="X:sem-obj">
        <diamond mode="Airline"><nomvar name="Y:airline"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="P.has-rel.Of.X.Airline.Y">
    <xsl:call-template name="make-has-rel-lf">
      <xsl:with-param name="rel">Airline</xsl:with-param>
      <xsl:with-param name="sort-Y">airline</xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="Prep-Airline" pos="Prep" closed="true" indexRel="Airline">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($prep.n-postmod)/*"/>
        <xsl:with-param name="ext" select="$X.Airline.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="Predicative">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($pred.prep)/*"/>
        <xsl:with-param name="ext" select="$P.has-rel.Of.X.Airline.Y"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Time/Date Prepositions -->  
  <xsl:variable name="prep.adv">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($adv.backward)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$np.3.Y.acc"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="E.Time.Y">
    <lf>
      <satop nomvar="E:situation">
        <diamond mode="Time">
          <nomvar name="Y:time"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Prep-Time" pos="Prep" closed="true" indexRel="Time">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($prep.adv)/*"/>
        <xsl:with-param name="ext" select="$E.Time.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <xsl:variable name="E.TimeRel.P.Default">
    <lf>
      <satop nomvar="E:situation">
        <diamond mode="TimeRel">
          <nomvar name="P:proposition"/>
          <prop name="[*DEFAULT*]"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <xsl:variable name="E.TimeRel.P.Default.Time.Y">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.TimeRel.P.Default)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="P:proposition">
          <diamond mode="Time"><nomvar name="Y:time"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <family name="Prep-TimeRel" pos="TempAdv" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($prep.adv)/*"/>
        <xsl:with-param name="ext" select="$E.TimeRel.P.Default.Time.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <family name="Between-TimeRel" pos="TempAdv" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adv.backward)/*"/>
        <xsl:with-param name="ext">
          <slash dir="/" mode="*"/>
          <xsl:copy-of select="$np.Z"/>
          <slash dir="/" mode="*"/>
          <xsl:copy-of select="$and-punct"/>
          <slash dir="/" mode="*"/>
          <xsl:copy-of select="$np.Y"/>
          <xsl:call-template name="extend">
            <xsl:with-param name="elt" select="xalan:nodeset($E.TimeRel.P.Default)/*"/>
            <xsl:with-param name="ext">
              <satop nomvar="P:proposition">
                <diamond mode="Time1"><nomvar name="Y:time"/></diamond>
                <diamond mode="Time2"><nomvar name="Z:time"/></diamond>
              </satop>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>
    </entry>
  </family>
  
  <family name="Comparative-TimeRel" pos="TempAdv" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($adv.backward)/*"/>
        <xsl:with-param name="ext" select="$E.TimeRel.P.Default"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <xsl:variable name="E.TimeFrame.Y">
    <lf>
      <satop nomvar="E:situation">
        <diamond mode="TimeFrame">
          <nomvar name="Y:timeframe"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Prep-TimeFrame" pos="Prep" closed="true" indexRel="TimeFrame">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($prep.adv)/*"/>
        <xsl:with-param name="ext" select="$E.TimeFrame.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <xsl:variable name="E.Date.Y">
    <lf>
      <satop nomvar="E:situation">
        <diamond mode="Date">
          <nomvar name="Y:date"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Prep-Date" pos="Prep" closed="true" indexRel="Date">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($prep.adv)/*"/>
        <xsl:with-param name="ext" select="$E.Date.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  
  <!-- Departing: uses Theme, Source roles -->
  <xsl:variable name="E.Default.Theme.X">  
    <lf>
      <satop nomvar="E:motion">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Theme"><nomvar name="X:phys-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="E.Source.Y">
    <lf>
      <satop nomvar="E:motion">
        <diamond mode="Source"><nomvar name="Y:location"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <xsl:variable name="E.Default.Theme.X.Source.Y">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Theme.X)/*"/>
      <xsl:with-param name="ext" select="xalan:nodeset($E.Source.Y)/lf/*"/>
    </xsl:call-template>
  </xsl:variable>

  <family name="Departing" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Theme.X"/>
      </xsl:call-template>
    </entry>
    <entry name="Obj-Source">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Theme.X.Source.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="PP-From-Source">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.from)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Theme.X.Source.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Arriving: uses Theme, Goal roles -->
  <xsl:variable name="E.Goal.Y">
    <lf>
      <satop nomvar="E:motion">
        <diamond mode="Goal">
          <nomvar name="Y:location"/>
        </diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <xsl:variable name="E.Default.Theme.X.Goal.Y">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Theme.X)/*"/>
      <xsl:with-param name="ext" select="xalan:nodeset($E.Goal.Y)/lf/*"/>
    </xsl:call-template>
  </xsl:variable>

  <family name="Arriving" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Theme.X"/>
      </xsl:call-template>
    </entry>
    <entry name="Obj-Goal">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Theme.X.Goal.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="PP-in-Goal">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.in)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Theme.X.Goal.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  
  <!-- Travel: uses Traveler, Category, Airline, Prop, Source, Goal roles -->
  <!-- NB: not all role combinations are supported, b/c there's no direct support for optional args -->
  <xsl:variable name="E.Default.Traveler.X">  
    <lf>
      <satop nomvar="E:motion">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Traveler"><nomvar name="X:phys-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="E.Default.Traveler.X.Category.Y">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Traveler.X)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:motion">
          <diamond mode="Category"><nomvar name="Y:fareclass"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="E.Default.Traveler.X.Prop.P">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Traveler.X)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:motion">
          <diamond mode="Prop"><nomvar name="P:proposition"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="E.Default.Traveler.X.Airline.Y">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Traveler.X)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:motion">
          <diamond mode="Airline"><nomvar name="Y:airline"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="E.Default.Traveler.X.Category.Y.Airline.Z">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Traveler.X.Category.Y)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:motion">
          <diamond mode="Airline"><nomvar name="Z:airline"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="E.Default.Traveler.X.Prop.P.Airline.Z">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Traveler.X.Prop.P)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:motion">
          <diamond mode="Airline"><nomvar name="Z:airline"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="E.Default.Traveler.X.Source.Y">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Traveler.X)/*"/>
      <xsl:with-param name="ext" select="xalan:nodeset($E.Source.Y)/lf/*"/>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="E.Default.Traveler.X.Goal.Y">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Traveler.X)/*"/>
      <xsl:with-param name="ext" select="xalan:nodeset($E.Goal.Y)/lf/*"/>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="E.Default.Traveler.X.Source.Y.Goal.Z">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Traveler.X.Source.Y)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:motion">
          <diamond mode="Goal"><nomvar name="Z:location"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="Travel" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Traveler.X"/>
      </xsl:call-template>
    </entry>
    <entry name="TV">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Traveler.X.Category.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="TV-Goal">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Traveler.X.Goal.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="TV-Pred-Y">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.pred.Y)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Traveler.X.Prop.P"/>
      </xsl:call-template>
    </entry>
    <entry name="TV-On">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.on)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Traveler.X.Airline.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="TV-With">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.with)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Traveler.X.Airline.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="TV-Plus-On">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.plus.on)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Traveler.X.Category.Y.Airline.Z"/>
      </xsl:call-template>
    </entry>
    <entry name="TV-Pred-Y-Plus-On">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.pred.Y.plus.on)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Traveler.X.Prop.P.Airline.Z"/>
      </xsl:call-template>
    </entry>
    <entry name="TV-From">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.from)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Traveler.X.Source.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="TV-To">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.to)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Traveler.X.Goal.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="DTV-From-To">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($dtv.from.to)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Traveler.X.Source.Y.Goal.Z"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Connecting: uses Traveler and Path -->
  <xsl:variable name="E.Default.Traveler.X.Path.Y-city">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Traveler.X)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:motion">
          <diamond mode="Path"><nomvar name="Y:city"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="E.Default.Traveler.X.Path.Y-airport">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Traveler.X)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:motion">
          <diamond mode="Path"><nomvar name="Y:airport"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="Connecting" pos="V" closed="true">
    <entry name="TV-In">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.in)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Traveler.X.Path.Y-city"/>
      </xsl:call-template>
    </entry>
    <entry name="TV-At">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.at)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Traveler.X.Path.Y-airport"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Evaluative Comparison -->
  <xsl:variable name="E.Default.Item.X.Var.Y">  
    <lf>
      <satop nomvar="E:proposition">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Item"><nomvar name="X:sem-obj"/></diamond>
        <diamond mode="Var"><nomvar name="Y:sem-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Evaluative-Comparison" pos="V" closed="true">
    <entry name="TV">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Item.X.Var.Y"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Choosing: uses Cognizer, Chosen, Possib(ility) roles -->
  <xsl:variable name="E.Default.Cognizer.X.Possib.Y">  
    <lf>
      <satop nomvar="E:action">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Cognizer"><nomvar name="X:causal-agent"/></diamond>
        <diamond mode="Possib"><nomvar name="Y:sem-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <family name="Choosing" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Cognizer.X.Chosen.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="TV-From">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.from)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Cognizer.X.Possib.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Booking (cf. FrameNet's Commerce): has Agent and Beneficiary, in addition to Goods --> 
  <xsl:variable name="E.Default.Agent.X">  
    <lf>
      <satop nomvar="E:action">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Agent"><nomvar name="X:causal-agent"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="E.Default.Agent.X.Goods.Y">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Agent.X)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:action">
          <diamond mode="Goods"><nomvar name="Y:phys-obj"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="E.Default.Agent.X.Goods.Y.Beneficiary.Z">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Agent.X.Goods.Y)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:action">
          <diamond mode="Beneficiary"><nomvar name="Z:causal-agent"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:variable name="E.Default.Agent.X.Beneficiary.Y.Goods.Z">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Agent.X)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:action">
          <diamond mode="Beneficiary"><nomvar name="Y:causal-agent"/></diamond>
          <diamond mode="Goods"><nomvar name="Z:phys-obj"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="Booking" pos="V" closed="true">
    <entry name="TV">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Agent.X.Goods.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="DTV">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($dtv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Agent.X.Goods.Y.Beneficiary.Z"/>
      </xsl:call-template>
    </entry>
    <entry name="DTV-For">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($dtv.for)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Agent.X.Beneficiary.Y.Goods.Z"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Offering: just trans for now -->
  <family name="Offering" pos="V" closed="true">
    <entry name="TV">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Agent.X.Goods.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Conveyance (cf. FrameNet's Transportation/Motion): uses Vehicle, Cargo (/Passenger), Goal --> 
  <xsl:variable name="E.Default.Vehicle.X.Goal.Y">  
    <lf>
      <satop nomvar="E:motion">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Vehicle"><nomvar name="X:phys-obj"/></diamond>
        <diamond mode="Goal"><nomvar name="Y:location"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="E.Default.Vehicle.X.Goal.Y.Cargo.Z">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Vehicle.X.Goal.Y)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:motion">
          <diamond mode="Cargo"><nomvar name="Z:phys-obj"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="Conveyance" pos="V" closed="true">
    <entry name="TV-To">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.to)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Vehicle.X.Goal.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="DTV-To">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($dtv.to)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Vehicle.X.Goal.Y.Cargo.Z"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Being Obligated: uses Agent and Duty -->
  <xsl:variable name="E.Default.Agent.X.Duty.Y">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Agent.X)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:situation">
          <diamond mode="Duty"><nomvar name="Y:sem-obj"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="Requiring" pos="V" closed="true">
    <entry name="Obj-Duty">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Agent.X.Duty.Y"/>
      </xsl:call-template>
    </entry>
  </family>

  <xsl:variable name="E.Default.Agent.X.Duty.E2">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Agent.X)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:situation">
          <diamond mode="Duty"><nomvar name="E2:situation"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="Needing-To" pos="V" closed="true">
    <entry name="Obj-Inf">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($subject-control)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Agent.X.Duty.E2"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Costing: uses Goods and Asset -->
  <xsl:variable name="E.Default.Goods.X.Asset.Y">  
    <lf>
      <satop nomvar="E:proposition">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Goods"><nomvar name="X:phys-obj"/></diamond>
        <diamond mode="Asset"><nomvar name="Y:amt"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Costing" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Goods.X.Asset.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Wanting: uses Experiencer and Situation -->
  <xsl:variable name="E.Default.Experiencer.X.Situation.E2">  
    <lf>
      <satop nomvar="E:experience">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Experiencer"><nomvar name="X:animate-being"/></diamond>
        <diamond mode="Situation"><nomvar name="E2:situation"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Wanting" pos="V" closed="true">
    <entry name="Obj-Inf">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($subject-control)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Experiencer.X.Situation.E2"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Finding: uses Perceiver and Sought -->
  <xsl:variable name="E.Default.Perceiver.X.Sought.Y">  
    <lf>
      <satop nomvar="E:proposition">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Perceiver"><nomvar name="X:animate-being"/></diamond>
        <diamond mode="Sought"><nomvar name="Y:phys-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Finding" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Perceiver.X.Sought.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Hearing-About: uses Perceiver, Phenomenon roles -->
  <family name="Hearing-About" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.about)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Perceiver.X.Phenomenon.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Taking-Time: uses Activity, Duration roles -->
  <xsl:variable name="E.Default.Activity.X.Duration.Y">
    <lf>
      <satop nomvar="E:situation">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Activity"><nomvar name="X:situation"/></diamond>
        <diamond mode="Duration"><nomvar name="Y:dur"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Taking-Time" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Activity.X.Duration.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  </ccg-lexicon>

  
  <!-- ***** Write type changing rules to unary-rules.xml ***** -->
  <redirect:write file="unary-rules.xml">
  <unary-rules>
  <xsl:call-template name="add-unary-rules"/>
  </unary-rules>
  </redirect:write>
  </xsl:template>

</xsl:transform>

