<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White)
$Revision: 1.16 $, $Date: 2008/12/11 17:42:31 $ 

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

  <!-- IV -->
  <xsl:variable name="iv">
    <complexcat>
      <xsl:copy-of select="$s.1.E.dcl.default"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np.2.X.nom.default"/>
    </complexcat>
  </xsl:variable>

  <!-- TV -->
  <xsl:variable name="tv">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$np.3.Y.acc"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <!-- TV-About -->
  <xsl:variable name="tv.about">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pp.3.Y.about"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <!-- TV-At -->
  <xsl:variable name="tv.at">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pp.3.Y.at"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <!-- TV-From -->
  <xsl:variable name="tv.from">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pp.3.Y.from"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <!-- TV-In -->
  <xsl:variable name="tv.in">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pp.3.Y.in"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <!-- TV-On -->
  <xsl:variable name="tv.on">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pp.3.Y.on"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <!-- TV-To -->
  <xsl:variable name="tv.to">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pp.3.Y.to"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <!-- TV-With -->
  <xsl:variable name="tv.with">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pp.3.Y.with"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <!-- TV Phrasal -->
  <xsl:variable name="tv.phrasal">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <setarg>
          <slash dir="/" mode="&gt;"/>
          <xsl:copy-of select="$np.3.Y.acc"/>
          <slash dir="/" mode="*"/>
          <atomcat type="prt">
            <fs id="4"/>
          </atomcat>
        </setarg>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <!-- TV-Pred-Y -->
  <xsl:variable name="tv.pred.Y">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pred.adj.Y"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <!-- DTV -->
  <xsl:variable name="dtv">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$np.4.Z.acc"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <!-- DTV-For -->
  <xsl:variable name="dtv.for">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pp.3.Y.for"/>
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$np.4.Z.acc"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <!-- DTV-To -->
  <xsl:variable name="dtv.to">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($tv.to)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$np.4.Z.acc"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <!-- DTV-From-To -->
  <xsl:variable name="dtv.from.to">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <setarg>
          <slash dir="/" mode="&lt;"/>
          <xsl:copy-of select="$pp.3.Y.from"/>
          <slash dir="/" mode="&lt;"/>
          <xsl:copy-of select="$pp.4.Z.to"/>
        </setarg>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <!-- TV-Plus-In -->
  <xsl:variable name="tv.plus.in">    
    <xsl:call-template name="insert-after-last-leftward-arg">
      <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
      <xsl:with-param name="ins">
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$pp.4.Z.in"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <!-- TV-Plus-On -->
  <xsl:variable name="tv.plus.on">    
    <xsl:call-template name="insert-after-last-leftward-arg">
      <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
      <xsl:with-param name="ins">
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$pp.4.Z.on"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <!-- TV-Plus-Pred-Y -->
  <xsl:variable name="tv.plus.pred.Y">    
    <xsl:call-template name="insert-after-last-leftward-arg">
      <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
      <xsl:with-param name="ins">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pred.adj.Y"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <!-- TV-Pred-Y-Plus-On -->
  <xsl:variable name="tv.pred.Y.plus.on">    
    <xsl:call-template name="insert-after-last-leftward-arg">
      <xsl:with-param name="elt" select="xalan:nodeset($tv.pred.Y)/*"/>
      <xsl:with-param name="ins">
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$pp.4.Z.on"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <!-- TV Scomp -->
  <!-- NB: could add a supertype for dcl/emb -->
  <xsl:variable name="tv.scomp.dcl">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$s.E2.dcl"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="tv.scomp.emb">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$s.E2.emb"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <!-- Subject Control -->
  <xsl:variable name="subject-control">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
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

  <!-- Object Control (not yet used) -->
  <xsl:variable name="object-control">    
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
      <xsl:with-param name="ext">
        <slash dir="/" mode="&gt;"/>
        <complexcat>
          <xsl:copy-of select="$s.E2.inf"/>
          <slash dir="\"/>
          <xsl:copy-of select="$np.Y"/>
        </complexcat>
        <slash dir="/" mode="&lt;"/>
        <xsl:copy-of select="$np.3.Y.acc"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  
  <!-- ***** LFs ***** -->

  <!-- Perceiving: uses Perceiver, Phenomenon roles -->
  <!-- NB: different families for  Looking-At/Seeing/Hearing-About -->
  <xsl:variable name="E.Default.Perceiver.X.Phenomenon.Y">  
    <lf>
      <satop nomvar="E:experience">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Perceiver"><nomvar name="X:animate-being"/></diamond>
        <diamond mode="Phenomenon"><nomvar name="Y:sem-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  <xsl:variable name="E.Default.Perceiver.X">  
    <lf>
      <satop nomvar="E:experience">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Perceiver"><nomvar name="X:animate-being"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <!-- Choosing/Going-With: uses Cognizer, Chosen roles -->
  <xsl:variable name="E.Default.Cognizer.X.Chosen.Y">  
    <lf>
      <satop nomvar="E:action">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Cognizer"><nomvar name="X:causal-agent"/></diamond>
        <diamond mode="Chosen"><nomvar name="Y:sem-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  
  <!-- ***** Verb Families ***** -->
  <xsl:template name="add-v-families">

  <!-- To Infinitive -->
  <family name="To-Infinitive" pos="Prep" closed="true" indexRel="*NoSem*">
    <entry name="Basic">
      <complexcat>
        <xsl:copy-of select="$s.from-1.inf"/>
        <slash dir="\" mode="&lt;" ability="inert"/>
        <xsl:copy-of select="$np.2.X"/>
        <slash dir="/" mode="^"/>
        <complexcat>
          <xsl:copy-of select="$s.1.E.base"/>
          <slash dir="\" mode="&lt;"/>
          <xsl:copy-of select="$np.2.X"/>
        </complexcat>
      </complexcat>
    </entry>
  </family>

  <!-- Possession: uses Owner and Poss(ession); may 
       also give a prop via the Where role -->
  <xsl:variable name="E.Default.Owner.X.Poss.Y">  
    <lf>
      <satop nomvar="E:situation">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Owner"><nomvar name="X:sem-obj"/></diamond>
        <diamond mode="Poss"><nomvar name="Y:sem-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="E.Default.Owner.X.Poss.Y.Where.P">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Owner.X.Poss.Y)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:situation">
          <diamond mode="Where"><nomvar name="P:proposition"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <family name="Possession" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Owner.X.Poss.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="TV-Plus-Pred-Y">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.plus.pred.Y)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Owner.X.Poss.Y.Where.P"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Statement: Speaker and optional Message -->
  <xsl:variable name="E.Default.Speaker.X">  
    <lf>
      <satop nomvar="E:statement">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Speaker"><nomvar name="X:causal-agent"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <xsl:variable name="E.Default.Speaker.X.Message.E2">  
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.Default.Speaker.X)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:statement">
          <diamond mode="Message"><nomvar name="E2:situation"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <family name="Statement" pos="V" closed="true">
    <entry name="IV">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($iv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Speaker.X"/>
      </xsl:call-template>
    </entry>
    <entry name="TV-SComp-Dcl">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.scomp.dcl)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Speaker.X.Message.E2"/>
      </xsl:call-template>
    </entry>
    <entry name="TV-SComp-Emb">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv.scomp.emb)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Speaker.X.Message.E2"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Experiencer-Subj (eg liking): uses Experiencer, Content roles -->
  <xsl:variable name="E.Default.Experiencer.X.Content.E2">  
    <lf>
      <satop nomvar="E:experience">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Experiencer"><nomvar name="X:animate-being"/></diamond>
        <diamond mode="Content"><nomvar name="E2:situation"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  <xsl:variable name="E.Default.Experiencer.X.Content.Y">  
    <lf>
      <satop nomvar="E:experience">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Experiencer"><nomvar name="X:animate-being"/></diamond>
        <diamond mode="Content"><nomvar name="Y:sem-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <family name="Experiencer-Subj" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($subject-control)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Experiencer.X.Content.E2"/>
      </xsl:call-template>
    </entry>
    <entry name="TV">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Experiencer.X.Content.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Agentive-Causing: uses Agent and Effect -->
  <xsl:variable name="E.Default.Agent.X.Effect.Y">  
    <lf>
      <satop nomvar="E:situation">
        <prop name="[*DEFAULT*]"/>
        <diamond mode="Agent"><nomvar name="X:causal-agent"/></diamond>
        <diamond mode="Effect"><nomvar name="Y:situation"/></diamond>
      </satop>
    </lf>
  </xsl:variable>

  <family name="Agentive-Causing" pos="V" closed="true">
    <entry name="Primary">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.Default.Agent.X.Effect.Y"/>
      </xsl:call-template>
    </entry>
  </family>

  <!-- Copula -->
  <xsl:variable name="copula.pred">
    <complexcat>
      <xsl:copy-of select="$s.1.E.dcl.default"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np.2.X.nom.default"/>
      <slash dir="/" mode="&gt;"/>
      <xsl:copy-of select="$pred.adj"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="copula.pred.inverted">
    <complexcat>
      <xsl:copy-of select="$s.1.E.q.default"/>
      <slash dir="/" mode="&gt;"/>
      <xsl:copy-of select="$pred.adj"/>
      <slash dir="/" mode="*"/>
      <xsl:copy-of select="$np.2.X.nom.default"/>
    </complexcat>
  </xsl:variable>

  <!-- 
    NB: Both X and P are treated as semantic arguments of be, 
        in order to simplify the realization problem. 
        If just X appeared as a semantic argument, then 
        the realizer would have to guess P.
        If just P appeared as a semantic argument, 
        this would either force a change in the analysis of 
        the inverted form to s/s, complicating subj-v 
        agreement by making it indirect, or force the 
        realizer to handle unbound index vars, making it more 
        complicated and less efficient.
        (But note that inverted 'do' has to handle 
        agreement indirectly using s/s.)
  -->
  <xsl:variable name="E.be.Arg.X.Prop.P">
    <lf>
      <satop nomvar="E:state">
        <prop name="be"/>
        <diamond mode="Arg">
          <nomvar name="X:sem-obj"/>
        </diamond>
        <diamond mode="Prop"><nomvar name="P:proposition"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <xsl:variable name="copula.tv.inverted">
    <complexcat>
      <xsl:copy-of select="$s.1.E.q.default"/>
      <slash dir="/" mode="&gt;"/>
      <xsl:copy-of select="$np.3.Y.acc"/>
      <slash dir="/" mode="*"/>
      <xsl:copy-of select="$np.2.X.nom.default"/>
    </complexcat>
  </xsl:variable>

  <!-- NB: This doesn't really capture the predicational nature of Y. -->
  <xsl:variable name="E.be.Arg.X.Pred.Y">
    <lf>
      <satop nomvar="E:state">
        <prop name="be"/>
        <diamond mode="Arg">
          <nomvar name="X:sem-obj"/>
        </diamond>
        <diamond mode="Pred"><nomvar name="Y:sem-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="Copula" pos="V" closed="true">
    <entry name="Pred">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($copula.pred)/*"/>
        <xsl:with-param name="ext" select="$E.be.Arg.X.Prop.P"/>
      </xsl:call-template>
    </entry>
    <entry name="Pred-Inverted">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($copula.pred.inverted)/*"/>
        <xsl:with-param name="ext" select="$E.be.Arg.X.Prop.P"/>
      </xsl:call-template>
    </entry>
    <entry name="NP">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($tv)/*"/>
        <xsl:with-param name="ext" select="$E.be.Arg.X.Pred.Y"/>
      </xsl:call-template>
    </entry>
    <entry name="NP-Inverted">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($copula.tv.inverted)/*"/>
        <xsl:with-param name="ext" select="$E.be.Arg.X.Pred.Y"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- There existential -->
  <!-- NB: would be nice to use features to collapse pred.adj and pred.ng -->
  <xsl:variable name="there-be.np">
    <complexcat>
      <xsl:copy-of select="$s.1.E.dcl.default"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np_expl.there"/>
      <slash dir="/" mode="&gt;"/>
      <xsl:copy-of select="$np.2.X.nom.default"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="there-be.np.pred.adj">
    <xsl:call-template name="insert-after-last-leftward-arg">
      <xsl:with-param name="elt" select="xalan:nodeset($there-be.np)/*"/>
      <xsl:with-param name="ins">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pred.adj"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="there-be.np.pred.ng">
    <xsl:call-template name="insert-after-last-leftward-arg">
      <xsl:with-param name="elt" select="xalan:nodeset($there-be.np)/*"/>
      <xsl:with-param name="ins">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pred.ng"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="there-be.np.inverted">
    <complexcat>
      <xsl:copy-of select="$s.1.E.q.default"/>
      <slash dir="/" mode="&gt;"/>
      <xsl:copy-of select="$np.2.X.nom.default"/>
      <slash dir="/" mode="*"/>
      <xsl:copy-of select="$np_expl.there"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="there-be.np.pred.adj.inverted">
    <xsl:call-template name="insert-after">
      <xsl:with-param name="elt" select="xalan:nodeset($there-be.np.inverted)/*"/>
      <xsl:with-param name="ins">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pred.adj"/>
      </xsl:with-param>
      <xsl:with-param name="pos" select="1"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="there-be.np.pred.ng.inverted">
    <xsl:call-template name="insert-after">
      <xsl:with-param name="elt" select="xalan:nodeset($there-be.np.inverted)/*"/>
      <xsl:with-param name="ins">
        <slash dir="/" mode="&gt;"/>
        <xsl:copy-of select="$pred.ng"/>
      </xsl:with-param>
      <xsl:with-param name="pos" select="1"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="E.there-be.Arg.X">
    <lf>
      <satop nomvar="E:state">
        <prop name="there-be"/>
        <diamond mode="Arg"><nomvar name="X:sem-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <xsl:variable name="E.there-be.Arg.X.Prop.P">
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($E.there-be.Arg.X)/*"/>
      <xsl:with-param name="ext">
        <satop nomvar="E:state">
          <diamond mode="Prop"><nomvar name="P:situation"/></diamond>
        </satop>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <family name="ThereExistential" pos="V" closed="true">
    <entry name="NP">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($there-be.np)/*"/>
        <xsl:with-param name="ext" select="$E.there-be.Arg.X"/>
      </xsl:call-template>
    </entry>
    <entry name="NP-PredAdj">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($there-be.np.pred.adj)/*"/>
        <xsl:with-param name="ext" select="$E.there-be.Arg.X.Prop.P"/>
      </xsl:call-template>
    </entry>
    <entry name="NP-PredNg">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($there-be.np.pred.ng)/*"/>
        <xsl:with-param name="ext" select="$E.there-be.Arg.X.Prop.P"/>
      </xsl:call-template>
    </entry>
    <entry name="NP-Inverted">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($there-be.np.inverted)/*"/>
        <xsl:with-param name="ext" select="$E.there-be.Arg.X"/>
      </xsl:call-template>
    </entry>
    <entry name="NP-PredAdj-Inverted">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($there-be.np.pred.adj.inverted)/*"/>
        <xsl:with-param name="ext" select="$E.there-be.Arg.X.Prop.P"/>
      </xsl:call-template>
    </entry>
    <entry name="NP-PredNg-Inverted">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($there-be.np.pred.ng.inverted)/*"/>
        <xsl:with-param name="ext" select="$E.there-be.Arg.X.Prop.P"/>
      </xsl:call-template>
    </entry>
  </family>
  
  <!-- Here existential -->
  <!-- NB: Probably should just be locative subject with inverted copula -->
  <xsl:variable name="here-be.np">
    <complexcat>
      <xsl:copy-of select="$s.1.E.dcl.default"/>
      <slash dir="\" mode="&lt;"/>
      <xsl:copy-of select="$np_expl.here"/>
      <slash dir="/" mode="&gt;"/>
      <xsl:copy-of select="$np.2.X.nom.default"/>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="E.here-be.Arg.X">
    <lf>
      <satop nomvar="E:state">
        <prop name="here-be"/>
        <diamond mode="Arg"><nomvar name="X:sem-obj"/></diamond>
      </satop>
    </lf>
  </xsl:variable>
  
  <family name="HereExistential" pos="V" closed="true">
    <entry name="NP">
      <xsl:call-template name="extend">
        <xsl:with-param name="elt" select="xalan:nodeset($here-be.np)/*"/>
        <xsl:with-param name="ext" select="$E.here-be.Arg.X"/>
      </xsl:call-template>
    </entry>
  </family>
  
  </xsl:template>

</xsl:transform>


