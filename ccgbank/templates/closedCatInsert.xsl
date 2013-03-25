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

  <!-- Transform which loads word stems into closed lexical families -->

  <xsl:output method="xml" indent="yes" xalan2:indent-amount="2"/>
  <xsl:strip-space elements="*"/>
  
  <!-- For checking uniqueness of family members -->
  <xsl:variable name="obj" select="java:opennlp.ccgbank.extract.InsertLFHelper.new()"/>
  
  
  
  <!-- root -->
  <xsl:template match="ccg-lexicon">
    <ccg-lexicon>
      <!-- add licensing features for complementizer, infinitival-to, and expletive 'there' and 'it' -->
      <!-- nb: leaving out expletive 'there' and 'it' for now -->
      <licensing-features>
        <feat attr="form" val="emb" location="target-only"/>
        <feat attr="form" val="to" location="target-only"/>
	<!--
        <feat attr="form" val="thr" location="target-only"/>
        <feat attr="form" val="expl" location="target-only"/>
	-->
      </licensing-features>    
      <!-- process rest in descending order of frequency -->
      <xsl:apply-templates select="family">
        <xsl:sort select="@freq" order="descending" data-type="number"/>
      </xsl:apply-templates>
    </ccg-lexicon>
  </xsl:template>  


  
  <!-- closed families -->
  <xsl:template match="family[@closed='true']">           
    <family>
      <xsl:apply-templates select="@*"/>
     <!-- add indexRel -->
      <xsl:choose>
				<!-- case marking preps -->
        <xsl:when test="starts-with(@name,'pp[') and contains(@name,'~2') and not(@pos='CC' or @pos='PUNCT_CONJ')">
          <xsl:attribute name="indexRel">*NoSem*</xsl:attribute>
        </xsl:when>
        <!-- bare punct -->
        <xsl:when test="starts-with(@name,'punct')">
          <xsl:attribute name="indexRel">*NoSem*</xsl:attribute>
        </xsl:when>
				<!--Particles-->
				<xsl:when test="starts-with(@name,'prt')">
          <xsl:attribute name="indexRel">*NoSem*</xsl:attribute>
        </xsl:when>

        <!-- to-inf -->
        <xsl:when test="@name='s[to]_~1\np_2/(s[b]_1\np_2)' and @pos='TO'">
          <xsl:attribute name="indexRel">*NoSem*</xsl:attribute>
        </xsl:when>
        <!-- complementizer -->
        <xsl:when test="@name='s[em]_~1/s[dcl]_1'">
          <xsl:attribute name="indexRel">*NoSem*</xsl:attribute>
        </xsl:when>
        <!-- expletives -->
	<!-- nb: leaving out expletive 'there' and 'it' for now -->
	<!--
        <xsl:when test="@name='np[thr]_1' or @name='np[expl]_1'">
          <xsl:attribute name="indexRel">*NoSem*</xsl:attribute>
        </xsl:when>
	-->
        <!-- possessive -->
        <xsl:when test="@name='np_1/n_1\np_2' and @pos='POS'">
          <xsl:attribute name="indexRel">GenOwn</xsl:attribute>
        </xsl:when>
        <!--  relative pronouns -->
        <!-- nb: pos should be WDT or WP; but IN frequent w/ obj extr (!) -->
        <!-- nb: could distinguish 'that', 'which'; could distinguish 'who(m)' -->
        <xsl:when test="@name='np_~1\np_1/(s[dcl]_2\np_1)' or @name='np_~1\np_1/(s[dcl]_2/np_1)' 
          or @name='np_~1\np_1/s[dcl]_2\(np_3\np_4/np_1)'"
        >
          <!--<xsl:attribute name="indexRel">GenRel</xsl:attribute>-->
					<xsl:attribute name="indexRel">GenRel</xsl:attribute>
        </xsl:when>
        <!--  tbd: poss rel pro, 'whose' (WP$); free relatives? -->
      </xsl:choose>
      <xsl:apply-templates/>
      <!-- add members -->
      <!-- nb: should perhaps do indexing -->
      <xsl:variable name="fam" select="@name"/>
			<xsl:variable name="pos" select="@pos"/>
      <xsl:if test="java:resetStemRelPairs($obj)"/>
      <xsl:for-each select="../entry[@family=$fam and @pos=$pos]">
        <xsl:sort select="@stem"/>
        <xsl:sort select="@word"/>
        <xsl:variable name="stem">
          <xsl:choose>
            <xsl:when test="@stem"><xsl:value-of select="@stem"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="@word"/></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:if test="not(java:containsStemRelPair($obj,$stem,@rel))">
          <member stem="{$stem}">
            <!-- add rel as pred when present -->
            <xsl:if test="@rel">
              <xsl:attribute name="pred"><xsl:value-of select="@rel"/></xsl:attribute>
            </xsl:if>
          </member>
        </xsl:if>
      </xsl:for-each>
    </family>
  </xsl:template>
  
  
  <!-- filter old internal punctuation categories (should eventually be replaced!) -->
  <!-- nb: use start of @name to catch old cats -->
  <!-- 
  <xsl:template match="family[@pos=',' or @pos=';' or @pos=':' or @pos='LRB' or @pos='RRB']"/>
  -->
  <xsl:template match="family[starts-with(@name,'.') or starts-with(@name,',') or starts-with(@name,';') or 
                              starts-with(@name,':') or starts-with(@name,'lrb') or starts-with(@name,'rrb')]"/>

  <!-- filter bogus conj cats -->
  <!-- eg: $_1\*($_1)/*($_1) and np_1\np_1\*($_1)/*(s[dcl]_2$_1) -->
  <xsl:template match="family[contains(@name,'($_1)')]"/>
  
  
  <!-- sentence-final punctuation --> 
  <!-- nb: could give int for syntactic questions with periods -->
  <xsl:template match="family[@pos='.' and ((starts-with(@name,'s_1') or starts-with(@name,'sent_1')))]"> 

    <family pos="{@pos}" name="{@name}" closed="true" indexRel="mood">
      <xsl:apply-templates/>
      <xsl:variable name="fam" select="@name"/>
      <xsl:variable name="argcat" select="entry/complexcat/*[3]"/>
      <xsl:for-each select="../entry[@family=$fam]">
        <xsl:variable name="pred">
          <xsl:choose>
            <xsl:when test="@word='?'">int</xsl:when>
            <xsl:when test="@word='!'">excl</xsl:when>
            <!-- mood is imperative for vp args -->
            <xsl:when test="$argcat/atomcat[1]/@type='s' and $argcat/atomcat[2]/@type='np'">imp</xsl:when>
            <xsl:otherwise>dcl</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <member stem="{@word}" pred="{$pred}"/>
      </xsl:for-each>
    </family>
  </xsl:template>

	<xsl:template match="family[@pos='``']">
		<family>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
			<member stem="``" pred="quote-rel"/>
		</family>
	</xsl:template>

  <!-- filter @freq -->
  <xsl:template match="@freq"/>
  
  <!-- filter atomcat/@dep -->
  <xsl:template match="atomcat/@dep"/>
  
  
  <!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>
