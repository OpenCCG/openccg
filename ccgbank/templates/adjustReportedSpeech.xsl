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

<xsl:output method="xml" indent="yes" xalan2:indent-amount="2" omit-xml-declaration = "yes"/>

<xsl:strip-space elements="*"/>

<!--Java helper class-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>
<!--Transform which deletes original balancing commas of sentence medial parentheticals-->

<xsl:template match="/">
    <xsl:apply-templates/>
</xsl:template>

<!--Delete spurious comma-->
<xsl:template match="Treenode[count(*)=3 and child::Leafnode[@pos1=',' and (following::Leafnode[1][@pos1='PUNCT_SAY'] or preceding::Leafnode[1][@pos1='PUNCT_SAY_BAL']) ]]">

	<xsl:apply-templates select="*[position() &gt; 1 and not(@pos1=',')]"/>
</xsl:template>


<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>