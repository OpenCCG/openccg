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

<!-- transform illustrating search for a pattern of interest (here, a buggy cat in the ccgbank) -->
<xsl:transform
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xalan xalan2 java">

  <xsl:output method="xml" indent="yes" xalan2:indent-amount="2" omit-xml-declaration="yes"/>

  <xsl:strip-space elements="*"/>

  <!-- match leafnodes with eg cat="((np_1\np_1)/(s[dcl]_2\n_3))/n_3" where it should be s\np -->
  <xsl:template match="/">
    <yuck>
      <xsl:copy-of select=".//Leafnode[
			      .//complexcat/complexcat[
			         atomcat[1][@type='s'] and slash[1][@dir='\'] and atomcat[2][@type='n']]]"/>
    </yuck>
  </xsl:template>

  <!--Default global copy rule-->
  <!--
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  -->
</xsl:transform>











