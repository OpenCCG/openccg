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
  xmlns:exsl="http://exslt.org/common"
  extension-element-prefixes="exsl"		
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xalan xalan2 java">

  <xsl:output method="xml" indent="yes" xalan2:indent-amount="2"/>
  <xsl:strip-space elements="*"/>

<!--Java class to manipulate strings of args and results of conj rule-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>
<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>


<!--Remove PRT tag from the corpus-->
<xsl:template match="PRT"/>

<xsl:template match="Treenode[@ptb-tag=*[2]/@ptb-tag]">
	<Treenode>
		<xsl:apply-templates select="@*[not(name()='ptb-tag')]|node()"/>
	</Treenode>
</xsl:template> 

<!--Remove dependency evaluation label :B and :U-->
<xsl:template match="atomcat[@dep]">
	<atomcat>
		<xsl:apply-templates select="@*[not(name()='dep')]|node()"/>
	</atomcat>
</xsl:template> 


<xsl:template match="Leafnode[@cat='punct[,]_1' or @cat='punct[,]']">

	<Leafnode cat="{@cat}" lexeme="{@lexeme}" pos1="PUNCT_COMMA" cat0="{@cat0}">
		<xsl:apply-templates select="@*[not(name()='pos1')]|node()"/>
	</Leafnode>
</xsl:template>

<xsl:template match="Leafnode[@cat='punct[--]_1' or @cat='punct[--]']">

	<Leafnode cat="{@cat}" lexeme="{@lexeme}" pos1="PUNCT_DASH" cat0="{@cat0}">
		<xsl:apply-templates select="@*[not(name()='pos1')]|node()"/>
	</Leafnode>
</xsl:template>

<!--Revert wrongly detected balancing commas back to orig form-->
<xsl:template match="Leafnode[@pos1='_BAL']">

	<Leafnode cat=",_1" lexeme="," lexeme0="," pos1="," cat0=",">
		<xsl:apply-templates select="@*"/>
  	<atomcat type="*[1]/@type">
    	<fs id="1"/>
    </atomcat>
  </Leafnode>
</xsl:template>

<!--Change pos of dashes marked with pos=IN
<xsl:template match="Leafnode[@lexeme='' and @pos='IN']">
	<Leafnode>
		<xsl:copy-of select="@*"/>	
			<xsl:attribute name="pos">IN-DASH</xsl:attribute>
		<xsl:apply-templates/>
	</Leafnode>
</xsl:template>-->

<!--Changing commas connecting place names to regular appositives-->
<xsl:template match="Leafnode[@pos1='PUNCT_APPOS_PLACE']">
	<Leafnode>
		<xsl:copy-of select="@*"/>	
			<xsl:attribute name="pos1">PUNCT_APPOS</xsl:attribute>
		<xsl:apply-templates/>
	</Leafnode>
</xsl:template>


<!--Corrections that need to be made to the original corpus ideally. Steve? -->

<!--wh-rels-->
<xsl:template match="Leafnode[@cat='np_~1\np_1/s[dcl]_2\(np_3\np_4/np_5)' and starts-with(@pos,'W')]">
	
	<Leafnode>
		<xsl:copy-of select="@*"/>

		<xsl:attribute name="cat">np_~1\np_1/s[dcl]_2\(np_3\np_4/np_1)</xsl:attribute>
		<xsl:apply-templates select="*[1]" mode="wh-corr"/>
	</Leafnode>
</xsl:template>
<xsl:template match="atomcat[@type='np']/fs[@id='5']" mode="wh-corr">
	<fs id="1"/>
</xsl:template>

<!--Changing id=1 of n and np cats to id=9-->
<xsl:template match="Leafnode[@cat='np_1' or @cat='n_1']">
	<Leafnode cat="{substring-before(@cat,'_')}_9">
		<xsl:copy-of select="@*[not(name()='cat')]"/>
		<xsl:apply-templates mode="np-id-corr"/>
	</Leafnode>
</xsl:template>
<xsl:template match="fs" mode="np-id-corr">
	<fs id="9">
		<xsl:apply-templates mode="np-id-corr"/>
	</fs>	
</xsl:template>

<!--Correct treenode of non-case marking preps-->

<xsl:template match="Treenode[*[1][self::atomcat[@type='pp']] and starts-with(@cat,'pp[') and *[2]/@cat='pp_1/np_2']">

	<Treenode cat="pp">
	<xsl:apply-templates select="@*[not(name()='cat')]"/>
	<xsl:apply-templates select="*[position()>0]"/>
	</Treenode>
</xsl:template>

<!--Make sure that result treenodes of non-case marking preps are not lexicalized-->
<xsl:template match="Treenode[starts-with(@cat,'pp[') and *[2]/@cat='pp_1/np_2']/*[1]/fs/feat[@attr='lex']">
</xsl:template>

<!--Replace pp[] by pp-->
<xsl:template match="Leafnode[contains(@cat,'pp[]')]">

	<Leafnode>
		<xsl:copy-of select="@*"/>

		 <xsl:variable name="cat" select="java:cleanPP($obj,@cat)"/>

		 <xsl:attribute name="cat">
			<xsl:value-of select = "$cat" />
		 </xsl:attribute>
		<xsl:apply-templates/>
  </Leafnode>
</xsl:template>

<!--Changing pp[]~2/np_2 to pp[lex]_~2/np_2-->
<xsl:template match="Leafnode[@cat='pp[]_~2/np_2']">

	<Leafnode>
		<xsl:copy-of select="@*"/>

		 <xsl:attribute name="cat">pp[<xsl:value-of select="@lexeme"/>]_~2/np_2</xsl:attribute>
		 <complexcat>
        <atomcat type="pp">
          <fs inheritsFrom="2">
            <feat attr="form">
              <featvar name="FORM"/>
            </feat>
						<feat attr="lex" val="{@lexeme}"/>
          </fs>
        </atomcat>
        <slash dir="/" mode="&gt;"/>
        <atomcat type="np">
          <fs id="2"/>
        </atomcat>
      </complexcat>
  </Leafnode>
</xsl:template>

<!--Remove an erroneous ARG assignment to to a case-marking preposition-->
<xsl:template match="Leafnode[@cat='pp[in]_1/np_2:Arg0' or @cat='pp[in]_~2/np_2:Arg0']">
	<Leafnode cat="{substring-before(@cat,':')}">
		<xsl:apply-templates select="@*[not(name()='cat' or starts-with(name(),'argRoles'))]|node()"/>
	</Leafnode>
</xsl:template>

<!--Remove erroneous unary rules-->
<xsl:template match="Treenode[count(*)=2 and @cat=*[2]/@cat]">
	<xsl:apply-templates select="*[position()>1]"/>
</xsl:template>

<xsl:template match="Treenode[count(*)=2 and @cat='s/s' and *[2]/@cat='s[dcl]/s[dcl]']">
	<xsl:apply-templates select="*[position()>1]"/>
</xsl:template>

<xsl:template match="Treenode[count(*)=2 and @cat='sent_1' and not(java:purgeCat1($obj,@cat0)=java:purgeCat1($obj,@cat))]">

	<Treenode cat="{java:purgeCat1($obj,@cat0)}">
		<xsl:apply-templates select="@*[not(name()='cat')]"/>	
		<atomcat type="{java:purgeCat1($obj,@cat0)}">
      <fs id="1"/>
    </atomcat>
		<xsl:apply-templates select="*[position()>1]"/>
	</Treenode>
</xsl:template>

<!--Spl copy rule for np-id-corr-->
<xsl:template match="@*|node()" mode="np-id-corr">
	<xsl:copy>
 		<xsl:apply-templates select="@*|node()" mode="np-id-corr"/>
	</xsl:copy>
</xsl:template>

<!--Spl copy rule for wh-corr-->
<xsl:template match="@*|node()" mode="wh-corr">
	<xsl:copy>
 		<xsl:apply-templates select="@*|node()" mode="wh-corr"/>
	</xsl:copy>
</xsl:template>

<!--Default global copy rule-->
<xsl:template match="@*|node()">
	<xsl:copy>
 		<xsl:apply-templates select="@*|node()"/>
	</xsl:copy>
</xsl:template>

</xsl:transform>











