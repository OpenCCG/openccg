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

	<xsl:template match="/">
  	<xsl:apply-templates/>
	</xsl:template>

	<!-- relative pronouns -->
  <xsl:template match="family[@indexRel='GenRel']/entry/complexcat">
    <complexcat>
      <xsl:apply-templates select="*[not(name()='lf')]"/>
			<lf>
			<satop nomvar="X1">
				<diamond mode="GenRel">
					<nomvar name="M"/>
          <prop name="[*DEFAULT*]"/>
          <diamond mode="Arg1">
            <nomvar name="X2"/>
					</diamond>
				</diamond>
      </satop>
    </lf>
		</complexcat>
  </xsl:template>

	<!--WH relative pronouns indentified as appositions-->
	<xsl:template match="complexcat[parent::entry and ancestor::family[(@pos='WRB' or @pos='WP' or @pos='WDT') and contains(@name,'punct[,]')]]">
		<complexcat>	
		<xsl:apply-templates select="*[not(name()='lf')]"/>

		<lf>
			<satop nomvar="X1">
				<diamond mode="whApposRel">
					<nomvar name="M"/>
          <prop name="[*DEFAULT*]"/>
          <diamond mode="Arg1">
            <nomvar name="X2"/>
					</diamond>
				</diamond>
      </satop>
    </lf>
	</complexcat>				
	</xsl:template>

	<!--Default global copy rule-->	
	<xsl:template match="@*|node()">
	  <xsl:copy>
    	<xsl:apply-templates select="@*|node()"/>
  	</xsl:copy>
	</xsl:template>

</xsl:transform>
