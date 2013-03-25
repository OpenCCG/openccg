<?xml version="1.0"?>

<!-- Setting up the lexicon parameters from parameters file (parameters.xml)

 Cem Bozsahin 2003-5 (cem.bozsahin@ed.ac.uk / bozsahin@metu.edu.tr)
-->

<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0" xmlns:xalan="http://xml.apache.org/xslt"
    exclude-result-prefixes="xalan">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"
      xalan:indent-amount="3"/>

  <xsl:variable name="controlled.predicate.basic.type"
      select="//iv//predicate/@syn-type"/>

  <xsl:variable name="infinitive.type"
      select="//infinitive/@subject-type"/>

  <xsl:variable name="controlled.argument.basic.type"
      select="//iv//s-argument/@syn-type"/>

  <xsl:variable name="pred.0.E">
    <fs id="100">        <!-- 0 is special; use 100 instead for result-->
      <feat attr="index">
        <lf>
          <nomvar name="E"/>
        </lf>
      </feat>
    </fs>
  </xsl:variable>

  <xsl:variable name="controlled-vp-inf-cat.1">
    <complexcat>
      <xsl:comment>NB: Word order and directionality of embedded clause's 
|(S|NP) type is an 'educated guess' from the syntactic type of TV. 
Change them accordingly.
  </xsl:comment>
      <atomcat>
        <xsl:attribute name="type">
          <xsl:value-of select="$controlled.predicate.basic.type"/>
        </xsl:attribute>
        <fs id="3">
          <feat attr="vform" val="non-fin"/>
          <feat attr="index">
            <lf>
              <nomvar name="E2"/>
            </lf>
          </feat>
        </fs>
      </atomcat>
      <slash dir="|"/>
      <atomcat>
        <xsl:attribute name="type">
          <xsl:value-of select="$controlled.argument.basic.type"/>
        </xsl:attribute>
        <fs id="4">
          <xsl:comment> NB: If infinitive type is syntactic subject (which is the 
		  default), there is a built-in type in types.xml file called
		  "subject-case". Include in this type all the subjects that
		  can be controlled e.g. nominative subjects only (as in German), 
		  dative and nominative subjects (as in Malayalam) etc. In this
		  case, the controllee is not semantically restricted.
		  
		  If infinitive type is semantic subject, you need a larger
		  fragment of Hybrid Logic than HLDS uses to implement identity
		  of two event variables e.g.
		  
		  @_e(Arg1 a) ^ @_e(Arg1 b) --> @_a(b)
		  
		  where a is the event variable for controller verb, and
		  b is the event variable for the controlled verb, and Arg1 is the
		  modality for primary arguments (1s).
		  
		  This constraint is formulable in HL but HLDS does not cover that
		  fragment (yet). When it does, the lf tag should just stick in that
		  constraint.
		  
		  Currently, OpenCCG can generate an LF with TWO non-identical Arg1 modalities
		  in the same event structure, therefore some illicit examples would
		  go through.
		</xsl:comment>
          <xsl:choose>
            <xsl:when test="$infinitive.type = 'semantic'">
              <feat attr="index">
                <lf>
                  <nomvar name="X1"/>
                </lf>
                <xsl:comment>NB: Index is same as Arg1's </xsl:comment>
              </feat>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="make-attr-val">
                <xsl:with-param name="attr" select="'case'"/>
                <xsl:with-param name="val" select="'subject-case'"/>
              </xsl:call-template>
              <feat attr="index">
                <lf>
                  <nomvar name="X1"/>
                </lf>
                <xsl:comment>NB: Index is same as Arg1's </xsl:comment>
              </feat>
            </xsl:otherwise>
          </xsl:choose>
        </fs>
      </atomcat>
    </complexcat>
  </xsl:variable>

  <xsl:variable name="controlled-vp-inf-cat.2">
    <complexcat>
      <xsl:comment>NB: Word order and directionality of embedded clause's |(S|NP) 
type is an 'educated guess' from the syntactic type of TV. Change them
accordingly 
  </xsl:comment>
      <atomcat>
        <xsl:attribute name="type">
          <xsl:value-of select="$controlled.predicate.basic.type"/>
        </xsl:attribute>
        <fs id="3">
          <feat attr="vform" val="non-fin"/>
          <feat attr="index">
            <lf>
              <nomvar name="E2"/>
            </lf>
          </feat>
        </fs>
      </atomcat>
      <slash dir="|"/>
      <atomcat>
        <xsl:attribute name="type">
          <xsl:value-of select="$controlled.argument.basic.type"/>
        </xsl:attribute>
        <fs id="4">
          <xsl:comment> NB: If infinitive type is syntactic subject (which is the 
		  default), there is a built-in type in types.xml file called
		  "subject-case". Include in this type all the subjects that
		  can be controlled e.g. nominative subjects only (as in German), 
		  dative and nominative subjects (as in Malayalam) etc. In this
		  case, the controllee is not semantically restricted.
		  
		  If infinitive type is semantic subject, you need a larger
		  fragment of Hybrid Logic than HLDS uses to implement identity
		  of two event variables e.g.
		  
		  @_e(Arg1 a) ^ @_e(Arg1 b) --> @_a(b)
		  
		  where a is the event variable for controller verb, and
		  b is the event variable for the controlled verb, and Arg1 is the
		  modality for primary arguments (1s).
		  
		  This constraint is formulable in HL but HLDS does not cover that
		  fragment (yet). When it does, the lf tag should just stick in that
		  constraint.
		  
		  Currently, OpenCCG can generate an LF with TWO non-identical Arg1 modalities
		  in the same event structure, therefore some illicit examples would
		  go through.
		</xsl:comment>
          <xsl:choose>
            <xsl:when test="$infinitive.type = 'semantic'">
              <feat attr="index">
                <lf>
                  <nomvar name="X2"/>
                </lf>
                <xsl:comment>NB: Index is same as Arg2's </xsl:comment>
              </feat>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="make-attr-val">
                <xsl:with-param name="attr" select="'case'"/>
                <xsl:with-param name="val" select="'subject-case'"/>
              </xsl:call-template>
              <feat attr="index">
                <lf>
                  <nomvar name="X2"/>
                </lf>
                <xsl:comment>NB: Index is same as Arg2's  </xsl:comment>
              </feat>
            </xsl:otherwise>
          </xsl:choose>
        </fs>
      </atomcat>
    </complexcat>
  </xsl:variable>

  <xsl:template match="language">

   <!-- ** start output here ** -->

    <xsl:comment> 
      - This file is generated by parametric-lexicon.xsl to set up
          accusativity/ergativity parameter for IV and TV primary families
          and control primary families.

          NB: pre-CCG categories of parameters.xml are mapped to
              CCG categories in this file. From now on, it's all CCG

    Suggestions to start-up lexicon development:

      1) Copy this file to lexicon-base.xml to avoid losing your changes
         to it (remember, this file is auto-generated at the start)
      2) Edit lexicon-base.xml to modify the preset families and to add your 
          own families as needed (merging the entries of same family is
          left to you)
      3) Use the ccg-build facility of openCCG, which uses lexicon-base to build
          the lexicon.xml, morph.xml and rules.xml files needed by the system.
  </xsl:comment>

    <xsl:comment>

      *** Families derived from language parameters ***

   Includes primary entries for IV (unerg and unacc), basic TV, TV-control1, 
   TV-control2, IV-control1
   
  </xsl:comment>
    <xsl:comment>
  subject-case, s-case, p-case and a-case are value types that set up ergative-
  accusative mapping and surface cases of these argumnents (cf. types.xml file).
  Actual case values for them (e.g. nom for a-case in accusative languages,
  erg for a-case in ergative languages) are defined in types.xml file.
  You can of course refer to actual values since they are types,  but if a
  construction is related to GR mapping (ERG or ACC), it's better to use
  `subject-case' (which covers a-case and s-case in ACC; p-case and s-case in ERG).
  Types.xml sets these up from parameter specification.
  
  </xsl:comment>

    <ccg-lexicon xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../lexicon.xsd">

      <xsl:attribute name="name">
        <xsl:value-of select="@name"/>
      </xsl:attribute>


      <xsl:apply-templates select="parameters/iv"/>
      <xsl:apply-templates select="parameters/tv" mode="normal"/>
      <xsl:apply-templates select="parameters/tv" mode="control"/>

      <xsl:comment>

    *** End of derived families ***
 
        Add new families here, and merge the new entries for preset
         families as needed (e.g., you may add an entry to TV family
         for pro-dropping the subject etc.)
  </xsl:comment>


    </ccg-lexicon>

  </xsl:template>

  <xsl:template match="parameters/iv">

    <family name="unergative" pos="V" closed="true">
      <entry name="primary">
        <complexcat>
          <atomcat>
            <xsl:attribute name="type">
              <xsl:value-of select="predicate/@syn-type"/>
            </xsl:attribute>
            <xsl:copy-of select="$pred.0.E"/>
          </atomcat>
          <xsl:apply-templates select="setarg|arg">
            <xsl:with-param name="mode" select="'normal'"/>
          </xsl:apply-templates>
          <lf>
            <satop nomvar="E">
              <prop name="[*DEFAULT*]"/>
              <diamond mode="Arg1">
                <nomvar name="X1"/>
              </diamond>
              <diamond mode="Arg2">
                <nom name="one"/>
              </diamond>
            </satop>
          </lf>
        </complexcat>
      </entry>
    </family>

    <family name="unaccusative" pos="V" closed="true">
      <entry name="primary">
        <complexcat>
          <atomcat>
            <xsl:attribute name="type">
              <xsl:value-of select="predicate/@syn-type"/>
            </xsl:attribute>
            <xsl:copy-of select="$pred.0.E"/>
          </atomcat>
          <xsl:apply-templates select="setarg|arg">
            <xsl:with-param name="mode" select="'normal'"/>
          </xsl:apply-templates>
          <lf>
            <satop nomvar="E">
              <prop name="[*DEFAULT*]"/>
              <diamond mode="Arg2">
                <nomvar name="X1"/>
              </diamond>
              <diamond mode="Arg1">
                <nom name="one"/>
              </diamond>
            </satop>
          </lf>
        </complexcat>
      </entry>
    </family>
  </xsl:template>

  <xsl:template match="parameters/tv" mode="normal">

    <family name="TV" pos="V" closed="true">
      <entry name="primary">
        <complexcat>
          <atomcat>
            <xsl:attribute name="type">
              <xsl:value-of select="predicate/@syn-type"/>
            </xsl:attribute>
            <xsl:copy-of select="$pred.0.E"/>
          </atomcat>
          <xsl:apply-templates select="setarg|arg">
            <xsl:with-param name="mode" select="'normal'"/>
          </xsl:apply-templates>
          <lf>
            <satop nomvar="E">
              <prop name="[*DEFAULT*]"/>
              <diamond mode="Arg1">
                <nomvar name="X1"/>
              </diamond>
              <diamond mode="Arg2">
                <nomvar name="X2"/>
              </diamond>
            </satop>
          </lf>
        </complexcat>
      </entry>
    </family>
  </xsl:template>

  <xsl:template match="parameters/tv" mode="control">

    <family name="TV-control2" pos="V" closed="true">
      <entry name="primary">
        <complexcat>
          <atomcat>
            <xsl:attribute name="type">
              <xsl:value-of select="predicate/@syn-type"/>
            </xsl:attribute>
            <xsl:copy-of select="$pred.0.E"/>
          </atomcat>
          <xsl:apply-templates select="setarg|arg">
            <xsl:with-param name="mode" select="'control2-tv'"/>
          </xsl:apply-templates>
          <lf>
            <satop nomvar="E">
              <prop name="[*DEFAULT*]"/>
              <diamond mode="Arg1">
                <nomvar name="X1"/>
              </diamond>
              <diamond mode="Arg2">
                <nomvar name="X2"/>
              </diamond>
              <diamond mode="Arg3">
                <nomvar name="E2"/>
                <xsl:comment>NB: Argument is the PAS of the embedded S|NP  (cf. E2 above)
</xsl:comment>
              </diamond>
            </satop>
          </lf>
        </complexcat>
      </entry>
    </family>
    <family name="TV-control1" pos="V" closed="true">
      <entry name="primary">
        <complexcat>
          <atomcat>
            <xsl:attribute name="type">
              <xsl:value-of select="predicate/@syn-type"/>
            </xsl:attribute>
            <xsl:copy-of select="$pred.0.E"/>
          </atomcat>
          <xsl:apply-templates select="setarg|arg">
            <xsl:with-param name="mode" select="'control1-tv'"/>
          </xsl:apply-templates>
          <lf>
            <satop nomvar="E">
              <prop name="[*DEFAULT*]"/>
              <diamond mode="Arg1">
                <nomvar name="X1"/>
              </diamond>
              <diamond mode="Arg2">
                <nomvar name="X2"/>
              </diamond>
              <diamond mode="Arg3">
                <nomvar name="E2"/>
                <xsl:comment>NB: Argument is the PAS of the embedded S|NP  (cf. E2 above)
</xsl:comment>
              </diamond>
            </satop>
          </lf>
        </complexcat>
      </entry>
    </family>
    <family name="IV-control1" pos="V" closed="true">
      <entry name="primary">
        <complexcat>
          <atomcat>
            <xsl:attribute name="type">
              <xsl:value-of select="predicate/@syn-type"/>
            </xsl:attribute>
            <xsl:copy-of select="$pred.0.E"/>
          </atomcat>
          <xsl:apply-templates select="setarg|arg">
            <xsl:with-param name="mode" select="'control1-iv'"/>
          </xsl:apply-templates>
          <lf>
            <satop nomvar="E">
              <prop name="[*DEFAULT*]"/>
              <diamond mode="Arg1">
                <nomvar name="X1"/>
              </diamond>
              <diamond mode="Arg2">
                <nomvar name="E2"/>
                <xsl:comment>NB: Argument is the PAS of the embedded S|NP  (cf. E2 above)
</xsl:comment>
              </diamond>
            </satop>
          </lf>
        </complexcat>
      </entry>
    </family>
  </xsl:template>

  <xsl:template match="setarg">
    <xsl:param name="mode"/>

    <setarg>
      <xsl:apply-templates select="s-argument|a-argument|p-argument">
        <xsl:with-param name="mode" select="$mode"/>
      </xsl:apply-templates>
    </setarg>
  </xsl:template>

  <xsl:template match="arg">
    <xsl:param name="mode"/>

    <xsl:apply-templates select="s-argument|a-argument|p-argument">
      <xsl:with-param name="mode" select="$mode"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="a-argument">
    <xsl:param name="mode"/>

    <slash>
      <xsl:attribute name="dir">
        <xsl:value-of select="@dir"/>
      </xsl:attribute>
    </slash>
    <atomcat>
      <xsl:attribute name="type">
        <xsl:value-of select="@syn-type"/>
      </xsl:attribute>
      <fs id="1">
        <xsl:call-template name="make-attr-val">
          <xsl:with-param name="attr" select="'case'"/>
          <xsl:with-param name="val" select="'a-case'"/>
        </xsl:call-template>
        <feat attr="index">
          <lf>
            <nomvar name="X1"/>
          </lf>
        </feat>
      </fs>
    </atomcat>
  </xsl:template>

  <xsl:template match="p-argument">
    <xsl:param name="mode"/>

    <xsl:choose>
      <xsl:when test="$mode = 'normal'">
        <xsl:call-template name="set-p-argument-only"/>
      </xsl:when>
      <xsl:when test="$mode = 'control1-tv' or 'control2-tv' or 'control1-iv'">
        <xsl:call-template name="set-p-argument-and-control">
          <xsl:with-param name="mode" select="$mode"/>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="set-p-argument-only">

    <slash>
      <xsl:attribute name="dir">
        <xsl:value-of select="@dir"/>
      </xsl:attribute>
    </slash>
    <atomcat>
      <xsl:attribute name="type">
        <xsl:value-of select="@syn-type"/>
      </xsl:attribute>
      <fs id="2">
        <xsl:call-template name="make-attr-val">
          <xsl:with-param name="attr" select="'case'"/>
          <xsl:with-param name="val" select="'p-case'"/>
        </xsl:call-template>
        <feat attr="index">
          <lf>
            <nomvar name="X2"/>
          </lf>
        </feat>
      </fs>
    </atomcat>
  </xsl:template>

  <xsl:template name="set-p-argument-and-control">
    <xsl:param name="mode"/>
  
      <!-- S|NP comes after NP2 of matrix clause in SAME direction-->
    <slash>
      <xsl:attribute name="dir">
        <xsl:value-of select="@dir"/>
      </xsl:attribute>
    </slash>
    <xsl:choose>
      <xsl:when test="$mode = 'control1-tv'">
        <xsl:copy-of select="$controlled-vp-inf-cat.1"/>
        <slash>
          <xsl:attribute name="dir">
            <xsl:value-of select="@dir"/>
          </xsl:attribute>
        </slash>
        <atomcat>
          <xsl:attribute name="type">
            <xsl:value-of select="@syn-type"/>
          </xsl:attribute>
          <fs id="2">
            <xsl:call-template name="make-attr-val">
              <xsl:with-param name="attr" select="'case'"/>
              <xsl:with-param name="val" select="'p-case'"/>
            </xsl:call-template>
            <feat attr="index">
              <lf>
                <nomvar name="X2"/>
              </lf>
            </feat>
          </fs>
        </atomcat>
      </xsl:when>
      <xsl:when test="$mode = 'control1-iv'">
      <!-- just replace the NP2 of TV with S|NP with SAME direc.-->
        <xsl:copy-of select="$controlled-vp-inf-cat.1"/>
      </xsl:when>
      <xsl:when test="$mode = 'control2-tv'">
        <xsl:copy-of select="$controlled-vp-inf-cat.2"/>
        <slash>
          <xsl:attribute name="dir">
            <xsl:value-of select="@dir"/>
          </xsl:attribute>
        </slash>
        <atomcat>
          <xsl:attribute name="type">
            <xsl:value-of select="@syn-type"/>
          </xsl:attribute>
          <fs id="2">
            <xsl:call-template name="make-attr-val">
              <xsl:with-param name="attr" select="'case'"/>
              <xsl:with-param name="val" select="'p-case'"/>
            </xsl:call-template>
            <feat attr="index">
              <lf>
                <nomvar name="X2"/>
              </lf>
            </feat>
          </fs>
        </atomcat>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="s-argument">
    <xsl:param name="mode"/>

    <slash>
      <xsl:attribute name="dir">
        <xsl:value-of select="@dir"/>
      </xsl:attribute>
    </slash>
    <atomcat>
      <xsl:attribute name="type">
        <xsl:value-of select="@syn-type"/>
      </xsl:attribute>
      <fs id="1">
        <xsl:call-template name="make-attr-val">
          <xsl:with-param name="attr" select="'case'"/>
          <xsl:with-param name="val" select="'s-case'"/>
        </xsl:call-template>
        <feat attr="index">
          <lf>
            <nomvar name="X1"/>
          </lf>
        </feat>
      </fs>
    </atomcat>
  </xsl:template>

  <xsl:template name="make-attr-val">
    <xsl:param name="attr"/>
    <xsl:param name="val"/>

    <feat>
      <xsl:attribute name="attr">
        <xsl:value-of select="$attr"/>
      </xsl:attribute>
      <xsl:attribute name="val">
        <xsl:value-of select="$val"/>
      </xsl:attribute>
    </feat>
  </xsl:template>

</xsl:transform>
