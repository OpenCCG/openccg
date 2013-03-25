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


<!-- Transform which corrects previously marked mistakes in ccgbank. -->
<xsl:transform
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xalan xalan2 java">

  <xsl:output method="xml" indent="yes" xalan2:indent-amount="2"/>
  <xsl:strip-space elements="*"/>
  
  <!-- correct s\n to s\np in cases such as cat="((np_1\np_1)/(s[dcl]_2\n_3))/n_3" -->
  <xsl:template match="complexcat/complexcat[atomcat[1][@type='s'] and slash[1][@dir='\']]
		       /atomcat[2][@type='n']">
    <atomcat type="np">
      <xsl:apply-templates select="@*[not(name(.)='type')]"/>
      <xsl:apply-templates select="node()"/>
    </atomcat>
  </xsl:template>

  <!-- Move sentence-final punctuation up to the top level. -->
  <xsl:template match="Derivation/Treenode[*[3] [not(self::Leafnode and @pos='.') and .//Leafnode[@whoops and @pos='.']]]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <!-- copy result -->
      <xsl:copy-of select="*[1]"/>
      <!-- put current children under new treenode -->
      <Treenode>
        <xsl:apply-templates select="@*[not(name(.)='Header')]"/>
        <!-- copy result again -->
        <xsl:copy-of select="*[1]"/>
        <!-- recurse through children -->
        <xsl:apply-templates select="*[2]"/>
        <xsl:apply-templates select="*[3]"/>
      </Treenode>
      <!-- copy sentence-final punct -->
      <xsl:apply-templates select="*[3]//Leafnode[@whoops and @pos='.']"/>
    </xsl:copy>
  </xsl:template>

  <!--Newly added-->
  <xsl:template match="Derivation/Treenode[count(*)=2 and *[2][descendant::Leafnode[@whoops]]]">
    <Treenode dtr="2">
      <xsl:apply-templates select="@*|node()"/>     
      <xsl:apply-templates select="descendant::Leafnode[@whoops]"/>
    </Treenode>
  </xsl:template>

  <!-- Filter sentence-final punct lower down. -->
  <xsl:template match="Treenode[*[2][self::Treenode] and Leafnode[@whoops]]">
    <xsl:apply-templates select="*[2]"/>
  </xsl:template>

  <!--Newly added-->
  <xsl:template match="Treenode[*[2][self::Leafnode] and *[3][self::Leafnode[@whoops]]]">
    <xsl:apply-templates select="*[2]"/>
  </xsl:template>

  <!-- Filter @whoops on sentence-final punct. -->
  <xsl:template match="Leafnode[@pos='.']/@whoops"/>
	

  <!--Correct (sent .) whoops1 on (sent .) structure-->
  
  <!--Add new resultant-->
  <xsl:template match="Treenode[descendant::Leafnode[@whoops1] and parent::Treenode[@Header]]">  
    <xsl:variable name = "brack" select="descendant::Leafnode[@pos='RRB' and preceding::Leafnode[1][@whoops1]]"/>  
    <Treenode cat="sent_1" head="1" dtr="2" cat0="NewlyAdded">
      <atomcat type="sent">
      	<fs id="1"/>
      </atomcat>
      <Treenode cat="sent_1" head="1" dtr="2" cat0="NewlyAdded">
	<atomcat type="sent">
	  <fs id="1"/>
    	</atomcat>
	<Treenode>
	  <xsl:apply-templates select="@*|node()"/>
	</Treenode>	
	<Leafnode cat="._1" lexeme="." lexeme0="." pos="." pos1="." cat0=".">
	  <atomcat type=".">
	    <fs id="1"/>
	  </atomcat>
	</Leafnode>
      </Treenode>
      <xsl:apply-templates select="$brack"/>
    </Treenode>
  </xsl:template>

  <!--Delete extant full-stop-->
  <xsl:template match="Treenode[*[3][@whoops1]]">  
    <xsl:apply-templates select="*[2]"/>
  </xsl:template>

  <!--Delete extant rbrac-->
  <xsl:template match="Treenode[*[3][self::Leafnode[@pos='RRB' and preceding::Leafnode[1][@whoops1]]]]">  
    <xsl:apply-templates select="*[2]"/>
  </xsl:template>  

  <!-- default copy rule -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>