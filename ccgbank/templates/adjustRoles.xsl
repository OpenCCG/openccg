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
  
  
  <!-- Transform for guessing at missing arg roles and adding roles to cat names -->
  
  <!-- Helper class for adjusting roles -->
  <xsl:variable name="obj" select="java:opennlp.ccgbank.convert.RoleAdjuster.new()"/>

  
  <!-- Adjust arg roles and cats on relevant leaf nodes -->
  <xsl:template match="Leafnode[@argRoles]">
    <xsl:copy>
      <!-- copy attrs -->
      <xsl:apply-templates select="@*"/>
      <!-- get adjusted roles -->
      <xsl:variable name="roles2" select="java:getAdjustedRoles($obj,@cat,@argRoles)"/>
      <!-- replace roles if changed -->
      <xsl:if test="$roles2 != @argRoles">
        <xsl:attribute name="argRoles0"><xsl:value-of select="@argRoles"/></xsl:attribute>
        <xsl:attribute name="argRoles"><xsl:value-of select="$roles2"/></xsl:attribute>
      </xsl:if>
      <!-- add roles to cat name -->
      <!-- nb: 'if' test not really nec but seems to avoid a warning at runtime -->
      <xsl:variable name="cat2" select="java:getCatPlusRoles($obj,@cat,$roles2)"/>
      <xsl:if test="$cat2 != @cat"> 
        <xsl:attribute name="cat"><xsl:value-of select="$cat2"/></xsl:attribute>
      </xsl:if>
      <!-- copy rest -->
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>

  
  <!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>


