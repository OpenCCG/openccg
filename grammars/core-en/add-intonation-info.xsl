<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White) 
$Revision: 1.5 $, $Date: 2006/03/28 14:56:34 $ 

This transformation adds dictionary entries for accented forms, 
along with macros for propagating intonation info.
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan xalan2">

  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  <xsl:variable name="apostrophe">'</xsl:variable>
  
  <!-- Add info, k-contrast, owner macros to dictionary -->
  <xsl:template match="dictionary">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:call-template name="add-macros"/>
    </xsl:copy>
  </xsl:template>
  
  
  <!-- Nominal, Numeric entries --> 
  <xsl:template match="entry[@pos='NNP' or @pos='N' or @pos='Pro' or @pos='Num' or @pos='NNP-Date' or @pos='NNP-Time' or @pos='NNP-Amt']">
    <xsl:call-template name="add-accented-forms">
      <xsl:with-param name="nokon-macros" select="'@INFO-X @nokon-X @OWNER-X'"/>
      <xsl:with-param name="th-kon-macros" select="'@th-X @kon-X @OWNER-X'"/>
      <xsl:with-param name="rh-kon-macros" select="'@rh-X @kon-X @OWNER-X'"/>
    </xsl:call-template>
  </xsl:template>
  
  <!-- Quant NP, Det entries --> 
  <xsl:template match="entry[@pos='QNP' or @pos='QDet']">
    <xsl:call-template name="add-accented-forms">
      <xsl:with-param name="nokon-macros" select="'@INFO-S @nokon-S @OWNER-S'"/>
      <xsl:with-param name="th-kon-macros" select="'@th-S @kon-S @OWNER-S'"/>
      <xsl:with-param name="rh-kon-macros" select="'@rh-S @kon-S @OWNER-S'"/>
    </xsl:call-template>
  </xsl:template>
  
  <!-- Wh NP, Det entries --> 
  <xsl:template match="entry[@pos='WhNP' or @pos='Wh']">
    <xsl:call-template name="add-accented-forms">
      <xsl:with-param name="nokon-macros" select="'@INFO-Q @nokon-Q @OWNER-Q'"/>
      <xsl:with-param name="th-kon-macros" select="'@th-Q @kon-Q @OWNER-Q'"/>
      <xsl:with-param name="rh-kon-macros" select="'@rh-Q @kon-Q @OWNER-Q'"/>
    </xsl:call-template>
  </xsl:template>
  
  <!-- Adj, Adv, Temp Adv, Pred Det, Poss Pro, AmPm entries --> 
  <xsl:template match="entry[@pos='Adj' or @pos='Adv' or @pos='TempAdv' or @pos='PDet' or @pos='PossPro' or @pos='AmPm']">
    <xsl:call-template name="add-accented-forms">
      <xsl:with-param name="nokon-macros" select="'@INFO-P @nokon-P @OWNER-P'"/>
      <xsl:with-param name="th-kon-macros" select="'@th-P @kon-P @OWNER-P'"/>
      <xsl:with-param name="rh-kon-macros" select="'@rh-P @kon-P @OWNER-P'"/>
    </xsl:call-template>
  </xsl:template>
  
  <!-- Verbal entries -->
  <xsl:template match="entry[@pos='V' and not(@stem='do')]">
    <xsl:call-template name="add-accented-forms">
      <xsl:with-param name="nokon-macros" select="'@INFO-E @nokon-E @OWNER-E'"/>
      <xsl:with-param name="th-kon-macros" select="'@th-E @kon-E @OWNER-E'"/>
      <xsl:with-param name="rh-kon-macros" select="'@rh-E @kon-E @OWNER-E'"/>
    </xsl:call-template>
  </xsl:template>

  <!-- 'do': need special handling of do-support -->
  <xsl:template match="entry[@stem='do']">
    <!-- make accented forms, excluding do-support -->
    <xsl:variable name="entry-with-accented-forms">
      <xsl:call-template name="add-accented-forms">
        <xsl:with-param name="nokon-macros" select="'@INFO-E @nokon-E @OWNER-E'"/>
        <xsl:with-param name="th-kon-macros" select="'@th-E @kon-E @OWNER-E'"/>
        <xsl:with-param name="rh-kon-macros" select="'@rh-E @kon-E @OWNER-E'"/>
        <xsl:with-param name="extra-excludes" select="'Do-Support'"/>
      </xsl:call-template>
    </xsl:variable>
    <!-- add existing forms, excluding modal family -->
    <xsl:call-template name="extend">
      <xsl:with-param name="elt" select="xalan:nodeset($entry-with-accented-forms)/*"/>
      <xsl:with-param name="ext">
        <xsl:for-each select="word">
          <word form="{@form}" excluded="{normalize-space(concat(@excluded,' Modal'))}">
            <xsl:apply-templates select="@macros"/>
          </word>
        </xsl:for-each>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  
  <!-- Simple prep entries and poss 's: just add INFO and OWNER vars -->
  <xsl:template match="entry[@pos='Prep' or @pos='PossS']">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="macros">
        <xsl:value-of select="normalize-space(concat(@macros,' ','@INFO-3 @OWNER-3'))"/>
      </xsl:attribute>
      <xsl:apply-templates select="*"/>
    </xsl:copy>
  </xsl:template>

  
  <!-- Conjunctions: just 'but' for now (reconciling other forms tbd) -->
  <xsl:template match="entry[@pos='Conj']">
    <xsl:call-template name="add-accented-forms">
      <xsl:with-param name="nokon-macros" select="'@INFO-S @nokon-S @OWNER-S'"/>
      <xsl:with-param name="th-kon-macros" select="'@th-S @kon-S @OWNER-S'"/>
      <xsl:with-param name="rh-kon-macros" select="'@rh-S @kon-S @OWNER-S'"/>
    </xsl:call-template>
  </xsl:template>

  
  <!-- Add word forms with pitch accents and appropriate macros to current entry -->
  <xsl:template name="add-accented-forms">
    <xsl:param name="nokon-macros"/>
    <xsl:param name="th-kon-macros"/>
    <xsl:param name="rh-kon-macros"/>
    <xsl:param name="extra-excludes" select="''"/>
    <xsl:copy>
      <!-- copy attributes and member-of|stem-for list -->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="member-of|stem-for"/>
      <!-- if no word forms listed, add accented word forms based on word or stem -->
      <xsl:if test="not(word)">
        <xsl:variable name="form">
          <xsl:choose>
            <xsl:when test="@word"><xsl:value-of select="@word"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="@stem"/></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <word form="{$form}" macros="{$nokon-macros}"/>
        <!-- avoid accents on reduced forms -->
        <xsl:if test="not(@pos='V' and contains($form, $apostrophe))">
          <word form="{concat($form,'_L+H*')}" macros="{$th-kon-macros}"/>
          <word form="{concat($form,'_H*')}" macros="{$rh-kon-macros}"/>
        </xsl:if>
      </xsl:if>
      <!-- otherwise add accented word forms for each listed word form, with any extra excludes -->
      <xsl:for-each select="word">
        <xsl:variable name="excluded" select="normalize-space(concat(@excluded,' ',$extra-excludes))"/>
        <word form="{@form}" macros="{normalize-space(concat(@macros,' ',$nokon-macros))}">
          <xsl:if test="$excluded != ''">
            <xsl:attribute name="excluded"><xsl:copy-of select="$excluded"/></xsl:attribute>
          </xsl:if>
        </word>
        <!-- avoid accents on reduced forms -->
        <xsl:if test="not(../@pos='V' and contains(@form, $apostrophe))">
          <word form="{concat(@form,'_L+H*')}" macros="{normalize-space(concat(@macros,' ',$th-kon-macros))}">
            <xsl:if test="$excluded != ''">
              <xsl:attribute name="excluded"><xsl:copy-of select="$excluded"/></xsl:attribute>
            </xsl:if>
          </word>
          <word form="{concat(@form,'_H*')}" macros="{normalize-space(concat(@macros,' ',$rh-kon-macros))}">
            <xsl:if test="$excluded != ''">
              <xsl:attribute name="excluded"><xsl:copy-of select="$excluded"/></xsl:attribute>
            </xsl:if>
          </word>
        </xsl:if>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>


  <!-- Macros for info, k-contrast, owner -->
  <xsl:template name="add-macros">
    <!-- info -->
    <macro name="@th-X">
      <fs id="2">
        <feat attr="info"><lf><prop name="th"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="X">
          <diamond mode="info"><prop name="th"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@rh-X">
      <fs id="2">
        <feat attr="info"><lf><prop name="rh"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="X">
          <diamond mode="info"><prop name="rh"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@INFO-X">
      <fs id="2">
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="X">
          <diamond mode="info"><var name="INFO"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@th-S">
      <fs id="1">
        <feat attr="info"><lf><prop name="th"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="S">
          <diamond mode="info"><prop name="th"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@rh-S">
      <fs id="1">
        <feat attr="info"><lf><prop name="rh"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="S">
          <diamond mode="info"><prop name="rh"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@INFO-S">
      <fs id="1">
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="S">
          <diamond mode="info"><var name="INFO"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@th-Q">
      <fs id="2">
        <feat attr="info"><lf><prop name="th"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="Q">
          <diamond mode="info"><prop name="th"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@rh-Q">
      <fs id="2">
        <feat attr="info"><lf><prop name="rh"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="Q">
          <diamond mode="info"><prop name="rh"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@INFO-Q">
      <fs id="2">
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="Q">
          <diamond mode="info"><var name="INFO"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@th-P">
      <fs id="1">
        <feat attr="info"><lf><prop name="th"/></lf></feat>
      </fs>
      <fs id="2">
        <feat attr="info"><lf><prop name="th"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="P">
          <diamond mode="info"><prop name="th"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@rh-P">
      <fs id="1">
        <feat attr="info"><lf><prop name="rh"/></lf></feat>
      </fs>
      <fs id="2">
        <feat attr="info"><lf><prop name="rh"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="P">
          <diamond mode="info"><prop name="rh"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@INFO-P">
      <fs id="1">
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
      </fs>
      <fs id="2">
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="P">
          <diamond mode="info"><var name="INFO"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@th-E">
      <fs id="1">
        <feat attr="info"><lf><prop name="th"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="E">
          <diamond mode="info"><prop name="th"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@rh-E">
      <fs id="1">
        <feat attr="info"><lf><prop name="rh"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="E">
          <diamond mode="info"><prop name="rh"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@INFO-E">
      <fs id="1">
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="E">
          <diamond mode="info"><var name="INFO"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@INFO-3">
      <fs id="3">
        <feat attr="info"><lf><var name="INFO"/></lf></feat>
      </fs>
    </macro>
  
    <!-- owner -->
    <macro name="@OWNER-X">
      <fs id="2">
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="X">
          <diamond mode="owner"><var name="OWNER"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@OWNER-S">
      <fs id="1">
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="S">
          <diamond mode="owner"><var name="OWNER"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@OWNER-Q">
      <fs id="2">
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="Q">
          <diamond mode="owner"><var name="OWNER"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@OWNER-P">
      <fs id="1">
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
      </fs>
      <fs id="2">
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="P">
          <diamond mode="owner"><var name="OWNER"/></diamond>
        </satop>
      </lf>
    </macro>
    
    <macro name="@OWNER-E">
      <fs id="1">
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
      </fs>
      <lf>
        <satop nomvar="E">
          <diamond mode="owner"><var name="OWNER"/></diamond>
        </satop>
      </lf>
    </macro>
    
    <macro name="@OWNER-3">
      <fs id="3">
        <feat attr="owner"><lf><var name="OWNER"/></lf></feat>
      </fs>
    </macro>
  
    <!-- k-contrast -->
    <macro name="@kon-X">
      <lf>
        <satop nomvar="X">
          <diamond mode="kon"><prop name="+"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@nokon-X">
      <lf>
        <satop nomvar="X">
          <diamond mode="kon"><prop name="-"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@kon-S">
      <lf>
        <satop nomvar="S">
          <diamond mode="kon"><prop name="+"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@nokon-S">
      <lf>
        <satop nomvar="S">
          <diamond mode="kon"><prop name="-"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@kon-Q">
      <lf>
        <satop nomvar="Q">
          <diamond mode="kon"><prop name="+"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@nokon-Q">
      <lf>
        <satop nomvar="Q">
          <diamond mode="kon"><prop name="-"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@kon-P">
      <lf>
        <satop nomvar="P">
          <diamond mode="kon"><prop name="+"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@nokon-P">
      <lf>
        <satop nomvar="P">
          <diamond mode="kon"><prop name="-"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@kon-E">
      <lf>
        <satop nomvar="E">
          <diamond mode="kon"><prop name="+"/></diamond>
        </satop>
      </lf>
    </macro>
  
    <macro name="@nokon-E">
      <lf>
        <satop nomvar="E">
          <diamond mode="kon"><prop name="-"/></diamond>
        </satop>
      </lf>
    </macro>
  </xsl:template>

  
  <!-- ***** Extend ***** -->
  <xsl:template name="extend">
    <xsl:param name="elt"/>
    <xsl:param name="ext"/>
    <xsl:element name="{name($elt)}">
      <xsl:apply-templates select="$elt/@*"/>
      <xsl:apply-templates select="$elt/node()"/>
      <xsl:copy-of select="$ext"/>
    </xsl:element>
  </xsl:template>


  <!-- Copy -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>

