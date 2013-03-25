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
  
  
  <!--Transform which takes in as input the morph.xml file and outputs a morph.xml file -->
  
  <!--Java Program in the grammar extractor package invoked-->
  <xsl:variable name="obj" select="java:opennlp.ccgbank.extract.MorphExtrHelper.new()"/>
  
	<xsl:template match="morph">
   <morph>
  
      <xsl:apply-templates select="@*|node()"/>
			
		  <!--<entry pos="Dummy" word="*dummy*"/>-->
			
      <macro name="@anim-nom">
         <fs id="9">
           <feat attr="anim" val="+"/>
         </fs>
      </macro>
      <macro name="@non-anim-nom">
         <fs id="9">
           <feat attr="anim" val="-"/>
         </fs>
      </macro>
			<macro name="@anim-wh">
         <fs id="1">
           <feat attr="anim" val="+"/>
         </fs>
      </macro>
      <macro name="@non-anim-wh">
         <fs id="1">
           <feat attr="anim" val="-"/>
         </fs>
      </macro>
   </morph>
  </xsl:template>
  
	<xsl:template match="entry">
    <entry>
			<xsl:apply-templates select="@*[not(name()='family')]"/>
    </entry>
  </xsl:template>

  <xsl:template match="entry[(@family='n_9' or @family='np_9')]">
    <entry>

			<xsl:variable name="macro-name" select="java:macroNamer($obj,@macros,@class,@pos,@word)"/>

			<xsl:if test="string-length($macro-name) &gt; 0">
				<xsl:attribute name = "macros" >
	  			<xsl:value-of select = "$macro-name" />
   			</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="@*[not(name()='family') and not(name()='macros')]"/>			

    </entry>
  </xsl:template>
    
	<xsl:template match="entry[@pos='WP' and (@word='who')]">
    <entry macros="@anim-wh">
			<xsl:apply-templates select="@*[not(name()='family')]"/>			
    </entry>
  </xsl:template>
	<xsl:template match="entry[@pos='WDT']">
    <entry macros="@non-anim-wh">
			<xsl:apply-templates select="@*[not(name()='family')]"/>			
    </entry>
  </xsl:template>

	<!--Add semantic class (BBN) to morph entry if present-->
	<xsl:template name="sem-class">
		<xsl:if test="@class">  
			<xsl:copy-of select="@class"/>
		</xsl:if>
  </xsl:template>


  <!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:transform>


