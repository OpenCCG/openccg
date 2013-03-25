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
  
  <!-- Transform which marks certain mistakes in ccgbank. -->

  
  <!-- mark sentence-final punct not at root level 
  <xsl:template match="
    Treenode[not(parent::Derivation)]/*[3][self::Leafnode and @pos='.' and
      not(following::Treenode[1][not(parent::Derivation)])
    ]
  ">-->

	<!--Change made by Raja Nov24,2007-->

	<xsl:template match="Treenode[not(parent::Derivation)]/*[3][self::Leafnode and @pos='.' and (following::*[1][parent::Derivation] or count(following::Leafnode)=0   )]">

    <Leafnode whoops="true">
      <xsl:apply-templates select="@*|node()"/>
    </Leafnode>
  </xsl:template>
  
	<!--Added by Raja Nov24,2007: Marks structures of the form(sent .) -->
	<xsl:template match="Treenode[not(parent::Derivation)]/*[3][self::Leafnode[@cat0='.' and ancestor::Treenode[@Header and *[2]/@pos='LRB'] and following::Leafnode[1][@pos='RRB']]]">

		<Leafnode whoops1="true">
      <xsl:apply-templates select="@*|node()"/>
    </Leafnode>

	</xsl:template>

  <!-- default copy rule -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>