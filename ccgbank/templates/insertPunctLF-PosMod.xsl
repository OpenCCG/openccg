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

	<xsl:template match="/">
  	<xsl:apply-templates/>
	</xsl:template>

	<!--Transform which takes in as input the lexicon.xml file and adds LFs of certain comma cats-->

	<!--Bkgrnd java class-->
	<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>

	<!--Add index rels to punctuation categories-->
	<!-- closed families -->
  <xsl:template match="family[@closed='true']">           
    <family>
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

				<!--Quotation marks-->
				<xsl:when test="@pos1='PUNCT_QUOTE' and not(starts-with(@name,'punct['))">
          <xsl:attribute name="indexRel">quote-rel</xsl:attribute>
        </xsl:when>

			</xsl:choose>
			<xsl:apply-templates/>
		</family>
	</xsl:template>		

	

	<!--LF of a non-case marking prep from Steve's corpus. To shifted from here shortly-->

	<!--To evaluate orig ccgbank-->
	<xsl:template match="complexcat[parent::entry[../@name='pp_1/np_2']]">

		<complexcat>	
			<xsl:apply-templates select="*[not(name()='lf')]"/>
			<lf>
        <satop nomvar="X1">
					<prop name="[*DEFAULT*]"/>
        	<diamond mode="Arg1">
	          <nomvar name="X2"/>
          </diamond>
      	</satop>
      </lf>
		</complexcat>		
	</xsl:template>


	<!--<xsl:template match="complexcat[parent::entry and ancestor::family[substring-after(@name,'_')='~2\(s_2\np_3\(s_4\np_5))/np_6' and starts-with(@name,'pp')]]">

		<complexcat>	
			<xsl:apply-templates select="*[not(name()='lf')]"/>
			<lf>
        <satop nomvar="X2">
					<diamond mode="Mod">
          	<nomvar name="M"/>
						<prop name="[*DEFAULT*]"/>
        		<diamond mode="Arg1">
	          	<nomvar name="X6"/>
          	</diamond>
					</diamond>
      	</satop>
      </lf>
		</complexcat>		
	</xsl:template>-->


	<!--LF of a renegade dollar sign cat. To shifted from here shortly-->
	<xsl:template match="complexcat[parent::entry and ancestor::family[@name='n_1/n[num]_2']]">

		<complexcat>	
			<xsl:apply-templates select="*[not(name()='lf')]"/>
			<lf>
        <satop nomvar="X1">
					<prop name="[*DEFAULT*]"/>
        	<diamond mode="Num">
	          <nomvar name="X2"/>
          </diamond>
      	</satop>
      </lf>
		</complexcat>		
	</xsl:template>

	<xsl:template match="complexcat[parent::entry and ancestor::family[@name='sent_1\*n_1']]">

		<complexcat>	
			<xsl:apply-templates select="*[not(name()='lf')]"/>
			<lf>
				<satop nomvar="X1">
     			<diamond mode="moodColon">
        		<prop name="[*DEFAULT*]"/>
        	</diamond>
     		</satop>	
			</lf>
		</complexcat>		
	</xsl:template>


	<xsl:template match="complexcat[parent::entry and ancestor::family[@name='np_1/(s[dcl]_2\np_3)' or @name='np_1/(s[dcl]_2/np_3)']]">
		<complexcat>	
			<xsl:apply-templates select="*[not(name()='lf')]"/>
			<lf>
        <satop nomvar="X1">
        	<diamond mode="GenRel">
	          <nomvar name="X2"/>
          </diamond>
      	</satop>
      </lf>
		</complexcat>		
	</xsl:template>
	
	<!--Conjunctions of certain cats which are not dealt with elsewhere-->

	 <xsl:template match="complexcat[parent::entry and ancestor::family[(@pos='PUNCT_CONJ' or @pos='CC' or @pos1='PUNCT_CONJ' or @pos1='CC') and starts-with(@name,'s_1\np_2\(s_1\np_2)\*(s_3\np_4\(s_3\np_4))')]]">
		<complexcat>	
			<xsl:apply-templates select="*[not(name()='lf')]"/>
			<lf>
				<satop nomvar="X1">
        	<diamond mode="GenRel">
						<nomvar name="M"/>
						<prop name="[*DEFAULT*]"/>
          	<diamond mode="Arg0">
        			<nomvar name="X3"/>
							<prop name="has-rel"/>
							<diamond mode="Of">
								<nomvar name="X1"/>
							</diamond>
						</diamond>
						<diamond mode="Arg1">
							<xsl:choose>
								<xsl:when test="descendant::punct">
									<nomvar name="X6"/>
								</xsl:when>
								<xsl:otherwise>
									<nomvar name="X5"/>
								</xsl:otherwise>
							</xsl:choose>
							<prop name="has-rel"/>
							<diamond mode="Of">
								<nomvar name="X1"/>
							</diamond>							
						</diamond>
        	</diamond>
        </satop>
      </lf>
		</complexcat>		
	</xsl:template>

	<xsl:template match="complexcat[parent::entry and ancestor::family[@name='n_1/n_1\*(n_2/n_2)/*(n_3/n_3)']]">

		<complexcat>	
			<xsl:apply-templates select="*[not(name()='lf')]"/>
			<lf>
				<satop nomvar="X1">
        	<diamond mode="GenRel">
						<nomvar name="M"/>
						<prop name="[*DEFAULT*]"/>
          	<diamond mode="Arg0">
        			<nomvar name="X2"/>
							<prop name="has-rel"/>
							<diamond mode="Of">
								<nomvar name="X1"/>
							</diamond>
						</diamond>
						<diamond mode="Arg1">
							<nomvar name="X3"/>
							<prop name="has-rel"/>
							<diamond mode="Of">
								<nomvar name="X1"/>
							</diamond>
						</diamond>							
        	</diamond>
        </satop>
      </lf>
		</complexcat>		
	</xsl:template>

<!--LFs of high freq punct cats-->
<xsl:template match="complexcat[parent::entry and ancestor::family[starts-with(@pos,'PUNCT_INIT_ADJ-MOD') or starts-with(@pos1,'PUNCT_INIT_ADJ-MOD')]]">
	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
      <satop nomvar="M">
	     	<diamond mode="emph-intro">
					<prop name="+"/>
        </diamond>
    	</satop>
    </lf>	
	</complexcat>		
</xsl:template>

<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_INIT_ADJ-ARG' or @pos1='PUNCT_INIT_ADJ-ARG']]">
	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
			<satop nomvar="X1">
      	<diamond mode="EmphIntro">
        	<nomvar name="X2"/>
      	</diamond>
      </satop>
		</lf>		
	</complexcat>	
</xsl:template>

<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_APPOS' or @pos1='PUNCT_APPOS']]">

	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
    	<satop nomvar="X1">
      	<diamond mode="ApposRel">
          <nomvar name="X3"/>
        </diamond>
      </satop>
    </lf>		
	</complexcat>				
</xsl:template>

<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_APPOS_PLACE' or @pos1='PUNCT_APPOS_PLACE']]">

	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
    	<satop nomvar="X1">
      	<diamond mode="ApposRel">
          <nomvar name="X3"/>
        </diamond>
      </satop>
    </lf>
	</complexcat>				
</xsl:template>

<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_APPOS_ADDR' or @pos1='PUNCT_APPOS_ADDR']]">

	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
			<satop nomvar="X1">
        <diamond mode="ApposRel">
        	<nomvar name="M"/>
      	</diamond>
      </satop>
    	<!--<satop nomvar="M">
        <diamond mode="PlaceRel">
        	<prop name="+"/>
      	</diamond>
      </satop>-->
    </lf>
	</complexcat>				
</xsl:template>

<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_APPOS_VRB' or @pos1='PUNCT_APPOS_VRB']]">

	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
    	<satop nomvar="X1">
      	<diamond mode="ApposRel">
         	<nomvar name="X4"/>
        </diamond>
      </satop>
    </lf>
	</complexcat>				
</xsl:template>

<xsl:template match="complexcat[parent::entry and ancestor::family[starts-with(@pos,'PUNCT_APPOS_MISC') or starts-with(@pos1,'PUNCT_APPOS_MISC')]]">
	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
			<satop nomvar="X1">
	     	<diamond mode="ApposRel">
					<nomvar name="M"/>
        </diamond>
			</satop>

      <!--<satop nomvar="M">
	     	<diamond mode="ApposRel">
					<prop name="+"/>
        </diamond>
    	</satop>-->
    </lf>	
	</complexcat>		
</xsl:template>

<!--Extraposed appositives-->
<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_EXTR-APPOS' or @pos1='PUNCT_EXTR-APPOS']]">

	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
    	<satop nomvar="X1">
      	<diamond mode="ApposRel">
          <nomvar name="X3"/>
        </diamond>
      </satop>
    </lf>
	</complexcat>	
</xsl:template>

<xsl:template match="complexcat[parent::entry and ancestor::family[@name='n_1\*(n_2/n_2)/*n_3']]">
	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
    	<satop nomvar="X1">
      	<prop name="[*DEFAULT*]"/>
     	 	<diamond mode="Arg0">
        	<nomvar name="X2"/>
					<prop name="has-rel"/>
					<diamond mode="Of">
						<nomvar name="X3"/>
					</diamond>
        </diamond>
				<diamond mode="Arg1">
					<nomvar name="X3"/>
				</diamond>
      </satop>
    </lf>
	</complexcat>		
</xsl:template>

<xsl:template match="complexcat[parent::entry and ancestor::family[@name='np_1\*np_2/*s[em]_3']]">

	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
    	<satop nomvar="X1">
      	<prop name="[*DEFAULT*]"/>
      	<diamond mode="First">
        	<nomvar name="X2"/>
        </diamond>
        <diamond mode="Next">
        	<nomvar name="X3"/>
        </diamond>
      </satop>
    </lf>
	</complexcat>		
</xsl:template>

<xsl:template match="complexcat[parent::entry and ancestor::family[(@pos='WRB' or @pos='WP' or @pos='WDT') and contains(@name,'punct[,]')]]">
		<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
    	<satop nomvar="X1">
      	<diamond mode="whApposRel">
        	<nomvar name="X2"/>
        </diamond>
      </satop>
    </lf>
	</complexcat>				
</xsl:template>

<!--say verbs-->
<xsl:template match="complexcat[parent::entry and ancestor::family[substring(@pos,1,9)='PUNCT_SAY' or substring(@pos1,1,9)='PUNCT_SAY']]">

	<xsl:variable name="satop1" select="*[1]/fs/@inheritsFrom"/>
	<xsl:variable name="satop2" select="*[1]/fs/@id"/>

	<xsl:variable name="arg0" select="slash[last()]/following::atomcat[1]/descendant::nomvar/@name"/>

	<xsl:variable name="args" select="descendant::atomcat"/>
	<xsl:variable name="arg1" select="$args[last()]/descendant::nomvar/@name"/>

	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
    	<satop nomvar="X{$satop1}">
				<xsl:if test="$satop2">
					 <xsl:attribute name="nomvar"><xsl:value-of select="concat('X',$satop2)"/></xsl:attribute>
				</xsl:if>
      	<diamond mode='ElabRel'>
        	<nomvar name="{$arg0}">
					</nomvar>
        </diamond>
      </satop>
    </lf>
	</complexcat>	
</xsl:template>
<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_COLON_SAY' or @pos1='PUNCT_COLON_SAY']]">

	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
    	<satop nomvar="X2">
      	<diamond mode="colonExp">
	       	<nomvar name="X1"/>
        </diamond>
      </satop>
    </lf>
	</complexcat>	
</xsl:template>

<!--Dash interpolations-->
<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_ELAB_DASH_CAT' or @pos1='PUNCT_ELAB_DASH_CAT']]">

	<xsl:variable name="satop" select="*[1]/fs/@inheritsFrom"/>
	<xsl:variable name="arg1" select="slash[last()]/following::atomcat[1]/descendant::nomvar/@name"/>

	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
    	<satop nomvar="X{$satop}">
      	<diamond mode='DashInterp'>
        	<nomvar name="{$arg1}"/>
        </diamond>
      </satop>
    </lf>
	</complexcat>	
</xsl:template>
<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_ELAB_DASH' or @pos1='PUNCT_ELAB_DASH']]">
	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
			<satop nomvar="X1">
	     	<diamond mode="DashInterp">
					<nomvar name="M"/>
        </diamond>
			</satop>
*    </lf>
	</complexcat>				
</xsl:template>
<!--<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='IN-DASH' or @pos1='IN-DASH']]">
	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
			<satop nomvar="X1">
				<diamond mode="DashInterp">
        	<nomvar name="M"/>
          <prop name="[*DEFAULT*]"/>
          <diamond mode="Arg1">
          	<nomvar name="X3"/>
          </diamond>
        </diamond>
			</satop>
    </lf>
	</complexcat>
</xsl:template>-->			



<!--Ellipsis Relation-->
<xsl:template match="diamond[@mode='Mod' and ancestor::family[@pos='PUNCT_DOTS1'or @pos='PUNCT_DOTS2' or @pos1='PUNCT_DOTS1'or @pos1='PUNCT_DOTS2']]">
	<diamond mode="EllipsisRel">
  	<xsl:apply-templates/>
  </diamond>
</xsl:template>

<!--Brackets-->
<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_LPAREN0' or @pos1='PUNCT_LPAREN0']]">

	<xsl:variable name="satop" select="*[1]/fs/@inheritsFrom"/>
	<xsl:variable name="arg1" select="slash[last()]/following::atomcat[1]/descendant::nomvar/@name"/>

	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
    	<satop nomvar="X{$satop}">
      	<diamond mode='ParenRel'>
          <nomvar name="{$arg1}"/>
        </diamond>
      </satop>
    </lf>
	</complexcat>	
</xsl:template>
<xsl:template match="complexcat[parent::entry and ancestor::family[@pos1='PUNCT_LPAREN1' or @pos1='LPAREN2']]">

	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
			<satop nomvar="X1">
        <diamond mode="ParenRel">
           <nomvar name="M"/>
           <prop name="[*DEFAULT*]"/>
      	</diamond>
      </satop>
    </lf>	
	</complexcat>	
</xsl:template>

<!--Vp Mod feature-->
<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_PRE-VP_ADJ' or @pos='PUNCT_POST-VP_ADJ' or @pos1='PUNCT_PRE-VP_ADJ' or @pos1='PUNCT_POST-VP_ADJ']]">
	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
			<satop nomvar="X1">
      	<diamond mode="modFeat">
          <nomvar name="M"/>
        </diamond>
	    </satop>
			<!--<satop nomvar="X1">
            <diamond mode="modFeat">
              <nomvar name="M"/>
              <prop name="[*DEFAULT*]"/>
            </diamond>
          </satop>-->
		</lf>
	</complexcat>	
</xsl:template>

<!--Emph final-->
<!--<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_FINAL_ADJ-MOD' or @pos1='PUNCT_FINAL_ADJ-MOD']]">-->
<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_EMPH_FINAL' or @pos1='PUNCT_EMPH_FINAL']]">
	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
			<satop nomvar="M">
      	<diamond mode="emph-final">
					<prop name="+"/>
        </diamond>
	    </satop>
		</lf>
	</complexcat>	
</xsl:template>
<!--<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_FINAL_ADJ-ARG' or @pos1='PUNCT_FINAL_ADJ-ARG']]">-->
<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_EMPH_FINAL_VRB' or @pos1='PUNCT_EMPH_FINAL_VRB']]">
	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
			<satop nomvar="X1">
      	<diamond mode="EmphFinal">
        	<nomvar name="X3"/>
      	</diamond>
      </satop>
    </lf>
	</complexcat>	
</xsl:template>

<!--Parentheticals-->
<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_PARENTHETICAL' or @pos1='PUNCT_PARENTHETICAL']]">
	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
			<satop nomvar="M">
      	<diamond mode="interrupt">
					<prop name="+"/>
        </diamond>
	    </satop>
		</lf>
	</complexcat>	
</xsl:template>
<xsl:template match="complexcat[parent::entry and ancestor::family[@pos='PUNCT_PARENTHETICAL_VRB' or @pos1='PUNCT_PARENTHETICAL_VRB']]">
	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
			<satop nomvar="X1">
      	<diamond mode="InterruptRel">
        	<nomvar name="X3"/>
      	</diamond>
      </satop>
    </lf>
	</complexcat>	
</xsl:template>


<!--Quotation marks-->

<xsl:template match="complexcat[parent::entry and ancestor::family[@pos1='PUNCT_QUOTE' and not(starts-with(@name,'punct'))]]">
	<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>
		<lf>
			<satop nomvar="X1">
				<prop name="quote-rel"/>
				<diamond mode="Arg">
					<nomvar name="X2"/>
				</diamond>
      </satop>
    </lf>
	</complexcat>	
</xsl:template>

<!--Removing cases where punct args of lexicalized cats had alloted args in the lf -->
<xsl:template match="diamond[starts-with(@mode,'Arg') and nomvar/@name=ancestor::entry/descendant::atomcat[@type='punct']/fs/feat/lf/nomvar/@name and  ancestor::lf]"/>

	<!--Default global copy rule-->	
	<xsl:template match="@*|node()">
	  <xsl:copy>
    	<xsl:apply-templates select="@*|node()"/>
  	</xsl:copy>
	</xsl:template>

</xsl:transform>
