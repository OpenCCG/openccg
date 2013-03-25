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
  
  <!-- Transform which modifies punctuation categories in parses. -->

  
  <!-- Change sentence-final punctuation to lex cats. -->
  <xsl:template match="Derivation/Treenode/*[1]">
    <xsl:variable name="head" select="../*[2]"/>
    <atomcat type="sent"> 
      <fs id="1">
        <xsl:apply-templates select="$head/atomcat/fs/* | $head/complexcat/atomcat[1]/fs/*"/>
      </fs>
    </atomcat>
  </xsl:template>
  
  <xsl:template match="Derivation/Treenode/*[3][self::Leafnode and @pos='.']">
    <Leafnode lexeme="{@lexeme}" pos="." cat0=".">
			<xsl:apply-templates select="@*"/>
      <complexcat>
        <atomcat type="sent"> <fs id="1"/> </atomcat>
        <slash dir="\" mode="*"/>
        <xsl:apply-templates select="../*[2]/*[1]" mode="add-id-1"/>
      </complexcat>
    </Leafnode>
  </xsl:template>
  
  <!-- add id="1" to result cat -->
  <xsl:template match="*[self::Treenode or self::Leafnode]/atomcat | *[self::Treenode or self::Leafnode]/complexcat/atomcat[1]" mode="add-id-1">
    <xsl:copy>
      <xsl:apply-templates select="@*" mode="add-id-1"/>
      <fs id="1">
        <xsl:apply-templates select="fs/*" mode="add-id-1"/>
      </fs>
    </xsl:copy>
  </xsl:template>
  
  <!-- add-id-1 copy rule -->
  <xsl:template match="@*|node()" mode="add-id-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="add-id-1"/>
    </xsl:copy>
  </xsl:template>

  
  <!-- default copy rule -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>