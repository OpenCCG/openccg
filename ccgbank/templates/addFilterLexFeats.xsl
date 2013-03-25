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

  <!-- Transform which introduces punct placeholder feats in the lexical cats of unbalanced comma & dash appositive cats-->

	<!--Various kinds of comma appositions taken care of-->

	<!--Commas introducing np-appositions-->
	<xsl:template match="complexcat[parent::entry and ancestor::family[(starts-with(@pos1,'PUNCT_APPOS') or starts-with(@pos,'PUNCT_APPOS')) and not(contains(@name,'punct'))]]/*[1]/fs">             
	
		<fs>
			<xsl:apply-templates select="@*|node()"/>
			<feat attr="unbal" val="comma"/>
		</fs>

	</xsl:template>		

	<!--wh-prns introducing np-appositions-->
	<xsl:template match="complexcat[count(descendant::atomcat[@type='punct'])=1 and parent::entry and ancestor::family[@indexRel='whApposRel']]/*[1]/fs">             

		<fs>
			<xsl:apply-templates select="@*|node()"/>
			<feat attr="unbal" val="comma"/>
		</fs>

	</xsl:template>		

	<!--Dashes except that are treated like prepositions-->
	<xsl:template match="complexcat[parent::entry and ancestor::family[not(@name='punct[--]') and (contains(@pos1,'DASH') or contains(@pos,'DASH')) and not(contains(@name,'punct'))]]/*[1]/fs">             
		<fs>
			<xsl:apply-templates select="@*|node()"/>
			<feat attr="unbal" val="dash"/>
		</fs>

	</xsl:template>		

	<xsl:template match="complexcat[parent::entry and ancestor::family[count(child::member)=1 and child::member/@stem='--'  and @pos=':' and (@name='s_~1\np_2\(s_1\np_2)/np_3' or  @name='np_~1\np_1/np_2')]]/*[1]/fs">             
		<fs>
			<xsl:apply-templates select="@*|node()"/>
			<feat attr="unbal" val="dash"/>
		</fs>
	</xsl:template>

  <!-- default copy rule -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>