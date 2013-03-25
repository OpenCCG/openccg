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

  <!--Transform which adds standard ccg rules -->
  
  <xsl:variable name="obj" select="java:opennlp.ccgbank.extract.RulesTally.new()"/>
  
  
  <!--Add attributes to the root for openccg-->
  <xsl:template match="rules"> 
    <rules name="protogrammar" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../rules.xsd">
      <!-- combinatory rules -->
      <application dir="forward"/> 
      <application dir="backward"/>
      <composition dir="forward" harmonic="true"/>
      <composition dir="backward" harmonic="true"/>
      <!-- <composition dir="forward" harmonic="false"/> -->
      <composition dir="backward" harmonic="false"/>
      <typeraising dir="forward" useDollar="false"/>
      <typeraising dir="backward" useDollar="true"/>
      <typeraising dir="backward" useDollar="true">
        <arg><atomcat type="pp"/></arg>
      </typeraising>
      <!-- process extracted rules -->
      <xsl:apply-templates/>
      <!-- add extra rules -->
      <xsl:call-template name="add-extra-rules"/>
    </rules>
  </xsl:template>
  
  <!-- For dev, mark rule as unmatched if no other template matches -->
  <xsl:template match="typechanging" mode="refine">
    <xsl:if test="java:keepUnmatched($obj)">
      <typechanging name="{@name}" unmatched="true">
        <xsl:apply-templates mode="refine"/>
      </typechanging>
    </xsl:if>
  </xsl:template>
  
  
  <!-- Skip rules equivalent to type raising ones (except clausal type raising handled below) -->
  <xsl:template match="typechanging[arg/atomcat[@type='np' or @type='pp'] 
		       and result/complexcat/atomcat[1][@type='s']]"/>
  
  <!-- Skip rules with $ in them (presumably equivalent to ones without $) -->
  <xsl:template match="typechanging[contains(@name,'$')]"/>

  <!-- Skip s[adj]\np clausal adjunction (subsumed by more general rule) -->
  <xsl:template match="typechanging[@name='s[adj]\np_to_s[adj]\np\(s[adj]\np)']"/>

  <!-- Skip bogus s[dcl]\np_to_np\np rule -->
  <xsl:template match="typechanging[@name='s[dcl]\np_to_np\np']"/>


  <!-- Add type changing rules when seen with sufficient frequency -->
  <xsl:template match="typechanging">
    <xsl:if test="java:checkRuleFreqStatus($obj,string(@name))">
      <xsl:apply-templates select="." mode="refine"/>
    </xsl:if>
  </xsl:template>

  
  <!-- Refine n_to_np with nil det, coindexation --> 
  <!-- nb: should restrict, but need to handle bare plurals, mass nouns and proper nouns -->
  <xsl:template match="typechanging[@name='n_to_np']" mode="refine">
    <typechanging name="n_to_np">
      <arg>
        <atomcat type="n">
          <fs id="2">
            <feat attr="index"> <lf> <nomvar name="X"/> </lf> </feat>
          </fs>
        </atomcat>
      </arg>
      <result>
        <atomcat type="np">
          <fs id="2">
            <feat attr="pers" val="3rd"/>
          </fs>
          <lf>
            <satop nomvar="X">
              <diamond mode="det"> <prop name="nil"/> </diamond>
            </satop>
          </lf>
        </atomcat>
      </result>
    </typechanging>
  </xsl:template>

  
  <!-- clausal modifiers -->
  <xsl:template match="typechanging[ 
		       arg/complexcat[atomcat[1][@type='s'] and atomcat[2][@type='np']]]" 
		mode="refine"> 
    <xsl:apply-templates select="." mode="refine-clausal"/>
  </xsl:template>
  
  <!-- clausal modifiers: s in arg -->
  <xsl:template match="arg/complexcat/atomcat[1][@type='s']" mode="refine-clausal">
    <atomcat type="{@type}">
      <fs>
        <feat attr="index"> <lf> <nomvar name="E"/> </lf> </feat>
        <xsl:apply-templates select="fs/*" mode="refine-clausal"/>
      </fs>
    </atomcat>
  </xsl:template>
  
  <!-- clausal modifiers: n|np -->
  <xsl:template match="atomcat[@type='n' or @type='np']" mode="refine-clausal">
    <atomcat type="{@type}">
      <fs id="2">
        <feat attr="index"> <lf> <nomvar name="X"/> </lf> </feat>
        <xsl:apply-templates select="fs/*" mode="refine-clausal"/>
      </fs>
    </atomcat>
  </xsl:template>
  
  <!-- clausal modifiers: s in result -->
  <xsl:template match="result//atomcat[@type='s']" mode="refine-clausal">
    <atomcat type="{@type}">
      <fs id="3">
        <feat attr="index"> <lf> <nomvar name="E0"/> </lf> </feat>
        <xsl:apply-templates select="fs/*" mode="refine-clausal"/>
      </fs>
    </atomcat>
  </xsl:template>
  
	
  <!-- clausal modifiers: add lf to postnominal result -->
  <xsl:template match="result/complexcat[atomcat[1][@type='np' or @type='n']]" mode="refine-clausal">
    <xsl:variable name="rel">
      <xsl:call-template name="clausal-mod-rel">
	<xsl:with-param name="arg" select="../../arg"/> 
      </xsl:call-template>
    </xsl:variable>
    <complexcat>
      <xsl:apply-templates mode="refine-clausal"/>
      <lf>
        <satop nomvar="X">
          <diamond mode="{$rel}"> <nomvar name="E"/> </diamond>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  
  <!-- clausal modifiers: add lf to vp result -->
  <xsl:template match="result/complexcat[atomcat[1][@type='s']]" mode="refine-clausal">
    <xsl:variable name="rel">
      <xsl:call-template name="clausal-mod-rel">
	<xsl:with-param name="arg" select="../../arg"/> 
      </xsl:call-template>
    </xsl:variable>
    <complexcat>
      <xsl:apply-templates mode="refine-clausal"/>
      <lf>
        <satop nomvar="E0">
          <diamond mode="{$rel}"> <nomvar name="E"/> </diamond>
        </satop>
      </lf>
    </complexcat>
  </xsl:template>
  
  <!-- determine Purpose or GenRel for clausal-mod rule -->
  <xsl:template name="clausal-mod-rel">
    <xsl:param name="arg"/>
    <xsl:choose>
      <xsl:when test="$arg/complexcat/atomcat[1]/fs/feat[@attr='form']/@val='to'">Purpose</xsl:when>
      <xsl:otherwise>GenRel</xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  
  <!-- clausal type raising -->
  <xsl:template match="typechanging[ 
		       @name='s[adj]\np_to_s\np\(s\np/(s[adj]\np))' or
		       @name='s[to]\np_to_s\np\(s\np/(s[to]\np))' ]" 
		mode="refine">
    <xsl:apply-templates select="." mode="refine-clausal-tr"/>
  </xsl:template>

  <!-- clausal TR: s in result -->
  <xsl:template match="result//atomcat[1][@type='s']" mode="refine-clausal-tr">
    <atomcat type="{@type}">
      <fs id="3">
        <xsl:apply-templates select="fs/*" mode="refine-clausal-tr"/>
      </fs>
    </atomcat>
  </xsl:template>
  
  <!-- clausal TR: np in result -->
  <xsl:template match="result//atomcat[2][@type='np']" mode="refine-clausal-tr">
    <atomcat type="{@type}">
      <fs id="4">
        <xsl:apply-templates select="fs/*" mode="refine-clausal-tr"/>
      </fs>
    </atomcat>
  </xsl:template>
  
  <!-- clausal TR: raised s in arg or result -->
  <xsl:template match="arg/complexcat/atomcat[1][@type='s'] | 
		       result/complexcat/complexcat/complexcat/atomcat[1][@type='s']" 
		mode="refine-clausal-tr">
    <atomcat type="{@type}">
      <fs id="1">
        <xsl:apply-templates select="fs/*" mode="refine-clausal-tr"/>
      </fs>
    </atomcat>
  </xsl:template>
  
  <!-- clausal TR: np in arg or result -->
  <xsl:template match="arg/complexcat/atomcat[2][@type='np'] | 
		       result/complexcat/complexcat/complexcat/atomcat[2][@type='np']" 
		mode="refine-clausal-tr">
    <atomcat type="{@type}">
      <fs id="2">
        <xsl:apply-templates select="fs/*" mode="refine-clausal-tr"/>
      </fs>
    </atomcat>
  </xsl:template>
  

  <!-- Some more unary rules (nb: these bypass frequency filter) -->

  <xsl:template match="typechanging[@name='s[dcl]_to_np\np']">
    <typechanging name="s[dcl]_to_np\np">
      <arg>
	<atomcat type="s">
          <fs>
            <feat attr="index">
              <lf>
                <nomvar name="E"/>
              </lf>
            </feat>
            <feat attr="form" val="dcl"/>
         </fs>
	</atomcat>
      </arg>
      <result>
	<complexcat>
          <atomcat type="np">
            <fs id="2">
              <feat attr="index">
		<lf>
                  <nomvar name="X"/>
		</lf>
              </feat>
            </fs>
          </atomcat>
          <slash dir="\" mode="^"/>
          <atomcat type="np">
            <fs id="2">
              <feat attr="index">
		<lf>
                  <nomvar name="X"/>
		</lf>
              </feat>
            </fs>
          </atomcat>
          <lf>
            <satop nomvar="X">
              <diamond mode="GenRel">
		<nomvar name="E"/>
              </diamond>
            </satop>
          </lf>
	</complexcat>
      </result>
    </typechanging>
  </xsl:template>
	
  <xsl:template match="typechanging[@name='s[dcl]_to_s\s']">
    <typechanging name="s[dcl]_to_s\s">
      <arg>
	<atomcat type="s">
          <fs>
            <feat attr="index">
              <lf>
                <nomvar name="E"/>
              </lf>
            </feat>
            <feat attr="form" val="dcl"/>
         </fs>
	</atomcat>
      </arg>
      <result>
	<complexcat>
          <atomcat type="s">
            <fs id="1">
              <feat attr="index">
		<lf>
                  <nomvar name="E0"/>
		</lf>
              </feat>
            </fs>
          </atomcat>
          <slash dir="\" mode="*"/>
          <atomcat type="s">
            <fs id="1">
              <feat attr="index">
		<lf>
                  <nomvar name="E0"/>
		</lf>
              </feat>
            </fs>
          </atomcat>
          <lf>
            <satop nomvar="E0">
              <diamond mode="GenRel">
		<nomvar name="E"/>
              </diamond>
            </satop>
          </lf>
	</complexcat>
      </result>
    </typechanging>
  </xsl:template>
	
  <xsl:template match="typechanging[@name='s[dcl]_to_s\np\(s\np)\(s\np\(s\np))']">
    <typechanging name="s[dcl]_to_s\np\(s\np)\(s\np\(s\np))">
      <arg>
	<atomcat type="s">
          <fs>
            <feat attr="index">
              <lf>
                <nomvar name="E"/>
              </lf>
            </feat>
            <feat attr="form" val="dcl"/>
          </fs>
	</atomcat>
      </arg>
      <result>
	<complexcat>
          <atomcat type="s">
            <fs inheritsFrom="1">
              <feat attr="mod-index">
		<lf>
                  <nomvar name="M"/>
		</lf>
              </feat>
            </fs>
          </atomcat>
          <slash dir="\" mode="&lt;"/>
          <atomcat type="np">
            <fs id="2">
              <feat attr="index">
		<lf>
                  <nomvar name="X2"/>
		</lf>
              </feat>
            </fs>
          </atomcat>
          <slash dir="\" mode="&lt;"/>
          <complexcat>
            <atomcat type="s">
              <fs id="1">
		<feat attr="index">
                  <lf>
                    <nomvar name="X1"/>
                  </lf>
		</feat>
              </fs>
            </atomcat>
            <slash dir="\" mode="&lt;"/>
            <atomcat type="np">
              <fs id="2">
		<feat attr="index">
                  <lf>
                    <nomvar name="X2"/>
                  </lf>
		</feat>
              </fs>
            </atomcat>
          </complexcat>
          <slash dir="\" mode="^"/>
          <complexcat>
            <atomcat type="s">
              <fs inheritsFrom="1">
		<feat attr="mod-index">
                  <lf>
                    <nomvar name="M"/>
                  </lf>
		</feat>
              </fs>
            </atomcat>
            <slash dir="\" mode="&lt;"/>
            <atomcat type="np">
              <fs id="2">
		<feat attr="index">
                  <lf>
                    <nomvar name="X2"/>
                  </lf>
		</feat>
              </fs>
            </atomcat>
            <slash dir="\" mode="&lt;"/>
            <complexcat>
              <atomcat type="s">
		<fs id="1">
                  <feat attr="index">
                    <lf>
                      <nomvar name="X1"/>
                    </lf>
                  </feat>
		</fs>
              </atomcat>
              <slash dir="\" mode="&lt;"/>
              <atomcat type="np">
		<fs id="2">
                  <feat attr="index">
                    <lf>
                      <nomvar name="X2"/>
                    </lf>
                  </feat>
		</fs>
              </atomcat>
            </complexcat>
          </complexcat>
          <lf>
            <satop nomvar="M">
              <diamond mode="GenRel">
		<nomvar name="E"/>
              </diamond>
            </satop>
          </lf>
	</complexcat>
      </result>
    </typechanging>
  </xsl:template>

  <xsl:template match="typechanging[@name='s[dcl]_to_s/s\(s/s)']">
    <typechanging name="s[dcl]_to_s/s\(s/s)">
      <arg>
	<atomcat type="s">
      	  <fs>
            <feat attr="index">
              <lf>
                <nomvar name="E"/>
              </lf>
            </feat>
            <feat attr="form" val="dcl"/>
          </fs>
      	</atomcat>
      </arg>
      <result>
	<complexcat>
          <atomcat type="s">
            <fs inheritsFrom="1">
              <feat attr="mod-index">
		<lf>
                  <nomvar name="M"/>
		</lf>
              </feat>
            </fs>
          </atomcat>
          <slash dir="/" mode="&gt;"/>
          <atomcat type="s">
            <fs id="1">
              <feat attr="index">
		<lf>
                  <nomvar name="X1"/>
		</lf>
              </feat>
            </fs>
          </atomcat>
          <slash dir="\" mode="^"/>
          <complexcat>
            <atomcat type="s">
              <fs inheritsFrom="1">
		<feat attr="mod-index">
                  <lf>
                    <nomvar name="M"/>
                  </lf>
		</feat>
              </fs>
            </atomcat>
            <slash dir="/" mode="&gt;"/>
            <atomcat type="s">
              <fs id="1">
		<feat attr="index">
                  <lf>
                    <nomvar name="X1"/>
                  </lf>
		</feat>
              </fs>
            </atomcat>
          </complexcat>
          <lf>
            <satop nomvar="M">
              <diamond mode="Mod">
		<nomvar name="E"/>
              </diamond>
            </satop>
          </lf>
	</complexcat>
      </result>
    </typechanging>
  </xsl:template>


  <!-- event nominalization (VP) -->
  <xsl:template match="typechanging[@name='s[ng]\np_to_np']">
    <typechanging name="s[ng]\np_to_np">
      <arg>
        <complexcat>
          <atomcat type="s">
            <fs id="1">
              <feat attr="index">
		<lf>
		  <nomvar name="E"/>
		</lf>
              </feat>
	      <feat attr="form" val="ng"/>
            </fs>
          </atomcat>
          <slash dir="\" mode="&lt;"/>
          <atomcat type="np">
            <fs id="2">
	      <feat attr="index">
                <lf>
                  <nomvar name="X2"/>
                </lf>
	      </feat>
            </fs>
          </atomcat>
        </complexcat>
      </arg>
      <result>
	<atomcat type="np">
          <fs>
	    <feat attr="index">
              <lf>
                <nomvar name="E"/>
              </lf>
	    </feat>
          </fs>
          <lf>
            <satop nomvar="E">
              <diamond mode="nom">
		<prop name="+"/>
              </diamond>
            </satop>
          </lf>
	</atomcat>
      </result>
    </typechanging>
  </xsl:template>

  <!-- event nominalization (S) -->
  <xsl:template match="typechanging[@name='s[ng]_to_np']">
    <typechanging name="s[ng]_to_np">
      <arg>
	<atomcat type="s">
          <fs id="1">
            <feat attr="index">
              <lf>
		<nomvar name="E"/>
              </lf>
            </feat>
	    <feat attr="form" val="ng"/>
          </fs>
	</atomcat>
      </arg>
      <result>
	<atomcat type="np">
          <fs>
	    <feat attr="index">
              <lf>
                <nomvar name="E"/>
              </lf>
	    </feat>
          </fs>
          <lf>
            <satop nomvar="E">
              <diamond mode="nom">
		<prop name="+"/>
              </diamond>
            </satop>
          </lf>
	</atomcat>
      </result>
    </typechanging>
  </xsl:template>

  <!-- 'to' rel clause with no subject (eg 'anything to say') -->
  <xsl:template match="typechanging[@name='s[to]\np/np_to_np\np']">
    <typechanging name="s[to]\np/np_to_np\np">
      <arg>
        <complexcat>
          <atomcat type="s">
            <fs id="1">
              <feat attr="index">
		<lf>
		  <nomvar name="E"/>
		</lf>
              </feat>
	      <feat attr="form" val="to"/>
            </fs>
          </atomcat>
          <slash dir="\" mode="&lt;"/>
          <atomcat type="np">
            <fs id="2">
	      <feat attr="index">
                <lf>
                  <nomvar name="X"/>
                </lf>
	      </feat>
            </fs>
          </atomcat>
          <slash dir="/" mode="&gt;"/>
          <atomcat type="np">
            <fs id="3">
	      <feat attr="index">
                <lf>
                  <nomvar name="Y"/>
                </lf>
	      </feat>
            </fs>
          </atomcat>
        </complexcat>
      </arg>
      <result>
	<complexcat>
          <atomcat type="np">
            <fs id="3">
              <feat attr="index">
		<lf>
                  <nomvar name="Y"/>
		</lf>
              </feat>
            </fs>
          </atomcat>
          <slash dir="\" mode="^"/>
          <atomcat type="np">
            <fs id="3">
              <feat attr="index">
		<lf>
                  <nomvar name="Y"/>
		</lf>
              </feat>
            </fs>
          </atomcat>
          <lf>
            <satop nomvar="Y">
              <diamond mode="GenRel">
		<nomvar name="E"/>
              </diamond>
            </satop>
          </lf>
	</complexcat>
      </result>
    </typechanging>
  </xsl:template>

  <!-- np topicalization -->
  <xsl:template match="typechanging[@name='np_to_s/(s/np)']">
    <typechanging name="np_to_s/(s/np)">
      <arg>
        <atomcat type="np">
          <fs id="2">
	    <feat attr="index">
	      <lf>
                <nomvar name="X2"/>
	      </lf>
            </feat>
          </fs>
        </atomcat>
      </arg>
      <result>
	<complexcat>
          <atomcat type="s">
            <fs id="1">
	      <feat attr="index">
		<lf>
                  <nomvar name="X1"/>
		</lf>
	      </feat>
            </fs>
          </atomcat>
          <slash dir="/" mode="&gt;"/>
	  <complexcat>
            <atomcat type="s">
              <fs id="1">
            	<feat attr="index">
              	  <lf>
                    <nomvar name="X1"/>
              	  </lf>
            	</feat>
              </fs>
            </atomcat>
	    <slash dir="/" mode="&gt;"/>
	    <atomcat type="np">
              <fs id="2">
            	<feat attr="index">
              	  <lf>
                    <nomvar name="X2"/>
              	  </lf>
            	</feat>
              </fs>
            </atomcat>
	  </complexcat>
          <lf>
            <satop nomvar="X2">
              <diamond mode="top">
		<prop name="+"/>
              </diamond>
            </satop>
          </lf>
	</complexcat>
      </result>
    </typechanging>
  </xsl:template>
  
  <!-- np type raising rule with np result (eg "some/all of which") -->
  <xsl:template match="typechanging[@name='np_to_np/(np\np)']">
    <typechanging name="np_to_np/(np\np)">
      <arg>
        <atomcat type="np">
          <fs id="2">
	    <feat attr="index">
	      <lf>
                <nomvar name="X2"/>
	      </lf>
            </feat>
          </fs>
        </atomcat>
      </arg>
      <result>
	<complexcat>
          <atomcat type="np">
            <fs id="1">
	      <feat attr="index">
		<lf>
                  <nomvar name="X1"/>
		</lf>
	      </feat>
            </fs>
          </atomcat>
          <slash dir="/" mode="^"/>
	  <complexcat>
            <atomcat type="np">
              <fs id="1">
            	<feat attr="index">
              	  <lf>
                    <nomvar name="X1"/>
              	  </lf>
            	</feat>
              </fs>
            </atomcat>
	    <slash dir="\" mode="^"/>
	    <atomcat type="np">
              <fs id="2">
            	<feat attr="index">
              	  <lf>
                    <nomvar name="X2"/>
              	  </lf>
            	</feat>
              </fs>
            </atomcat>
	  </complexcat>
	</complexcat>
      </result>
    </typechanging>
  </xsl:template>
  
  <!-- This may play the same role as CandC's np np => np binary rule -->
  <xsl:template match="typechanging[@name='np_to_np\np']">
    <typechanging name="np_to_np\np">
      <arg>
	<atomcat type="np">
	  <fs>
	    <feat attr="index">
	      <lf>
		<nomvar name="E"/>
	      </lf>
	    </feat>
	  </fs>
	</atomcat>
      </arg>
      <result>
	<complexcat>
	  <atomcat type="np">
	    <fs id="2">
	      <feat attr="index">
		<lf>
		  <nomvar name="X"/>
		</lf>
	      </feat>
	    </fs>
	  </atomcat>
	  <slash dir="\" mode="^"/>
	  <atomcat type="np">
	    <fs id="2">
	      <feat attr="index">
		<lf>
		  <nomvar name="X"/>
		</lf>
	      </feat>
	    </fs>
	  </atomcat>
	  <lf>
	    <satop nomvar="X">
	      <diamond mode="ApposRel">
		<nomvar name="E"/>
	      </diamond>
	    </satop>
	  </lf>
	</complexcat>
      </result>
    </typechanging>
  </xsl:template>


  <!-- extra rules -->
  <xsl:template name="add-extra-rules">
    <!-- sent sequencing (also need s[dcl] s[dcl] => s[dcl], as in CandC?) -->
    <typechanging name="sent_to_sents\sent">
      <arg>
	<atomcat type="sent">
          <fs>
            <feat attr="index">
              <lf>
		<nomvar name="E2"/>
              </lf>
            </feat>
          </fs>
	</atomcat>
      </arg>
      <result>
	<complexcat>
          <atomcat type="sents">
            <fs>
              <feat attr="index">
		<lf>
                  <nomvar name="E"/>
		</lf>
              </feat>
            </fs>
          </atomcat>
          <slash dir="\" mode="&lt;"/>
          <atomcat type="sent">
            <fs>
              <feat attr="index">
		<lf>
                  <nomvar name="E1"/>
		</lf>
              </feat>
            </fs>
          </atomcat>
          <lf>
            <satop nomvar="E">
              <prop name="SEQ"/>
              <diamond mode="Arg1">
		<nomvar name="E1"/>
              </diamond>
              <diamond mode="Arg2">
		<nomvar name="E2"/>
              </diamond>
            </satop>
          </lf>
	</complexcat>
      </result>
    </typechanging>
    <typechanging name="sents_to_sents\sent">
      <arg>
	<atomcat type="sents">
          <fs>
            <feat attr="index">
              <lf>
		<nomvar name="E2"/>
              </lf>
            </feat>
          </fs>
	</atomcat>
      </arg>
      <result>
	<complexcat>
          <atomcat type="sents">
            <fs>
              <feat attr="index">
		<lf>
                  <nomvar name="E"/>
		</lf>
              </feat>
            </fs>
          </atomcat>
          <slash dir="\" mode="&lt;"/>
          <atomcat type="sent">
            <fs>
              <feat attr="index">
		<lf>
                  <nomvar name="E1"/>
		</lf>
              </feat>
            </fs>
          </atomcat>
          <lf>
            <satop nomvar="E">
              <prop name="SEQ"/>
              <diamond mode="Arg1">
		<nomvar name="E1"/>
              </diamond>
              <diamond mode="Arg2">
		<nomvar name="E2"/>
              </diamond>
            </satop>
          </lf>
	</complexcat>
      </result>
    </typechanging>
  </xsl:template>


  <!-- refine copy rule-->
  <xsl:template match="@*|node()" mode="refine">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="refine"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- refine-clausal copy rule-->
  <xsl:template match="@*|node()" mode="refine-clausal">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="refine-clausal"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- refine-clausal-tr copy rule-->
  <xsl:template match="@*|node()" mode="refine-clausal-tr">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"
      mode="refine-clausal-tr"/>
    </xsl:copy>
  </xsl:template>
  
  <!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:transform>
