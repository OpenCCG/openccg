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
  
  
  <!--Transform which takes in as input the morph.xml file and outputs a morph.xml file -->
  
  <!--Java Program in the grammar extractor package invoked-->
  <xsl:variable name="obj" select="java:opennlp.ccgbank.extract.MorphExtrHelper.new()"/>

	<xsl:template match="ccg-lexicon">
		<ccg-lexicon>
		<xsl:apply-templates select="@*|node()"/>

		<!--Strategy 2-->
		<family  closed="true" pos="Dummy" name="Dummy">
			<entry name="n">
				<atomcat type="n">
				<fs id="9">
					<feat attr="anim">
						<featvar name="ANIM"/>
					</feat>
					<feat attr="num">
						<featvar name="NUM"/>
					</feat>
				</fs>
				</atomcat>
			</entry>
			<entry name="np">
				<atomcat type="np">
				<fs id="9">
					<feat attr="anim">
						<featvar name="ANIM"/>
					</feat>
					<feat attr="num">
						<featvar name="NUM"/>
					</feat>
				</fs>
				</atomcat>
			</entry>	
			<member stem="*dummy*"/>
		</family>
		</ccg-lexicon>
	</xsl:template>

  <!--Strategy 1-->
	<!--<xsl:template match="fs[ancestor::family[@name='n_9' or @name='np_9']]">
		<fs>
			<xsl:apply-templates select="@*|node()"/>
			 <feat attr="anim">
				 <featvar name="ANIM"/>
       </feat>
		</fs>
  </xsl:template>-->

	


  <!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:transform>


