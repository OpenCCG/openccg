<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-5 University of Edinburgh (Michael White)
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

  
  <!-- ***** Unary Rules ***** -->
  
  <xsl:template name="add-unary-rules">

  <!-- Reduced relatives -->
  <typechanging name="rrel">
    <arg>
      <complexcat>
        <xsl:copy-of select="$s.1.E.dcl"/>
        <slash dir="/"/>
        <xsl:copy-of select="$np.2.X.3rd"/>
      </complexcat>
    </arg>
    <result>
      <complexcat>
        <xsl:copy-of select="$n.from-2.X.CASE"/>
        <slash dir="\" mode="*"/>
        <xsl:copy-of select="$n.from-2.X.CASE"/>
        <xsl:copy-of select="$X.GenRel.E"/>
      </complexcat>
    </result>
  </typechanging>
  
  <!-- Topicalization: adds <tpc>+ to the semantics; requires theme, h owner -->
  <typechanging name="tpc">
    <arg>
      <atomcat type="np">
        <fs id="2">
          <feat attr="index"><lf><nomvar name="X"/></lf></feat>
          <feat attr="info"><lf><prop name="th"/></lf></feat>
          <feat attr="owner"><lf><prop name="h"/></lf></feat>
        </fs>
      </atomcat>
    </arg>
    <result>
      <complexcat>
        <atomcat type="s">
          <fs inheritsFrom="1">
            <feat attr="form" val="fronted"/>
          </fs>
        </atomcat>
        <slash dir="/" mode="^"/>
        <complexcat>
          <atomcat type="s">
            <fs id="1">
              <feat attr="form" val="dcl"/>
            </fs>
          </atomcat>
          <slash dir="/"/>
          <atomcat type="np">
            <fs id="2"/>
          </atomcat>
        </complexcat>
        <lf>
          <satop nomvar="X:sem-obj">
            <diamond mode="tpc"><prop name="+"/></diamond>
          </satop>
        </lf>
      </complexcat>
    </result>
  </typechanging>

  <!-- Bare NPs: adds <det>nil to the semantics -->
  <typechanging name="bnp">
    <arg>
      <complexcat>
        <atomcat type="n">
          <fs id="2">
            <feat attr="index"><lf><nomvar name="X"/></lf></feat>
            <feat attr="num" val="pl-or-mass"/>
          </fs>
        </atomcat>
        <slash/>
        <dollar name="1"/>
      </complexcat>
    </arg>
    <result>
      <complexcat>
        <atomcat type="np">
          <fs id="2">
            <feat attr="pers" val="3rd"/>
          </fs>
          <lf>
            <satop nomvar="X:sem-obj">
              <diamond mode="det"><prop name="nil"/></diamond>
            </satop>
          </lf>
        </atomcat>
        <slash/>
        <dollar name="1"/>
      </complexcat>
    </result>
  </typechanging>
  
 
  <!-- List completion -->
  <xsl:copy-of select="$s-list"/>
  <xsl:copy-of select="$pred-adj-list-to-adj"/>
  <xsl:copy-of select="$np-list-c"/>
  <xsl:copy-of select="$np-list-d-rightward-TR"/>
  <xsl:copy-of select="$np-list-d-leftward-TR"/>

  <!-- Cardinality prenominals -->
  <typechanging name="card">
    <arg>
      <atomcat type="num">
        <fs>
          <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
          <feat attr="num"><featvar name="NUM"/></feat>
        </fs>
      </atomcat>
    </arg>
    <result>
      <complexcat>
        <atomcat type="n">
          <fs id="2">
            <feat attr="index"><lf><nomvar name="X"/></lf></feat>
            <feat attr="info"><lf><var name="INFO"/></lf></feat>
            <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
            <feat attr="num"><featvar name="NUM"/></feat>
          </fs>
        </atomcat>
        <slash dir="/" mode="^"/>
        <atomcat type="n">
          <fs id="2">
            <feat attr="index"><lf><nomvar name="X"/></lf></feat>
            <feat attr="info"><lf><var name="INFO"/></lf></feat>
            <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
            <feat attr="num"><featvar name="NUM"/></feat>
          </fs>
        </atomcat>
        <lf>
          <satop nomvar="X:sem-obj">
            <diamond mode="Card"><nomvar name="Y:num"/></diamond>
          </satop>
        </lf>
      </complexcat>
    </result>
  </typechanging>
  <typechanging name="card-h">
    <arg>
      <atomcat type="num">
        <fs>
          <feat attr="index"><lf><nomvar name="Y"/></lf></feat>
          <feat attr="num" val="pl"/>
        </fs>
      </atomcat>
    </arg>
    <result>
      <atomcat type="n">
        <fs id="2">
          <feat attr="index"><lf><nomvar name="X"/></lf></feat>
          <feat attr="info"><lf><var name="INFO"/></lf></feat>
          <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
          <feat attr="num" val="pl"/>
        </fs>
        <lf>
          <satop nomvar="X:sem-obj">
            <prop name="pro-n"/>
            <diamond mode="info"><var name="INFO"/></diamond>
            <diamond mode="owner"><var name="OWNER"/></diamond>
            <diamond mode="kon"><prop name="-"/></diamond>
            <diamond mode="Card"><nomvar name="Y:num"/></diamond>
          </satop>
        </lf>
      </atomcat>
    </result>
  </typechanging>

  <!-- purpose clauses -->
  <typechanging name="purp-i">
    <arg> 
      <complexcat>
        <atomcat type="s">
          <fs>
            <feat attr="form" val="inf"/>
            <feat attr="index"><lf><nomvar name="E2"/></lf></feat>
            <feat attr="info"><lf><var name="INFO"/></lf></feat>
            <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
          </fs>
        </atomcat>
        <slash dir="\" mode="&lt;"/>
        <xsl:copy-of select="$np.X"/>
      </complexcat>
    </arg>
    <result>
      <complexcat>
        <atomcat type="s">
          <fs inheritsFrom="1">
            <feat attr="form" val="fronted"/>
            <feat attr="index"><lf><nomvar name="S"/></lf></feat>
          </fs>
        </atomcat>
        <slash dir="/" mode="^"/>
        <atomcat type="s">
          <fs id="1">
            <feat attr="form" val="dcl"/>
            <feat attr="index"><lf><nomvar name="E"/></lf></feat>
            <feat attr="info"><lf><var name="INFO"/></lf></feat>
            <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
          </fs>
        </atomcat>
        <xsl:copy-of select="$S.purpose-rel.Core.E.Trib.E2"/>
      </complexcat>
    </result>
  </typechanging>
  </xsl:template>

</xsl:transform>


