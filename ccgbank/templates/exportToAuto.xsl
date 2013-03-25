<!--Copyright (C) 2010 Michael White
 
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
  xmlns:jstring="xalan://java.lang.String"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xalan jstring java">

  <xsl:output method="text"/>

  <xsl:strip-space elements="*"/>
  <xsl:variable name="newline">
    <xsl:text>&#10;</xsl:text>
  </xsl:variable>

  <xsl:template match="/">
    <xsl:for-each select="/*/Treenode[@Header]">
      <!-- header line -->
      <xsl:value-of select="@Header"/>
      <xsl:text> PARSER=GOLD NUMPARSE=1</xsl:text>
      <xsl:value-of select="$newline"/>
      <!-- curry cats in renaming -->
      <xsl:variable name="curried">
	<xsl:apply-templates select="." mode="curry"/>
      </xsl:variable>
      <!-- transform deriv -->
      <xsl:apply-templates select="xalan:nodeset($curried)/*"/>
      <xsl:value-of select="$newline"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="Treenode">
    <xsl:if test="not(@head) or not(@dtr)">
      <xsl:message>Missing head index or length in 
	<xsl:value-of select="ancestor::Treenode[@Header]/@Header"/>
      </xsl:message>
    </xsl:if>
    <xsl:text>(&lt;T </xsl:text>
    <xsl:value-of select="jstring:replaceAll(string(@cat),'_~?\d+','')"/> <xsl:text> </xsl:text>
    <xsl:value-of select="@head"/> <xsl:text> </xsl:text>
    <xsl:value-of select="@dtr"/>
    <xsl:text>&gt; </xsl:text>
    <xsl:apply-templates/>
    <xsl:text>) </xsl:text>
  </xsl:template>

  <xsl:template match="Leafnode">
    <xsl:text>(&lt;L </xsl:text>
    <xsl:value-of select="jstring:replaceAll(string(@cat),'_~?\d+','')"/> <xsl:text> </xsl:text>
    <xsl:value-of select="@pos"/> <xsl:text> </xsl:text>
    <xsl:value-of select="@pos1"/> <xsl:text> </xsl:text>
    <xsl:value-of select="@lexeme"/> <xsl:text> </xsl:text>
    <xsl:value-of select="@cat"/> 
    <xsl:text>&gt;) </xsl:text>
  </xsl:template>

  <!-- curry mode: set cat val to calculated cat name, curried -->
  <!-- this bit is adapted from computeCats transform -->
  <xsl:template match="Treenode|Leafnode" mode="curry">
    <xsl:copy>
      <xsl:attribute name="cat">
        <xsl:apply-templates mode="catname" select="*[1]"/>
      </xsl:attribute>
      <xsl:copy-of select="@*[name(.)!='cat']"/>
      <xsl:apply-templates select="node()" mode="curry"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="atomcat" mode="catname">
    <xsl:value-of select="jstring:toUpperCase(string(@type))"/>
    <xsl:apply-templates mode="catname"/>
  </xsl:template>
  
  <!-- add parens around complex args -->
  <xsl:template match="complexcat/complexcat" mode="catname">
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="." mode="catname-cc"/>
    <xsl:text>)</xsl:text>
  </xsl:template>
  
  <xsl:template match="complexcat" mode="catname">
    <xsl:apply-templates select="." mode="catname-cc"/>
  </xsl:template>
  
  <!-- add parens for curried args -->
  <xsl:template match="complexcat" mode="catname-cc">
    <!-- add one left paren per slash, except the first -->
    <xsl:for-each select="slash[position() &gt; 1]">
      <xsl:text>(</xsl:text>
    </xsl:for-each>
    <xsl:for-each select="*">
      <!-- add right paren before each slash, after the first (which is in the 2nd position) -->
      <xsl:if test="self::slash and position() &gt; 2">
	<xsl:text>)</xsl:text>
      </xsl:if>
      <xsl:apply-templates select="." mode="catname"/>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="slash" mode="catname">
    <xsl:value-of select="@dir"/>
    <xsl:if test="(@dir='/' and @mode!='&gt;') or (@dir='\' and @mode!='&lt;')">
      <xsl:value-of select="@mode"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="dollar" mode="catname">
    <xsl:text>$</xsl:text>
    <xsl:if test="@name">
      <xsl:text>_</xsl:text>
      <xsl:value-of select="@name"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="fs" mode="catname">
    <xsl:if test="feat[1][@attr='form' or @attr='lex']">
      <xsl:text>[</xsl:text>
      <xsl:value-of select="feat[@attr='form' or @attr='lex']/@val"/>    
      <xsl:text>]</xsl:text>
    </xsl:if>
    <xsl:if test="@id or @inheritsFrom">
      <xsl:text>_</xsl:text>
      <xsl:choose>
        <xsl:when test="@id">
          <xsl:value-of select="@id"/>
        </xsl:when>
        <xsl:when test="@inheritsFrom">
          <xsl:text>~</xsl:text><xsl:value-of select="@inheritsFrom"/>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
  
  <!-- default copy rule (catname) -->
  <xsl:template match="@*|node()" mode="catname">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="catname"/>
    </xsl:copy>
  </xsl:template>

  <!-- default recursion rule (curry) -->
  <xsl:template match="@*|node()" mode="curry">
    <xsl:apply-templates select="@*|node()" mode="curry"/>
  </xsl:template>

</xsl:transform>











