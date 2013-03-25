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
  
  <!-- Transform which adjusts some cats in C&C parses-->

  <!-- change eg s_1/s_1/(s_1/s_1) to s_~1/s_1/(s_~1/s_1), to support mod-index -->

	<xsl:template match="Leafnode[
      count(.//complexcat[
        atomcat[1][@type='s'] and *[3][self::atomcat][@type='s']
      ]) = 2
    and //atomcat[1]/fs[@id='1'] and count(.//atomcat[@type='s']/fs[@id='2'])=0]"
  >
    <Leafnode candc="true">
      <xsl:apply-templates select="@*[not(name()='candc')]|node()"/>
    </Leafnode>
  </xsl:template>

  <xsl:template match="Leafnode[
      count(.//complexcat[
        atomcat[1][@type='s'] and *[3][self::atomcat][@type='s']
      ]) = 2
    ]//atomcat[1]/fs[@id='1']"
  >
    <fs inheritsFrom="1">
      <xsl:apply-templates/>
    </fs>
  </xsl:template>
  
  <!-- change eg s_1\np_2/(s_1\np_2)/(s_1\np_2/(s_1\np_2)) to s_~1\np_2/(s_1\np_2)/(s_~1\np_2/(s_1\np_2)), to support mod-index -->

	<xsl:template match="Leafnode[
      count(.//complexcat[
        atomcat[1][@type='s'] and *[3][self::atomcat][@type='np']
      ]) = 4
    and //atomcat/fs[@id='1'] and count(.//atomcat[@type='s' and position()=1]/fs[@id='3'])=0 ]"
  >

		<Leafnode candc="true">
      <xsl:apply-templates select="@*[not(name()='candc')]|node()"/>
    </Leafnode>
    
  </xsl:template>

	<xsl:template match="Leafnode[
      count(.//complexcat[
        atomcat[1][@type='s'] and *[3][self::atomcat][@type='np']
      ]) = 4
    ]//atomcat[parent::complexcat[parent::Leafnode or count(preceding-sibling::complexcat)=1]]/fs[@id='1']"
  >

		<fs inheritsFrom="1">
      <xsl:apply-templates/>
    </fs>

  </xsl:template>

	    
  <!-- default copy rule -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>