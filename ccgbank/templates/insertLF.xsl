<!--Copyright (C) 2005-2009 Scott Martin, Rajakrishan Rajkumar and Michael White
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.-->

<xsl:transform
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xalan xalan2 java">

  <xsl:output method="xml" indent="yes" xalan2:indent-amount="2"/>
  <xsl:strip-space elements="*"/>
  
  
  <!--Transform which takes in as input the lexicon.xml file and outputs an augmented lexicon.xml with LF-->
  
  <!--Java Program in the grammar extractor package invoked-->
  <xsl:variable name="obj" select="java:opennlp.ccgbank.extract.InsertLFHelper.new()"/>
  
  <!-- root -->
  <xsl:template match="ccg-lexicon">
    <ccg-lexicon xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../lexicon.xsd" name="protogrammar">
      <xsl:apply-templates/>
    </ccg-lexicon>
  </xsl:template>

  
  <!-- nouns (of various kinds) -->
  <!-- adding existential 'there' and 'it' for now -->
  <xsl:template match="family[
      @name='np_9' or @name='n_9' or @name='n_1' or @name='np_1' or @name='n[num]_1' or @name='np[thr]_1' or @name='np[expl]_1'
    ]/entry/atomcat"
  >
    <atomcat type="{@type}">
      <!-- 
      <xsl:variable name="dummy1" select="java:initFeat($obj)"/>
      <xsl:variable name="dummy2" select="java:putFeat($obj,'num')"/>
      -->
      <xsl:apply-templates/>
      <lf>
        <!--<satop nomvar="X1"> <prop name="[*DEFAULT*]"/> </satop>-->
				<satop nomvar="X{fs/@id}"> <prop name="[*DEFAULT*]"/> </satop>
      </lf>
    </atomcat>
  </xsl:template>

  <!-- n w/ arg -->
  <xsl:template match="family[@name='n_1/n[num]_2' or @name='n_1/s[em]_2']/entry/complexcat">
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <prop name="[*DEFAULT*]"/>
          <diamond mode="Arg"> <nomvar name="X2"/> </diamond>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>

  
  
  <!-- determiners -->
  <!-- nb: should be np_1/^n_1 -->
  <xsl:template match="family[@name='np_1/^n_1' or @name='np_1/n_1']/entry/complexcat">
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <diamond mode="Det"> 
            <nomvar name="D"/> <prop name="[*DEFAULT*]"/> 
          </diamond>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>

  
  <!-- possessive -->
  <xsl:template match="family[@pos='POS' and @name='np_1/n_1\np_2']/entry/complexcat">
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <diamond mode="GenOwn"> <nomvar name="X2"/> </diamond>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  
  
  <!-- sentence-final punct -->
  <xsl:template match="family[@pos='.' and starts-with(@name,'sent_1')]/entry/complexcat">
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <diamond mode="mood"> <prop name="[*DEFAULT*]"/> </diamond>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>

  <!-- nominal pre- and post-modifiers -->
  <!-- eg: n_~1/n_1 or np_~1\np_1 -->
  <!-- nb: pred adj separate -->
  <!-- nb: should look at np_1\np_1/n_2 POS: DT -->
  <xsl:template match="family/entry/complexcat[
    (atomcat[1][@type='n'] and *[3][self::atomcat][@type='n']) or (atomcat[1][@type='np'] and *[3][self::atomcat][@type='np'])]"
  >
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <diamond mode="Mod">
            <nomvar name="M"/> <prop name="[*DEFAULT*]"/>
            <xsl:call-template name="add-ArgN"/>
          </diamond>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  
  <!-- nominal modifier modifiers -->
  <!-- eg: n_~1/n_1/(n_~1/n_1) or np_~1\np_1/(np_~1\np_1) -->
  <xsl:template match="family/entry[
      count(.//complexcat[
        (atomcat[1][@type='n'] and *[3][self::atomcat][@type='n']) or (atomcat[1][@type='np'] and *[3][self::atomcat][@type='np'])
      ]) = 2
    ]/complexcat"
  >
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="M">
          <diamond mode="Mod">
            <nomvar name="R"/> <prop name="[*DEFAULT*]"/>
            <xsl:call-template name="add-ArgN">
              <xsl:with-param name="min-pos" select="7"/>
            </xsl:call-template>
          </diamond>	  	  
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  
  
  <!-- tbd: nominal modifier modifier modifiers (!, eg "not"): np_~1\np_1/(np_3\np_1)/(np_~1\np_1/(np_3\np_1)) -->
  <xsl:template match="family/entry[
      count(.//complexcat[
        (atomcat[1][@type='n'] and *[3][self::atomcat][@type='n']) or (atomcat[1][@type='np'] and *[3][self::atomcat][@type='np'])
      ]) = 4
    ]/complexcat"
  >
    <complexcat>
      <xsl:apply-templates/>
    </complexcat>
  </xsl:template>

  
  <!-- tbd (?? for "in", "to"): n_~1/n_1\(n_~1/n_1)/(n_3/n_4) -->
  <!-- tbd (?? for "just", "roughly"): n_~1/n_1/(n_3/n_1)/(n_~1/n_1/(n_3/n_1)) -->
  <!-- tbd (?? for "as", "well"): np_1\*np_2\*punct[,]/*np_4/*(np_1\*np_2\*punct[,]/*np_4) -->
  <xsl:template match="family[@name='n_~1/n_1\(n_~1/n_1)/(n_3/n_4)' or @name='n_~1/n_1/(n_3/n_1)/(n_~1/n_1/(n_3/n_1))' or @name='np_1\*np_2\*punct[,]/*np_4/*(np_1\*np_2\*punct[,]/*np_4)']
    /entry/complexcat"
  >
    <complexcat>
      <xsl:apply-templates/>
    </complexcat>
  </xsl:template>
  
  
  <!-- relative pronouns -->
  <xsl:template match="family[@indexRel='GenRel']/entry/complexcat">
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <diamond mode="GenRel"> <nomvar name="X2"/> </diamond>	  	  
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  
  <!-- tbd: free relatives, np_1/(s[dcl]_2/np_3) -->

  
  <!-- canonical pred and args (Arg0 .. ArgN) -->
  <!-- exclude: vp modifiers, passives, expletive subjects -->
  <!-- nb: leaving out expletive 'there' and 'it' for now -->
  <xsl:template match="family/entry/complexcat[
    atomcat[1][@type='s']/fs[@id='1' and not(feat[@attr='form']/@val='pss')] and 
    atomcat[2][@type='np']/fs[@id='2'] and
    not(complexcat/atomcat[1][@type='s']/fs[@id='1'])
  ]">
    <!--
    atomcat[2][@type='np']/fs[@id='2' and not(feat[@attr='form' and (@val='thr' or @val='expl')])] and
    -->
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <prop name="[*DEFAULT*]"/>
          <diamond mode="Arg0"> <nomvar name="X2"/> </diamond>
          <!-- 
            vp args: in s\np_2/x_n/../x_3, x_3 to x_n map to Arg1 to ArgN-2, 
            except with double object (np np), where the order is reversed 
          -->
          <xsl:choose>
            <!-- Arg2 Arg1 for ind-obj dir-obj, ie s\np/np[X3]_3/np[X4]_4 -->
            <xsl:when test="atomcat[3][@type='np']/fs[@id='3'] and atomcat[4][@type='np']/fs[@id='4']">
              <diamond mode="Arg1"> <nomvar name="X3"/> </diamond>
              <diamond mode="Arg2"> <nomvar name="X4"/> </diamond>
            </xsl:when>
            <!-- general case: assign ArgN based on num args from end -->
            <xsl:otherwise>
              <xsl:call-template name="add-ArgN"/>
            </xsl:otherwise>
          </xsl:choose>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  

  <!-- passive: subj is Arg1, rest are Arg2..N; exception for ditrans -->
  <!-- tbd: should mark passive topic with a (relational) feature -->
  <!-- tbd: by-PP as Arg0 (but not in sect 00?) -->
  <!-- nb: indices wrong on passives with s[pss]\np/(pp/np) -->
  <xsl:template match="family[starts-with(@name,'s[pss]_1\np_2') and @name!='s[pss]_1\np_2/np_3']/entry/complexcat">
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <prop name="[*DEFAULT*]"/>
          <diamond mode="Arg1"> <nomvar name="X2"/> </diamond>
          <xsl:call-template name="add-ArgN">
            <xsl:with-param name="first-arg-num" select="2"/>
          </xsl:call-template>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  
  
  <!-- ditrans passive: subj is Arg2, obj is Arg1 -->
  <!-- nb: also possible, but rare, for subj to be Arg1; this isn't marked in CCGbank though -->
  <xsl:template match="family[@name='s[pss]_1\np_2/np_3']/entry/complexcat">
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <prop name="[*DEFAULT*]"/>
          <diamond mode="Arg2"> <nomvar name="X2"/> </diamond>
          <diamond mode="Arg1"> <nomvar name="X3"/> </diamond>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  
  
  <!-- non-canonical s/.. pred and args (Arg0 .. ArgN) -->
  <!-- exclude: passives, s modifiers (implicit), s modifier modifiers -->
  <!-- nb: this works (somewhat) with 'who', but would be better to introduce bound var -->
  <xsl:template match="family[not(@indexRel)]/entry/complexcat[
    atomcat[1][@type='s']/fs[@id='1' and not(feat[@attr='form']/@val='pss')] and 
    slash[1][@dir='/'] and
    not(complexcat/atomcat[1][@type='s']/fs[@id='1'])
  ]">
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <prop name="[*DEFAULT*]"/>
          <!-- 
            args: in s/x_n/../x_1, x_1 to x_n map to Arg0 to ArgN-1 
          -->
          <xsl:call-template name="add-ArgN">
            <xsl:with-param name="min-pos" select="3"/>
            <xsl:with-param name="first-arg-num" select="0"/>
          </xsl:call-template>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  

  <!-- s\.. pred and args, eg s[dcl]_1\s[dcl]_2\np_3, as in "S, reporters said" (or inverted) -->
  <xsl:template match="family[@name='s[dcl]_1\s[dcl]_2\np_3' or @name='s[dcl]_1\s[dcl]_2/np_3']/entry/complexcat">
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <prop name="[*DEFAULT*]"/>
          <diamond mode="Arg0"> <nomvar name="X3"/> </diamond>
          <diamond mode="Arg1"> <nomvar name="X2"/> </diamond>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  
  <!-- nb: can also have s[dcl]_1\s[dcl]_2\np_3/np_4, "told" -->
  <xsl:template match="family[@name='s[dcl]_1\s[dcl]_2\np_3/np_4']/entry/complexcat">
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <prop name="[*DEFAULT*]"/>
          <diamond mode="Arg0"> <nomvar name="X3"/> </diamond>
          <diamond mode="Arg1"> <nomvar name="X4"/> </diamond>
          <diamond mode="Arg2"> <nomvar name="X2"/> </diamond>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  
  
  <!-- expletive subjects, eg s[dcl]_1\np[expl]_2/(s[to]_3\np_4)/(s[adj]_5\np_6) -->
  <!-- also: s[dcl]_1\np[thr]_2/(s[b]_3\np_4) -->
  <!-- rightward args:  Arg0, then Arg1 if present -->
  <!-- nb: should perhaps change pred, eg to there-be -->
  <!-- nb: should check arg assignments for there-be -->
  <!-- nb: leaving out expletive 'there' and 'it' for now -->
  <!--
  <xsl:template match="family/entry/complexcat[
    atomcat[1][@type='s']/fs[@id='1'] and 
    atomcat[2][@type='np']/fs[@id='2' and feat[@attr='form' and (@val='thr' or @val='expl')]]
  ]">
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <prop name="[*DEFAULT*]"/>
          <xsl:variable name="arg0" select="*[not(self::slash)][3]"/>
          <diamond mode="Arg0"> 
            <nomvar name="X{$arg0/fs/@id | $arg0/atomcat[1]/fs/@id}"/>
          </diamond>
          <xsl:variable name="arg1" select="*[not(self::slash)][4]"/>
          <xsl:if test="$arg1">
            <diamond mode="Arg1"> 
              <nomvar name="X{$arg1/fs/@id | $arg1/atomcat[1]/fs/@id}"/>
            </diamond>
          </xsl:if>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  -->
  
  <!-- s modifier (pre or post) -->
  <xsl:template match="family[not(@indexRel)]/entry/complexcat[
    atomcat[1][@type='s']/fs[@inheritsFrom='1'] and atomcat[2][@type='s']/fs[@id='1']
  ]">
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <diamond mode="Mod">
            <nomvar name="M"/> <prop name="[*DEFAULT*]"/>
            <xsl:call-template name="add-ArgN"/>
          </diamond>	  	  
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  
  
  <!-- s modifier modifiers -->
  <!-- eg: s_~1/s_1/(s_~1/s_1) -->
  <xsl:template match="family/entry[
      count(.//complexcat[
        atomcat[1][@type='s'] and *[3][self::atomcat][@type='s']
      ]) = 2
    ]/complexcat"
  >
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="M">
          <diamond mode="Mod">
            <nomvar name="R"/> <prop name="[*DEFAULT*]"/>
            <xsl:call-template name="add-ArgN">
              <xsl:with-param name="min-pos" select="7"/>
            </xsl:call-template>
          </diamond>	  	  
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  
  
  <!-- vp modifier (pre or post) -->
  <!-- eg: s_~1\np_2\(s_1\np_2)/np_3 -->
  <xsl:template match="family[not(@indexRel)]/entry/complexcat[
    atomcat[1][@type='s']/fs[@inheritsFrom='1'] and atomcat[2][@type='np']/fs[@id='2'] and
    complexcat[1][atomcat[1][@type='s']/fs[@id='1']]
  ]">
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <diamond mode="Mod">
            <nomvar name="M"/> <prop name="[*DEFAULT*]"/>
            <xsl:call-template name="add-ArgN">
              <xsl:with-param name="min-pos" select="7"/>
            </xsl:call-template>
          </diamond>	  	  
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  

  <!-- vp modifier modifier, eg: s_~1\np_2\(s_1\np_2)/(s_~1\np_2\(s_1\np_2)) -->
  <xsl:template match="family/entry[
      count(.//complexcat[
        atomcat[1][@type='s'] and *[3][self::atomcat][@type='np']
      ]) = 4
    ]/complexcat"
  >
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="M">
          <diamond mode="Mod">
            <nomvar name="R"/> <prop name="[*DEFAULT*]"/>
            <xsl:call-template name="add-ArgN">
              <xsl:with-param name="min-pos" select="9"/>
            </xsl:call-template>
          </diamond>	  	  
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  

  <!-- tbd (??, "than"): s_~1\np_2\(s_3\np_4)/(s_5\np_6\(s_7\np_8))/(s_1\np_2\(s_3\np_4)/(s_5\np_6\(s_7\np_8)))\(s[adj]_9\np_10) -->
  
  <!-- tbd: look at auxv -->  

  <!-- nominal conj, eg
    np_1\*np_2/*np_3
    np_1\*np_2\*punct[,]/*np_4
  -->
  <xsl:template match="family[not(@indexRel) and (@pos='CC' or @pos='PUNCT_CONJ' or @pos1='PUNCT_CONJ')]/entry/complexcat[
    atomcat[1][starts-with(@type,'n')]/fs[@id='1'] and 
    atomcat[2][starts-with(@type,'n')]/fs[@id='2'] and 
    atomcat[starts-with(@type,'n')]/fs[@id='3' or @id='4']
  ]">
    <complexcat>
      <xsl:apply-templates/>
      <xsl:variable name="rt-id" select="atomcat[starts-with(@type,'n')]/fs[@id='3' or @id='4']/@id"/>
      <lf>
        <satop nomvar="X1">
					<xsl:choose>
						<xsl:when test="ancestor::family[@pred]">
          		<prop name="{ancestor::family/@pred}"/>
						</xsl:when>
						<xsl:otherwise>
							<prop name="[*DEFAULT*]"/>
						</xsl:otherwise>
					</xsl:choose>
          <diamond mode="First"> <nomvar name="X2"/> </diamond> 
          <diamond mode="Next"> <nomvar name="X{$rt-id}"/> </diamond> 
        </satop>
      </lf>
    </complexcat>
  </xsl:template>

  <!-- verbal coord, eg
    s[dcl]_1$_1\*(s[dcl]_2$_1)/*(s[dcl]_3$_1)
    s[pss]_1$_1\*(s[pss]_2$_1)\*punct[,]/*(s[pss]_4$_1)
  -->
  <xsl:template match="family[not(@indexRel)]/entry/complexcat[
    atomcat[1][@type='s']/fs[@id='1'] and dollar and
    complexcat[atomcat[1][@type='s']/fs[@id='2'] and dollar] and
    complexcat[atomcat[1][@type='s']/fs[@id='3' or @id='4'] and dollar]
  ]">
    <complexcat>
      <xsl:apply-templates/>
      <xsl:variable name="rt-id" select="complexcat/atomcat[1][@type='s']/fs[@id='3' or @id='4']/@id"/>
      <lf>
        <satop nomvar="X1">
          <prop name="[*DEFAULT*]"/>
          <diamond mode="First"> <nomvar name="X2"/> </diamond> 
          <diamond mode="Next"> <nomvar name="X{$rt-id}"/> </diamond> 
        </satop>
      </lf>
    </complexcat>
  </xsl:template>

  <!-- tbd: other conj -->

	<!--PP conjn-->

  <xsl:template match="family[(@pos='CC' or @pos='PUNCT_CONJ' or @pos1='PUNCT_CONJ')]/entry/complexcat[
    atomcat[1][starts-with(@type,'pp')]/fs[@id='1'] and 
    atomcat[2][starts-with(@type,'pp')]/fs[@id='2'] and 
    atomcat[starts-with(@type,'pp')]/fs[@id='3' or @id='4']
  ]">
    <complexcat>
      <xsl:apply-templates/>
      <xsl:variable name="rt-id" select="atomcat[starts-with(@type,'pp')]/fs[@id='3' or @id='4']/@id"/>
      <lf>
        <satop nomvar="X1">
          <prop name="[*DEFAULT*]"/>
          <diamond mode="First"> <nomvar name="X2"/> </diamond> 
          <diamond mode="Next"> <nomvar name="X{$rt-id}"/> </diamond> 
        </satop>
      </lf>
    </complexcat>
  </xsl:template>


  <!-- tbd: 'easy', 'tough' -->  
  <!-- tbd: pp_1/pp_1 -->

  
  <!-- LF variable insertion in the syntax, excluding no sem cases -->
  <xsl:template match="family[not(@indexRel='*NoSem*')]//fs[@id]">
    <fs> 
      <xsl:apply-templates select="@*|node()"/>
      <feat attr="index"><lf><nomvar name="X{@id}"/></lf></feat>
      <!-- num feat -->
      <!-- 
      <xsl:variable name="feat" select="java:getFeat($obj)"/>
      <xsl:if test = "$feat != 'xxx'">
        <feat attr="{$feat}"><featvar name="NUM"/></feat>
      </xsl:if>
      -->
    </fs>
  </xsl:template>

  <xsl:template match="family[not(@indexRel='*NoSem*')]//fs[@inheritsFrom]">
    <fs> 
      <xsl:apply-templates select="@*|node()"/>
      <feat attr="mod-index"><lf><nomvar name="M"/></lf></feat>
    </fs>
  </xsl:template>

  
  <!-- special case for 'during which': need to unify indices for X2 and X3 -->
  <!-- 
  during :- np_~1\np_1/np_2 : @X1(<Mod>(M ^ during ^ <Arg1>X2))
  
  goal:
  during which :- np_~1\np_1/s[dcl_2] : @X1(<GenRel>(E ^ <Mod>(M ^ during ^ <Arg1>X1)))
  
  thus:
  which :- np_~1\np_1/s[dcl]_2\(np_3[X2]\np_4/np_1) : @X1(<GenRel>X2) 
  -->
  <xsl:template match="family[@name='np_~1\np_1/s[dcl]_2\(np_3\np_4/np_1)']//fs[@id='3']">
    <fs> 
      <xsl:apply-templates select="@*|node()"/>
      <feat attr="index"><lf><nomvar name="X2"/></lf></feat>
    </fs>
  </xsl:template>
  

  <!-- general case: assign ArgN based on num args from end -->
  <!-- eg, in s\np_2/x_n/../x_3, x_3 to x_n map to Arg1 to ArgN-2 -->
  <xsl:template name="add-ArgN">
    <xsl:param name="min-pos" select="5"/>
    <xsl:param name="first-arg-num" select="1"/>
    <xsl:variable name="compl" select="*[position() &gt;= $min-pos and not(self::slash) and not(self::atomcat and @type='punct')]"/>
    <xsl:variable name="num-compl" select="count($compl)"/>
    <xsl:for-each select="xalan:nodeset($compl)">
      <diamond mode="Arg{$first-arg-num + ($num-compl - position())}"> 
        <nomvar name="X{fs/@id | atomcat[1]/fs/@id}"/> 
      </diamond>
    </xsl:for-each>
  </xsl:template>


  <!-- cats with roles from propbank -->
  <!-- nb: assuming ~ distinguished modifier cats -->
  <xsl:template match="family[@argRoles and not(contains(@name,'~'))]/entry/complexcat">
    <xsl:if test="java:setRoles($obj,ancestor::family/@argRoles)"/>
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <prop name="[*DEFAULT*]"/>
          <xsl:variable name="compl" select="*[position() &gt;= 3 and not(self::slash) and not(self::atomcat and @type='punct')]"/>
          <xsl:for-each select="xalan:nodeset($compl)">
            <xsl:variable name="role" select="java:getRole($obj,position()-1)"/>
            <xsl:if test="$role != 'null' and $role != 'e'">
              <diamond mode="{$role}"> 
                <nomvar name="X{fs/@id | atomcat[1]/fs/@id}"/> 
              </diamond>
            </xsl:if>
          </xsl:for-each>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>

  <!-- modifier cats -->
  <xsl:template match="family[@argRoles and contains(@name,'~')]/entry/complexcat">
    <xsl:if test="java:setRoles($obj,ancestor::family/@argRoles)"/>
    <complexcat>
      <xsl:apply-templates/>
      <lf>
        <satop nomvar="X1">
          <diamond mode="ArgM">
            <nomvar name="M"/> <prop name="[*DEFAULT*]"/>
            <xsl:variable name="compl" select="*[position() &gt;= 3 and not(self::slash) and not(self::atomcat and @type='punct')]"/>
            <xsl:for-each select="xalan:nodeset($compl)">
              <xsl:variable name="role" select="java:getRole($obj,position()-1)"/>
              <!-- exclude ArgM here -->
              <xsl:if test="$role != 'null' and $role != 'e' and $role != 'ArgM'"> 
                <diamond mode="{$role}"> 
                  <nomvar name="X{fs/@id | atomcat[1]/fs/@id}"/> 
                </diamond>
              </xsl:if>
            </xsl:for-each>
          </diamond>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>

  
  <!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:transform>
