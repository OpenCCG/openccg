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

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<!--Transform which allots index rel based on pos tags-->

<!--Add index rels to punctuation categories-->
  <xsl:template match="Leafnode">           
    <Leafnode>
      <xsl:apply-templates select="@*"/>
     <!-- add indexRel -->
      <xsl:choose>

				<!--Adv conj-->

				<!--Conjunction commas-->
				<xsl:when test="@pos='PUNCT_CONJ' or @pos1='PUNCT_CONJ'">
          <xsl:attribute name="indexRel">First</xsl:attribute>
        </xsl:when>
				
				<xsl:when test="(@pos='CC' or @pos='PUNCT_CONJ' or @pos1='CC' or @pos1='PUNCT_CONJ') and starts-with(@name,'s_1\np_2\(s_1\np_2)\*(s_3\np_4\(s_3\np_4))')">
          <xsl:attribute name="indexRel">GenRel</xsl:attribute>
        </xsl:when>

				<xsl:when test="@name='np_1/(s[dcl]_2\np_3)' or @name='np_1/(s[dcl]_2/np_3)'">
					<xsl:attribute name="indexRel">GenRel</xsl:attribute>
        </xsl:when>

				<xsl:when test="@name='n_1/n_1\*(n_2/n_2)/*(n_3/n_3)'">
					<xsl:attribute name="indexRel">GenRel</xsl:attribute>
        </xsl:when>

				<xsl:when test="@name='sent_1\*n_1'">
					<xsl:attribute name="indexRel">moodColon</xsl:attribute>
        </xsl:when>

				<!--Appos np commas-->

				<xsl:when test="@pos='PUNCT_APPOS' or @pos1='PUNCT_APPOS'">
          <xsl:attribute name="indexRel">ApposRel</xsl:attribute>
        </xsl:when>

				<xsl:when test="@pos='PUNCT_APPOS_PLACE' or @pos='PUNCT_APPOS_ADDR' or @pos1='PUNCT_APPOS_PLACE' or @pos1='PUNCT_APPOS_ADDR'">
          <xsl:attribute name="indexRel">ApposRel</xsl:attribute>
        </xsl:when>

				<xsl:when test="@pos='PUNCT_APPOS_MISC' or @pos1='PUNCT_APPOS_MISC'">
          <xsl:attribute name="indexRel">ApposRel</xsl:attribute>
        </xsl:when>

				<xsl:when test="@pos='PUNCT_APPOS_VRB' or @pos1='PUNCT_APPOS_VRB'">
          <xsl:attribute name="indexRel">ApposRel</xsl:attribute>
        </xsl:when>

				<!--Extraposed appositives-->
				<xsl:when test="@pos='PUNCT_EXTR-APPOS' or @pos1='PUNCT_EXTR-APPOS'">
          <xsl:attribute name="indexRel">ApposRel</xsl:attribute>
        </xsl:when>

				<xsl:when test="(@pos='WRB' or @pos='WP' or @pos='WDT') and contains(@name,'punct[,]')">
          <xsl:attribute name="indexRel">whApposRel</xsl:attribute>
        </xsl:when>

				<!--Commas which anchor pre-sentential adv adjuncts-->
				<xsl:when test="@pos='PUNCT_INIT_ADJ-MOD' or @pos1='PUNCT_INIT_ADJ-MOD'">
          <xsl:attribute name="indexRel">emph-intro</xsl:attribute>
        </xsl:when>
				<xsl:when test="@pos='PUNCT_INIT_ADJ-ARG' or @pos1='PUNCT_INIT_ADJ-ARG'">
          <xsl:attribute name="indexRel">EmphIntro</xsl:attribute>
        </xsl:when>
	
				<!--Commas which introduce say verbs-->
				<xsl:when test="substring(@pos,1,9)='PUNCT_SAY' or substring(@pos1,1,9)='PUNCT_SAY'">
					<xsl:attribute name="indexRel">ElabRel</xsl:attribute>
        </xsl:when>

				<!--post & pre vp adjunct commas-->
				<xsl:when test="@pos='PUNCT_PRE-VP_ADJ' or @pos='PUNCT_POST-VP_ADJ' or @pos1='PUNCT_PRE-VP_ADJ' or @pos1='PUNCT_POST-VP_ADJ'">
					<xsl:attribute name="indexRel">modFeat</xsl:attribute>
        </xsl:when>
			
				<!--Emph final commas-->
				<xsl:when test="@pos='PUNCT_EMPH_FINAL' or @pos1='PUNCT_EMPH_FINAL'">
					<xsl:attribute name="indexRel">emph-final</xsl:attribute>
        </xsl:when>
				<xsl:when test="@pos='PUNCT_EMPH_FINAL_VRB' or @pos1='PUNCT_EMPH_FINAL_VRB'">
					<xsl:attribute name="indexRel">EmphFinal</xsl:attribute>
        </xsl:when>

				<!--Parenthetical commas-->
				<xsl:when test="@pos='PUNCT_PARENTHETICAL' or @pos1='PUNCT_PARENTHETICAL'">
					<xsl:attribute name="indexRel">interrupt</xsl:attribute>
        </xsl:when>
				<xsl:when test="@pos='PUNCT_PARENTHETICAL_VRB' or @pos1='PUNCT_PARENTHETICAL_VRB'">
					<xsl:attribute name="indexRel">InterruptRel</xsl:attribute>
        </xsl:when>	

				<!--Brackets-->
				<xsl:when test="starts-with(@pos,'PUNCT_LPAREN') and not(starts-with(@name,'punct'))">
					<xsl:attribute name="indexRel">ParenRel</xsl:attribute>
				</xsl:when>

				<!--Colons after say verbs-->
				<xsl:when test="@pos='PUNCT_COLON_SAY' or @pos1='PUNCT_COLON_SAY'">
          <xsl:attribute name="indexRel">colonExp</xsl:attribute>
        </xsl:when>

				<!--Dash expansions-->
				<xsl:when test="@pos='IN-DASH' or @pos='PUNCT_ELAB_DASH_CAT' or @pos='PUNCT_ELAB_DASH' or @pos1='IN-DASH' or @pos1='PUNCT_ELAB_DASH_CAT' or @pos1='PUNCT_ELAB_DASH'">
          <xsl:attribute name="indexRel">DashInterp</xsl:attribute>
        </xsl:when>
				
				<!--Ellipsis relations ie dots in text-->
				<xsl:when test="@pos='PUNCT_DOTS1' or @pos='PUNCT_DOTS2' or @pos1='PUNCT_DOTS1' or @pos1='PUNCT_DOTS2'">
          <xsl:attribute name="indexRel">EllipsisRel</xsl:attribute>
        </xsl:when>
			</xsl:choose>
			<xsl:apply-templates/>
		</Leafnode>
	</xsl:template>		

<!--Default global copy rule-->
<xsl:template match="@*|node()">
   <xsl:copy>
     <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
</xsl:template>

</xsl:transform>











