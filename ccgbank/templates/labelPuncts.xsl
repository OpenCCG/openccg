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

<xsl:output method="xml" indent="yes" xalan2:indent-amount="2" omit-xml-declaration = "yes"/>

<xsl:strip-space elements="*"/>

<!--Java helper class-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>
<!--Transform which identifies & labels various punct cat part of speech -Part II-->

<xsl:template match="/">
  	<xsl:apply-templates/>
</xsl:template>

<!--APPOS_VRB stray case appositive-->
<xsl:template match="Leafnode[@pos1=',' and parent::Treenode/@cat0='NP\NP' and following-sibling::Treenode[@cat0='NP\NP' and *[3]/@pos1=',']]">

	<Leafnode cat="{@cat}" lexeme="{@lexeme}" pos1="PUNCT_NP_MOD" cat0="{@cat0}">
		<xsl:apply-templates select="@*[not(name()='pos1')]"/>
  	<atomcat type=",">
    	<fs id="1"/>
    </atomcat>
  </Leafnode>
	<xsl:variable name="dummy1" select="java:initPOS($obj)"/>
</xsl:template>

<xsl:template match="Leafnode[@pos1=',' and parent::Treenode[preceding-sibling::*/@cat0='N/N' and @cat0='N'] and following-sibling::Treenode/@cat0='N']">

	<Leafnode cat="{@cat}" lexeme="{@lexeme}" pos1="PUNCT_NOM_CONJ" cat0="{@cat0}">
		<xsl:apply-templates select="@*[not(name()='pos1')]"/>
  	<atomcat type=",">
    	<fs id="1"/>
    </atomcat>
  </Leafnode>

</xsl:template>

<!--Label comma which introduces a pre-sentential adjunct-->
<xsl:template match="Leafnode[parent::Treenode/@cat0='S[dcl]' and @pos1=',' and following-sibling::Treenode/@cat0='S[dcl]']">

	<xsl:choose>

		<xsl:when test="../../*[2]/@dtr='1' and not(parent::*/parent::*/descendant::Leafnode[1][contains(@cat0,'S/S')])">
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_INIT_ADJ-ARG')"/>
		</xsl:when>
	
		<xsl:otherwise>
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_SENT_ADJ')"/>
		</xsl:otherwise>

	</xsl:choose>
	
	<xsl:variable name="pos" select="java:getPOS($obj)"/>
	<Leafnode cat=",_1" lexeme="," pos1="{$pos}"  cat0=",">
		<xsl:apply-templates select="@*[not(name()='pos1')]"/>
    <atomcat type=",">
    	<fs id="1"/>
  	</atomcat>
	</Leafnode>
	<xsl:variable name="dummy0" select="java:initPOS($obj)"/>
</xsl:template>

<xsl:template match="Treenode[*[2][@pos1='PUNCT_LEX_CONJ' and preceding::Leafnode[1]/@pos1='PUNCT_APPOS_BAL']]">
	<xsl:apply-templates select="*[3]"/>
</xsl:template>

<!--Lex conj balancing comma
<xsl:template match="Leafnode[@pos1=',' and preceding-sibling::Treenode[1][*[3]/*[2]/@pos1='PUNCT_LEX_CONJ']]">

	<Leafnode cat="{@cat}" lexeme="{@lexeme}" pos1="PUNCT_LEX_CONJ_BAL" cat0="{@cat0}">
		<xsl:apply-templates select="@*[not(name()='pos1')]"/>
  	<atomcat type=",">
    	<fs id="1"/>
    </atomcat>
  </Leafnode>
</xsl:template>-->


<!--Pre and post vp commas-->

<xsl:template match="Leafnode[@pos1=',' and preceding-sibling::Treenode[java:purgeCat($obj,@cat)='s\np' and java:purgeCat($obj,../@cat)='s\np']]">

	<xsl:variable name="sentId" select="ancestor::*[@Header]/@Header"/>

	<xsl:choose>

		<xsl:when test="following::Treenode[1][ancestor::*[@Header=$sentId] and @cat0='(S\NP)\(S\NP)' and @dtr='1']">
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_EMPH_FINAL_VRB')"/>
    </xsl:when>

		<xsl:when test="following::Treenode[1][ancestor::*[@Header=$sentId] and @cat0='(S\NP)\(S\NP)']">
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_EMPH_FINAL')"/>
    </xsl:when>

		<xsl:otherwise>
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_POST-VP_ADJ')"/>
		</xsl:otherwise>

	</xsl:choose>

	<xsl:variable name="pos" select="java:getPOS($obj)"/>
	<Leafnode cat=",_1" lexeme="," pos1="{$pos}" cat0=",">
		<xsl:apply-templates select="@*[not(name()='pos1')]"/>
    <atomcat type=",">
    	<fs id="1"/>
  	</atomcat>
	</Leafnode>	
</xsl:template>
<xsl:template match="Leafnode[@pos1=',' and following-sibling::Treenode[java:purgeCat($obj,@cat)='s\np' and java:purgeCat($obj,../@cat)='s\np']]">

	<Leafnode cat=",_1" lexeme="," pos1="PUNCT_PRE-VP_ADJ" cat0=",">
		<xsl:apply-templates select="@*[not(name()='pos1')]"/>
    <atomcat type=",">
    	<fs id="1"/>
  	</atomcat>
	</Leafnode>	
</xsl:template>


<!--Verbal Parentheticals 2-->
<xsl:template match="Leafnode[parent::Treenode/@cat0='(S\NP)\(S\NP)' and @pos1=',' and following-sibling::Treenode/@cat0='(S\NP)\(S\NP)']"> 

	<xsl:variable name="anch" select="following::Leafnode[1]"/>

	<xsl:choose>
		
		<xsl:when test="following-sibling::Treenode[1]/*[2][@cat0='S[dcl]\S[dcl]' or @cat0='S[dcl]/S[dcl]' or @cat0='S[dcl]']">
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_SAY5')"/>
    </xsl:when>

    <xsl:when test="$anch[not(../following-sibling::Leafnode[1])]/*[1]/*[position()=last()-1 and @dir='/']">
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_VPAREN3')"/>
    </xsl:when>
		
		<xsl:when test="$anch/*[1]/*[position()=last()-1 and @dir='\']">
			<xsl:variable name="dummy0" select="java:storePOS($obj,'PUNCT_VPAREN4')"/>
    </xsl:when>
		<xsl:otherwise>
			<xsl:variable name="dummy0" select="java:storePOS($obj,',')"/>
		</xsl:otherwise>
	</xsl:choose>

	<xsl:variable name="pos" select="java:getPOS($obj)"/>

	<xsl:choose>
		<xsl:when test="not($pos=',')">
			<Leafnode cat=",_1" lexeme="," pos1="{$pos}" cat0=",">
				<xsl:apply-templates select="@*[not(name()='pos1')]"/>
    		<atomcat type="punct">
					<fs>
    				<feat attr="lex" val=","/>
      		</fs>
  			</atomcat>
			</Leafnode>
		</xsl:when>	
		<xsl:otherwise>
			<Leafnode>
				<xsl:copy-of select="@*"/>
				<xsl:apply-templates/>
			</Leafnode>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>
<!--Balancing comma of above-->
<xsl:template match="Leafnode[@pos1=','and parent::Treenode/preceding-sibling::Leafnode[parent::Treenode/@cat0='(S\NP)\(S\NP)' and @pos1=',' and following-sibling::Treenode[@cat0='(S\NP)\(S\NP)' and *[last()]/@pos1=',']]]">

	<xsl:variable name="pos" select="java:getPOS($obj)"/>
	<xsl:choose>
		<xsl:when test="not($pos=',')">
			<Leafnode cat=",_1" lexeme="," pos1="{$pos}_BAL" cat0=",">
				<xsl:apply-templates select="@*[not(name()='pos1')]"/>
    		<atomcat type="punct">
					<fs>
    				<feat attr="lex" val=","/>
      		</fs>
  			</atomcat>
			</Leafnode>
		</xsl:when>	
		<xsl:otherwise>
			<Leafnode>
				<xsl:copy-of select="@*"/>
				<xsl:apply-templates/>
			</Leafnode>
		</xsl:otherwise>
	</xsl:choose>
	<xsl:variable name="dummy0" select="java:initPOS($obj)"/>
</xsl:template>

<!--Dash cat1-->
<xsl:template match="Leafnode[@cat0=':' and @lexeme='--' and following-sibling::Treenode/@cat0='NP\NP']">
	<Leafnode cat="{@cat}" lexeme="{@lexeme}" pos1="PUNCT_ELAB_DASH" cat0="{@cat0}">
		<xsl:apply-templates select="@*[not(name()='pos1')]"/>
    <atomcat type=",">
    	<fs id="1"/>
  	</atomcat>
	</Leafnode>
</xsl:template>

<!--Dash cat2-->
<xsl:template match="Leafnode[not(@cat0=':') and @lexeme='--' and (parent::Treenode/parent::Treenode/*[3][@lexeme='--' and @cat0=':'] or ../parent::Treenode/parent::Treenode/*[3][@lexeme='--' and @cat0=':'])]">

	<Leafnode cat="{@cat}" lexeme="{@lexeme}" pos1="PUNCT_ELAB_DASH_CAT" cat0="{@cat0}">
		<xsl:apply-templates select="@*[not(name()='pos1')]"/>
    <xsl:apply-templates/>
	</Leafnode>
</xsl:template>


<!--lrb cat1-->
<xsl:template match="Leafnode[not(@cat0='LRB') and (@lexeme='-lrb-' or @lexeme='-lcb-')and (parent::Treenode/parent::Treenode/*[3][(@lexeme='-rcb' or @lexeme='-rrb-') and @cat0='RRB'] or ../parent::Treenode/parent::Treenode/*[3][(@lexeme='-rrb-' or @lexeme='-rcb-') and @cat0='RRB'])]">

	<Leafnode cat="{@cat}" lexeme="{@lexeme}" pos1="PUNCT_LPAREN0" cat0="{@cat0}">
		<xsl:apply-templates select="@*[not(name()='pos1')]|node()"/>
	</Leafnode>
</xsl:template>

<!--lrb cat2: Parens around sentences-->
<xsl:template match="Leafnode[@cat='lrb' and (parent::Treenode/@cat0='S[dcl]' and following-sibling::Treenode[1]/@cat='sent_1') ]">

	<Leafnode pos1="PUNCT_LPAREN1">
    <xsl:apply-templates select="@*[not(name()='pos1')]|node()"/>
	</Leafnode>
</xsl:template>

<!--lrb cat3: Parens around np mods-->
<xsl:template match="Leafnode[@cat='lrb' and ( (parent::Treenode/@cat0='NP\NP' and following-sibling::Treenode/@cat0='NP\NP') ) ]">

	<Leafnode pos1="PUNCT_LPAREN2">
    <xsl:apply-templates select="@*[not(name()='pos1')]|node()"/>
	</Leafnode>
</xsl:template>

<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>




