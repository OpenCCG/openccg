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

  <xsl:output method="xml" indent="yes" xalan2:indent-amount="2" omit-xml-declaration="yes"/>
  <xsl:strip-space elements="*"/>
  
  <!-- Transform which filters extracted cats and lex items by frequency cutoff in FreqTally -->
  

  <!-- Java Program in the grammar extractor package invoked -->
  <xsl:variable name="obj" select="java:opennlp.ccgbank.extract.FreqTally.new()"/>

  
  <!-- Filter family -->
  <xsl:template match="family">
    <!-- check freq -->
    <xsl:if test="java:checkFreqStatus($obj,@name,@pos)">
      <!-- set freq, closed attrs -->
      <family 
        freq="{java:getFreq($obj,@name,@pos)}"
        closed="{not(java:isOpen($obj,@name,@pos))}"
      >
        <!-- copy rest -->
        <xsl:apply-templates select="@*|node()"/>
      </family>
    </xsl:if>
  </xsl:template>
  
  
  <!-- Filter lex entry -->
  <xsl:template match="ccg-lexicon/entry">
    <!-- check freq -->
    <xsl:if test="java:checkFreqStatus($obj,@word,@family,@pos)">
      <!-- check for open family -->
      <xsl:if test="not(java:isOpen($obj,@name,@pos))">
        <entry>
          <xsl:apply-templates select="@*|node()"/>
        </entry>
      </xsl:if>
    </xsl:if>
  </xsl:template>

	<!--Filter some bogus cats-->  
	<xsl:template match="family[starts-with(@name,'$_') or starts-with(@name,'np_~1\np_2:')]">
	</xsl:template>

	<!--Add a "non-em" feat to prevent sent_1\s full-stop combining with complementizer sents-->
	<xsl:template match="fs[ancestor::family[@name='sent_1\*s_1'] and parent::atomcat[@type='s']]">
		<fs>
			<xsl:apply-templates select="@*|node()"/>
			<feat attr="form" val="non-em"/>
		</fs>
	</xsl:template>

  <!-- Copy Rule -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>