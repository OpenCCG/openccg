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

<!--Transform which infers conj rule for punct followed by lexical conjn -->

<!--Java class to manipulate strings of args and results of conj rule-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.GenConjRule.new()"/>
<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>


<!--The template dollDecider decides whether this is a dollar conj cat-->

<!--Updating the conjunct phrase-->
<xsl:template match="Treenode[child::Treenode[2][contains(@cat0,'[conj]') and Leafnode/@pos1='PUNCT_LEX_CONJ' and contains(Treenode/@cat0,'[conj]')]]">
	
	<xsl:variable name="res" select="@cat"/>
	<xsl:variable name="resFrag" select="./*[1]"/>

	<!--Initialize id tally-->
	<xsl:variable name="dummy0" select="java:globalInit($obj)"/>
	
	<Treenode cat="{@cat}" head="1" dtr="{@dtr}" cat0="{@cat0}">
		<xsl:apply-templates select="@*"/>
		<xsl:choose>  
			<xsl:when test="name($resFrag)='atomcat'">
				<xsl:call-template name="dollDecider">
					<xsl:with-param name = "frag"  select="$resFrag"/>
 	    		<xsl:with-param name = "cat" select="$res"/>
					<xsl:with-param name = "argStt" >args</xsl:with-param>
				</xsl:call-template>		
			</xsl:when>
			<xsl:otherwise>
				<complexcat>
					<xsl:call-template name="dollDecider">
						<xsl:with-param name = "frag"  select="$resFrag"/>
 	    			<xsl:with-param name = "cat" select="$res"/>
						<xsl:with-param name = "argStt" >res</xsl:with-param>
					</xsl:call-template>		
				</complexcat>
			</xsl:otherwise>
		</xsl:choose > 
		<xsl:apply-templates select="*[position()>1]"/>
	</Treenode>

</xsl:template>

<!--Updating the distal intermediate treenode having conj feature-->
<!--<xsl:template match="Treenode[contains(@cat0,'[conj]') and Leafnode/@pos1='PUNCT_LEX_CONJ' and contains(Treenode/@cat0,'[conj]')]">-->

<xsl:template match="Treenode[Leafnode/@pos1='PUNCT_LEX_CONJ']">

<xsl:variable name="dummy0" select="java:globalInit($obj)"/>

	<!--Parent ie result of the treenode selected -->
	<xsl:variable name="par" select="parent::Treenode"/>
	<xsl:variable name="res" select="$par/@cat"/>
	<xsl:variable name="resFrag" select="$par/*[1]"/>

	<!--Select the argument required-->
	<xsl:variable name="arg1" select="preceding-sibling::*/@cat"/>
	<xsl:variable name="arg1Frag" select="preceding-sibling::*[1]/*[1]"/>

	<!--Init id tally-->
	<xsl:variable name="dummy1" select="java:globalInit($obj)"/>		

	<Treenode cat="{$res}\*{$arg1}" head="0" dtr="{@dtr}" cat0="{@cat0}">
		<xsl:apply-templates select="@*"/>		
		<complexcat>		
			<!--Result,arg1 inserted with id initialization before each insertion-->
			<xsl:call-template name="dollDecider">
				<xsl:with-param name = "frag"  select="$resFrag"/>
 	    	<xsl:with-param name = "cat" select="$res"/>
				<xsl:with-param name = "argStt" >res</xsl:with-param>
			</xsl:call-template>		

			<slash dir="\" mode="*" />
			<xsl:call-template name = "localInit" />
			<xsl:call-template name="dollDecider">
				<xsl:with-param name = "frag"  select="$arg1Frag"/>
 	    	<xsl:with-param name = "cat" select="$arg1"/>
				<xsl:with-param name = "argStt" >args</xsl:with-param>
			</xsl:call-template>					
		</complexcat>	
		<xsl:apply-templates select="*[position()>1]"/>
	</Treenode>

</xsl:template>

<!--Updating the proximal intermediate treenode having conj feature and next to comma-->

<!--<xsl:template match="Treenode[contains(@cat0,'[conj]')and Leafnode/@pos1='CC' and preceding-sibling::Leafnode/@pos1='PUNCT_LEX_CONJ']">-->

<xsl:template match="Treenode[Leafnode/@pos1='CC' and preceding-sibling::Leafnode/@pos1='PUNCT_LEX_CONJ']">

	<xsl:variable name="dummy0" select="java:globalInit($obj)"/>

	<!--Grandparent of intermediate tree ie final result of the conjPhrase -->
	<xsl:variable name="gpar" select="../parent::Treenode"/>

	<!--Choose the result category ie the cat of the parent-->
	<xsl:variable name="res" select="$gpar/@cat"/>
	<xsl:variable name="resFrag" select="$gpar/*[1]"/>

	<!--Select the argument required-->
	<xsl:variable name="arg1" select="$gpar/*[2]/@cat"/>
	<xsl:variable name="arg1Frag" select="$gpar/*[2]/*[1]"/>

	<!--Retrieving punct position-->
	<xsl:variable name="argPunct" select="preceding-sibling::Leafnode"/>
	<xsl:variable name="argPunctFrag" select="$argPunct/*[1]"/>

	<!--Init id tally-->
	<xsl:variable name="dummy1" select="java:globalInit($obj)"/>		

	<Treenode cat="{$res}\*{$arg1}\*{$argPunct}" head="0" dtr="{@dtr}" cat0="{@cat0}">
		<xsl:apply-templates select="@*"/>		
		<complexcat>	
	
			<xsl:call-template name="dollDecider">
				<xsl:with-param name = "frag"  select="$resFrag"/>
 	    	<xsl:with-param name = "cat" select="$res"/>
				<xsl:with-param name = "argStt" >res</xsl:with-param>
			</xsl:call-template>		

			<slash dir="\" mode="*" />
			<xsl:call-template name = "localInit" />
			<xsl:call-template name="dollDecider">
				<xsl:with-param name = "frag"  select="$arg1Frag"/>
 	    	<xsl:with-param name = "cat" select="$arg1"/>
				<xsl:with-param name = "argStt" >args</xsl:with-param>
			</xsl:call-template>						

			<slash dir="\" mode="*" />
			<xsl:call-template name = "localInit" />
			<xsl:call-template name="dollDecider">
				<xsl:with-param name = "frag"  select="$argPunctFrag"/>
 	    	<xsl:with-param name = "cat" select="$argPunct"/>
				<xsl:with-param name = "argStt" >args</xsl:with-param>
			</xsl:call-template>					

		</complexcat>

		<xsl:choose>
			<xsl:when test="following::Leafnode[1]/@pos1='PUNCT_LEX_CONJ_BAL'">
			<!--Init id tally-->
			<xsl:variable name="dummy1" select="java:globalInit($obj)"/>		

			<Treenode cat="X/*punct[,]" head="0" dtr="2">
      	<complexcat>
					<xsl:call-template name="dollDecider">
						<xsl:with-param name = "frag"  select="$resFrag"/>
 	    			<xsl:with-param name = "cat" select="$res"/>
						<xsl:with-param name = "argStt" >res</xsl:with-param>
					</xsl:call-template>
					<slash dir="\" mode="*" />
				<xsl:call-template name = "localInit" />
				<xsl:call-template name="dollDecider">
					<xsl:with-param name = "frag"  select="$arg1Frag"/>
 	    		<xsl:with-param name = "cat" select="$arg1"/>
					<xsl:with-param name = "argStt" >args</xsl:with-param>
				</xsl:call-template>						
					<slash dir="\" mode="*"/>
        	<atomcat type="punct">
          	<fs id="9">
            	<feat attr="lex" val=","/>
          	</fs>
        	</atomcat>
					<slash dir="/" mode="*"/>
        	<atomcat type="punct">
          	<fs id="10">
            	<feat attr="lex" val=","/>
          	</fs>
        	</atomcat>
      	</complexcat>
				<xsl:apply-templates select="*[position()>1]"/>
			</Treenode>	
			<Leafnode cat="punct[,]_1" lexeme="," pos="," pos1="PUNCT_LEX_CONJ_BAL" cat0=",">
    		<atomcat type="punct">
      		<fs id="1">
        		<feat attr="lex" val=","/>
        	</fs>
      	</atomcat>
    	</Leafnode>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="*[position()>1]"/>
			</xsl:otherwise>
		</xsl:choose>
	</Treenode>

</xsl:template> 

<xsl:template match="Treenode[*[3]/@pos1='PUNCT_LEX_CONJ_BAL']">
	<xsl:apply-templates select="*[2]"/>
</xsl:template> 

<!--Leafnode where the lexical conjn ie "or" / "and" / "but" resides-->
<!--<xsl:template match="Leafnode[contains(../@cat0,'[conj]') and @cat0='conj' and ../preceding-sibling::*/@pos1='PUNCT_LEX_CONJ']">-->

<xsl:template match="Leafnode[contains(@cat0,'conj') and preceding::Leafnode[1]/@pos1='PUNCT_LEX_CONJ']">

	<!--Great Grandparent of the context leafnode selected -->
	<xsl:variable name="ggpar" select="../parent::Treenode/.."/>

	<!--Choose the result category ie the cat of the parent-->
	<xsl:variable name="res" select="$ggpar/@cat"/>
	<xsl:variable name="resFrag" select="$ggpar/*[1]"/>
	
	<!--Retrieving arg1 position from the grandparent-->
	<xsl:variable name="arg1" select="$ggpar/*[2]/@cat"/>
	<xsl:variable name="arg1Frag" select="$ggpar/*[2]/*[1]"/>

	<!--Retrieving punct position-->
	<xsl:variable name="argPunct" select="preceding::Leafnode[1]"/>
	<xsl:variable name="argPunctFrag" select="$argPunct/*[1]"/>

	<!--Retrieving arg2 position ie the following sibling of selected leafnode-->
	<xsl:variable name="arg2" select="following-sibling::*[1]/@cat"/>
	<xsl:variable name="arg2Frag" select="following-sibling::*[1]/*[1]"/>
	<xsl:variable name="commaTest" select="following-sibling::*[1]"/>

	<!--Initialization of id before start of new conj rule-->
	<xsl:variable name="dummy0" select="java:globalInit($obj)"/>

	<!--Conj rule inserted-->	
	
	<Leafnode>
		<xsl:copy-of select="@*"/>	

		<!--Inserting the new category for things marked "conj"-->
		<complexcat>

			<xsl:call-template name="dollDecider">
				<xsl:with-param name = "frag"  select="$resFrag"/>
 	    	<xsl:with-param name = "cat" select="$res"/>
				<xsl:with-param name = "argStt" >res</xsl:with-param>
			</xsl:call-template>		

			<slash dir="\" mode="*" />
			<xsl:call-template name = "localInit" />
			<xsl:call-template name="dollDecider">
				<xsl:with-param name = "frag"  select="$arg1Frag"/>
 	    	<xsl:with-param name = "cat" select="$arg1"/>
				<xsl:with-param name = "argStt" >args</xsl:with-param>
			</xsl:call-template>						

			<slash dir="\" mode="*" />
			<xsl:call-template name = "localInit" />
			<xsl:call-template name="dollDecider">
				<xsl:with-param name = "frag"  select="$argPunctFrag"/>
 	    	<xsl:with-param name = "cat" select="$argPunct"/>
				<xsl:with-param name = "argStt" >args</xsl:with-param>
			</xsl:call-template>					
				
			<xsl:if test="parent::Treenode/following::Leafnode[1]/@pos1='PUNCT_LEX_CONJ_BAL'">
				<slash dir="/" mode="*" />
				<xsl:call-template name = "localInit" />
				<xsl:call-template name="dollDecider">
					<xsl:with-param name = "frag"  select="$argPunctFrag"/>
 	    		<xsl:with-param name = "cat" select="$argPunct"/>
					<xsl:with-param name = "argStt" >args</xsl:with-param>
				</xsl:call-template>					
			</xsl:if>

			<slash dir="/" mode="*" />
			<xsl:call-template name = "localInit" />
			<xsl:call-template name="dollDecider">
				<xsl:with-param name = "frag"  select="$arg2Frag"/>
 	    	<xsl:with-param name = "cat" select="$arg2"/>
				<xsl:with-param name = "argStt" >args</xsl:with-param>
			</xsl:call-template>					

		</complexcat>        	 
	</Leafnode>

</xsl:template> 

<!--Function which initializes the idTally-->
<xsl:template name = "localInit" >
	<xsl:variable name="dummy" select="java:localInit($obj)"/>
</xsl:template> 

<!--Local fn which decides whether any given arg/res is a dollar cat-->
<xsl:template name="dollDecider">
	<xsl:param name="frag"/>
	<xsl:param name="cat"/>
	<xsl:param name="argStt"/>
	
	<xsl:choose>
		<!--Dollar result-->
		<xsl:when test="contains($cat,'s') and $argStt='res' and not($cat='(s\np)\(s\np)' or $cat='s/s')">
				<xsl:apply-templates select="$frag/descendant-or-self::atomcat[1][@type='s']" mode="res"/>
	     	<slash/>
       	<dollar name="1"/>
		</xsl:when>
		<!--Dollar arg-->
		<xsl:when test="contains($cat,'s') and $argStt='args' and not($cat='(s\np)\(s\np)' or $cat='s/s')">
			<complexcat>
				<xsl:apply-templates select="$frag/descendant-or-self::atomcat[1][@type='s']" mode="args"/>
	  		<slash/>
    		<dollar name="1"/>
			</complexcat>
		</xsl:when>
		<!--All otherwise cases-->
		<xsl:otherwise>
			<xsl:if test="$argStt='res'">
				<xsl:apply-templates select = "$frag" mode="res"/>
			</xsl:if>
			<xsl:if test="$argStt='args'">
				<xsl:apply-templates select = "$frag" mode="args"/>
			</xsl:if>
		</xsl:otherwise>
	</xsl:choose>	

</xsl:template> 


<!--Eliminating the complexcat element in the result,if any-->
<xsl:template match="complexcat[position()=1]" mode="res">
	<xsl:apply-templates select="*[1]" mode="res"/>
	<xsl:apply-templates select="*[position()>1]" mode="args"/>
</xsl:template> 

<!--Adding the result category with a conj atomcat type-->
<xsl:template match="atomcat" mode="res">
	
	<atomcat type="{@type}">
		<xsl:variable name="newId" select="java:normId($obj,./fs/@id,./fs/@inheritsFrom,@type)"/>
 		<fs id="{$newId}">
			<xsl:apply-templates select="./fs/*" mode="res"/>
 		</fs>
 	</atomcat>
	
</xsl:template> 

<!--Eliminating the conj feature in the result-->
<xsl:template match="feat[@val='conj']" mode="res"/>

<!--Adding arg1 and arg2-->
<xsl:template match="atomcat" mode="args">

	<atomcat type="{@type}">
		<xsl:variable name="newId" select="java:normId($obj,./fs/@id,./fs/@inheritsFrom,@type)"/>
 		<fs id="{$newId}">
  		<xsl:apply-templates select="./fs/*" mode="args"/>  
 		</fs>
 	</atomcat>

</xsl:template> 

<!--res copy rule-->
<xsl:template match="@*|node()" mode="res">
	<xsl:copy>
	  <xsl:apply-templates select="@*|node()" mode="res"/>
  </xsl:copy>
</xsl:template>

<!--args copy rule-->
<xsl:template match="@*|node()" mode="args">
  <xsl:copy>
  	<xsl:apply-templates select="@*|node()" mode="args"/>
	</xsl:copy>
</xsl:template>

<!--Default global copy rule-->
<xsl:template match="@*|node()">
	<xsl:copy>
 		<xsl:apply-templates select="@*|node()"/>
	</xsl:copy>
</xsl:template>

</xsl:transform>











