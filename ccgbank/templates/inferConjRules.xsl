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

<!--Transform which infers conj rule of categories from the bare parse-->

<!--This transform copies the result,arg1 & arg2 of the conj rule from the source xml. So unlike constituents will be taken care of-->

<!--Java class to manipulate strings of args and results of conj rule-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.GenConjRule.new()"/>
<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<!--Note: The extant ccgbank does not distinguish Appositives and NP list coordination involving commas. Ccgbank Manual Pg49-->

<!--Updating treenodes which are conjunct phrases-->
<xsl:template match="Treenode[not(@Header) and Treenode[Leafnode[1][@pos1='PUNCT_CONJ' or @cat='conj']]]">

	<xsl:variable name="res" select="@cat"/>
<xsl:variable name="resFrag" select="./*[1]"/>
	<xsl:variable name="conjUnar" select="java:getConjRes($obj,@cat)"/>

	<!--Select arg1-->
	<xsl:variable name="arg1" select="*[2]/@cat"/>

	<!--Select arg2-->
	<xsl:variable name="arg2" select="Treenode[Leafnode[1][@pos1='PUNCT_CONJ' or @cat='conj']]/*[3]/@cat"/>
	
	<xsl:variable name="dummy" select="java:dsInit($obj)"/>

	<xsl:call-template name="dollDecider">
		<xsl:with-param name = "cat1" select="$res"/>
 		<xsl:with-param name = "cat2" select="$arg1"/>
		<xsl:with-param name = "cat3" select="$arg2"/>
	</xsl:call-template>		

	<!--Initialize id tally-->
	<xsl:variable name="dummy0" select="java:globalInit($obj)"/>
	
	<!--Retrieve dollar status of conj-->
	<xsl:variable name="dollarStt" select="java:getDollarStatus($obj)"/>
	
	<Treenode cat="{@cat}" head="1" dtr="{@dtr}" cat0="{@cat0}" arg1="{$arg1}" arg2="{$arg2}">
		<xsl:apply-templates select="@*"/>
		<xsl:choose>
			<xsl:when test="$dollarStt='Dollar'">
				<xsl:call-template name = "localInit" />	
					
				<!--Result-->
				<complexcat>
					<xsl:apply-templates select="$resFrag/descendant-or-self::atomcat[1][@type='s']" mode="res"/>
	    		<slash/>
     			<dollar name="1"/>
				</complexcat>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select = "$resFrag"/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:apply-templates select="*[position()>1]"/>
	</Treenode>
</xsl:template>

<!--Updating intermediate treenodes having conj feature-->
<xsl:template match="Treenode[Leafnode[1][@pos1='PUNCT_CONJ'or @cat='conj']]">

	<xsl:variable name="dummy0" select="java:globalInit($obj)"/>

	<!--Parent ie result of the treenode selected -->
	<xsl:variable name="par" select="parent::Treenode"/>
	<xsl:variable name="res" select="$par/@cat"/>
	<xsl:variable name="resFrag" select="$par/*[1]"/>

	<!--Select the arg1-->
	<xsl:variable name="arg1" select="preceding-sibling::*/@cat"/>
	<xsl:variable name="arg1Frag" select="preceding-sibling::*[1]/*[1]"/>

	<!--Retrieve dollar status of conj-->
	<xsl:variable name="dollarStt" select="java:getDollarStatus($obj)"/>

	<xsl:variable name="dummy1" select="java:globalInit($obj)"/>		

	<Treenode cat="{$res}\*{$arg1}" head="0" dtr="{@dtr}" cat0="{@cat0}">
		<xsl:apply-templates select="@*"/>		
		<complexcat>		
			<xsl:apply-templates select = "$resFrag" mode="res"/>
			<slash dir="\" mode="*" />
			<xsl:apply-templates select = "$arg1Frag" mode="args"/>
		</complexcat>
		<xsl:apply-templates select="*[position()>1]"/>        	 
	</Treenode>

</xsl:template> 

<!--Select only those Leafnodes which have conj rules and commas-->
 <xsl:template match="Leafnode[1][@cat='conj' or @pos1='PUNCT_CONJ']">

	<!--Grandparent of the context leafnode selected -->
	<xsl:variable name="gpar" select="parent::Treenode/.."/>

	<!--Choose the result category ie the cat of the parent-->
	<xsl:variable name="res" select="$gpar/@cat"/>
	<xsl:variable name="resFrag" select="$gpar/*[1]"/>
	
	<!--Retrieving arg1 position from the grandparent-->
	<xsl:variable name="arg1" select="$gpar/*[2]/@cat"/>
	<xsl:variable name="arg1Frag" select="$gpar/*[2]/*[1]"/>

	<!--Retrieving arg2 position ie the following sibling of selected leafnode-->
	<xsl:variable name="arg2" select="following-sibling::*[1]/@cat"/>
	<xsl:variable name="arg2Frag" select="following-sibling::*[1]/*[1]"/>
	<xsl:variable name="commaTest" select="following-sibling::*[1]"/>

	<!--Initialization of id before start of new conj rule-->
	<xsl:variable name="dummy0" select="java:globalInit($obj)"/>
	<xsl:variable name="dummy1" select="java:dsInit($obj)"/>		

	<xsl:call-template name="dollDecider">
		<xsl:with-param name = "cat1" select="$res"/>
 		<xsl:with-param name = "cat2" select="$arg1"/>
		<xsl:with-param name = "cat3" select="$arg2"/>
	</xsl:call-template>		
	

	<!--Retrieve dollar status of conj-->
	<xsl:variable name="dollarStt" select="java:getDollarStatus($obj)"/>
	<xsl:variable name="dummy" select="java:dsInit($obj)"/>	

	<!--Conj rule inserted-->	

	<Leafnode>
		<xsl:apply-templates select="@*"/>
			
			<!--Debugging technique to mark unlike conjs-->
			<!--<xsl:if test="$dollarStt='No_Dollar'"> 
				<xsl:attribute name="dollar">No_Dollar</xsl:attribute>
			</xsl:if>-->

			<!--Inserting the new category for things marked "conj"-->
			<complexcat>

				<xsl:choose>
	
					<xsl:when test="$dollarStt='Dollar'">


						<xsl:call-template name = "localInit" />	
					
						<!--Result, Arg 1 & Arg2-->
						<xsl:apply-templates select="$resFrag/descendant-or-self::atomcat[1][@type='s']" mode="res"/>
	     			<slash/>
      			<dollar name="1"/>
						<slash dir="\" mode="*" />
						<xsl:call-template name = "localInit" />
						<complexcat>
							<xsl:apply-templates select="$arg1Frag/descendant-or-self::atomcat[1][@type='s']" mode="args"/>
	  					<slash/>
    					<dollar name="1"/>
						</complexcat>
						<slash dir="/" mode="*" />
						<xsl:call-template name = "localInit" />
						<complexcat>
							<xsl:apply-templates select="$arg2Frag/descendant-or-self::atomcat[1][@type='s']" mode="args"/>
	  					<slash/>
    					<dollar name="1"/>
						</complexcat>
					</xsl:when>
					<xsl:otherwise>
						
						<xsl:call-template name = "localInit" />
						<xsl:apply-templates select = "$resFrag" mode="res"/>
						<slash dir="\" mode="*" />
						<xsl:call-template name = "localInit" />
						<xsl:apply-templates select = "$arg1Frag" mode="args"/>
						<slash dir="/" mode="*" />
						<xsl:call-template name = "localInit" />
						<xsl:apply-templates select = "$arg2Frag" mode="args"/>			
					</xsl:otherwise>
				</xsl:choose>
			</complexcat>
			<xsl:apply-templates select="*[self::PRT]"/>        	 
	</Leafnode>
</xsl:template> 

<!--Function which initializes the idTally-->
<xsl:template name = "localInit" >
	<xsl:variable name="dummy" select="java:localInit($obj)"/>
</xsl:template> 

<xsl:template name="dollDecider">
	<xsl:param name="cat1"/>
	<xsl:param name="cat2"/>
	<xsl:param name="cat3"/>

	<xsl:if test="contains($cat1,'s') and not($cat1='(s\np)\(s\np)')">
		<xsl:variable name="dummy" select="java:storeDollarStatus($obj,'res')"/>
	</xsl:if>
	<xsl:if test="contains($cat2,'s') and not($cat2='(s\np)\(s\np)')">
		<xsl:variable name="dummy" select="java:storeDollarStatus($obj,'arg1')"/>	
	</xsl:if>
	<xsl:if test="contains($cat3,'s') and not($cat3='(s\np)\(s\np)')">
		<xsl:variable name="dummy" select="java:storeDollarStatus($obj,'arg2')"/>	
	</xsl:if>
	<xsl:variable name="dummy" select="java:dsCalc($obj)"/>	
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

<!--Template which adds fs according as it is id or inheritsFrom-->
<xsl:template name="addFs">

	<xsl:variable name="newId" select="java:normId($obj,./fs/@id,./fs/@inheritsFrom,@type)"/>
	
	<xsl:if test="./fs/@id">
 		<fs id="{$newId}">
			<xsl:apply-templates select="./fs/*" mode="res"/>
 		</fs>
	</xsl:if>
	<xsl:if test="./fs/@inheritsFrom">
 		<fs inheritsFrom="{$newId}">
			<xsl:apply-templates select="./fs/*" mode="res"/>
 		</fs>
	</xsl:if>

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











