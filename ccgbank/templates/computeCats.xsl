<!--Copyright (C) 2005-2010 Scott Martin, Rajakrishan Rajkumar and Michael White
 
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


  <!-- Transform which computes cat names and supertags. -->

  <!-- Set cat val to calculated cat name; likewise for (simpler) stag. -->
  <xsl:template match="Treenode|Leafnode">
    <xsl:copy>
      <xsl:attribute name="cat">
        <xsl:apply-templates mode="catname" select="*[1]"/>
      </xsl:attribute>
      <xsl:attribute name="stag">
        <xsl:apply-templates mode="supertag" select="*[1]"/>
      </xsl:attribute>
      <xsl:apply-templates select="@*[name(.)!='cat' and name(.)!='stag']"/>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>
  

  <!-- Calc cat name in catname mode. -->
  <xsl:template match="atomcat" mode="catname">
    <xsl:value-of select="@type"/>
    <xsl:apply-templates mode="catname"/>
  </xsl:template>
  
  <xsl:template match="complexcat/complexcat" mode="catname">
    <xsl:text>(</xsl:text>
    <xsl:apply-templates mode="catname"/>
    <xsl:text>)</xsl:text>
  </xsl:template>
  
  <xsl:template match="complexcat" mode="catname">
    <xsl:apply-templates mode="catname"/>
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
    <!-- skip ids with punctuation -->
    <xsl:if test="(@id or @inheritsFrom) and not(parent::*[@type='punct'])">
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

  <!-- skip ids with punctuation -->  
  <xsl:template match="atomcat[@type='punct']/fs/@id"/>


  <!-- Calc simpler supertag in supertag mode. -->
  <xsl:template match="atomcat" mode="supertag">
    <xsl:value-of select="@type"/>
    <xsl:apply-templates mode="supertag"/>
  </xsl:template>
  
  <xsl:template match="complexcat/complexcat" mode="supertag">
    <xsl:text>(</xsl:text>
    <xsl:apply-templates mode="supertag"/>
    <xsl:text>)</xsl:text>
  </xsl:template>
  
  <xsl:template match="complexcat" mode="supertag">
    <xsl:apply-templates mode="supertag"/>
  </xsl:template>
  
  <xsl:template match="slash" mode="supertag">
    <xsl:value-of select="@dir"/>
  </xsl:template>
  
  <xsl:template match="dollar" mode="supertag">
    <xsl:text>$</xsl:text>
  </xsl:template>

  <xsl:template match="fs" mode="supertag">
    <xsl:if test="feat[1][@attr='form' or @attr='lex']">
      <xsl:text>[</xsl:text>
      <xsl:value-of select="feat[@attr='form' or @attr='lex']/@val"/>    
      <xsl:text>]</xsl:text>
    </xsl:if>
  </xsl:template>


  <!-- default copy rule (catname) -->
  <xsl:template match="@*|node()" mode="catname">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="catname"/>
    </xsl:copy>
  </xsl:template>

  <!-- default copy rule (supertag) -->
  <xsl:template match="@*|node()" mode="supertag">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="supertag"/>
    </xsl:copy>
  </xsl:template>

  <!-- default copy rule -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>
