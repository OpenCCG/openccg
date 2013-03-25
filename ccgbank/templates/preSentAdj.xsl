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
  xmlns:exsl="http://exslt.org/common"
  extension-element-prefixes="exsl"		
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xalan xalan2 java">

  <xsl:output method="xml" indent="yes" xalan2:indent-amount="2"/>
  <xsl:strip-space elements="*"/>

<!--Transform which deals with pre-sentential adjuncts--> 

<!--Java class to manipulate strings of args and results of conj rule-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>


<!--Parent of the pre-sentential adjunct -->

<!--Cat0 : Comma anchors the pre-sentential adjunct containing a frame-verb-->
<!--In the process eliminate a vp to s/s unary rule-->

<xsl:template match="Treenode[@cat0='S/S' and following-sibling::Treenode/*[2]/@pos1='PUNCT_INIT_ADJ-ARG']">

	<Treenode>
		
		<xsl:apply-templates select="@*|node()"/>		

		<!--Comma cat-->
		<Leafnode cat="s_~1/s_1\*(s_2\np_3)" lexeme="," pos1="PUNCT_INIT_ADJ-ARG" pos=",">
			<complexcat>
				<xsl:apply-templates select="*[1]/*" mode="res"/>
				<xsl:variable name="void1" select="java:globalInit($obj)"/>				
				<slash dir="\" mode="*"/>
				<xsl:apply-templates select="*[2]/*[1]" mode="vrb-arg"/>
			</complexcat>
		</Leafnode>		
	</Treenode>	
	
</xsl:template>

<xsl:template match="atomcat[@type='s']" mode="vrb-arg">

	<atomcat type="s">
		<fs id="2"/>
	</atomcat>
	
</xsl:template>

<xsl:template match="atomcat[@type='np']" mode="vrb-arg">
	<atomcat type="np">
		<fs id="3"/>
	</atomcat>
</xsl:template>



<!--Cat1 : Comma anchors the pre-sentential adjunct containing a frame-adv-->

<xsl:template match="Treenode[@cat0='S/S' and following-sibling::Treenode/*[2]/@pos1='PUNCT_SENT_ADJ']">

	<Treenode cat="" head="1" dtr="2" cat0="NewlyAdded">
		<xsl:apply-templates select="@*|*[1]"/>
		<Treenode>
			<xsl:copy-of select="@*"/>	
			<xsl:apply-templates/>
		</Treenode>

		<!--Comma cat-->
		<Leafnode cat="s_~1/s_~1\*(s_1/s_1)" lexeme="," pos1="PUNCT_INIT_ADJ-MOD" pos=",">
			<complexcat>
				<xsl:apply-templates select="*[1]/*" mode="res"/>
				<xsl:variable name="void1" select="java:globalInit($obj)"/>				
				<slash dir="\" mode="*"/>
				<xsl:apply-templates select="*[1]" mode="res"/>
				<xsl:variable name="void2" select="java:globalInit($obj)"/>				
			</complexcat>
		</Leafnode>		
	</Treenode>	
	
</xsl:template>


<!--Cat2 (Another option) : Comma and a single adv as pre-sent adjunct-->

<xsl:template match="Leafnode[@cat0='S/S' and following-sibling::Treenode/*[2]/@pos1='PUNCT_SENT_ADJ']">

	<Treenode cat="" head="1" dtr="2" cat0="NewlyAdded">
		<xsl:apply-templates select="@*|*[1]"/>
		<Leafnode>
			<xsl:copy-of select="@*"/>
			<complexcat>
				<atomcat type="s">
        	<fs inheritsFrom="1">
          </fs>
        </atomcat>
        <slash dir="/" mode="&gt;"/>
        <atomcat type="s">
        	<fs id="1">
          </fs>
        </atomcat>
			</complexcat>
		</Leafnode>
		<!--Comma cat-->
		<Leafnode cat="s_~1/s_~1\*(s_1/s_1)" lexeme="," pos="," pos1="PUNCT_INIT_ADJ-MOD">
		
			<complexcat>
				<xsl:apply-templates select="*[1]/*" mode="res"/>
				<xsl:variable name="void1" select="java:globalInit($obj)"/>				
				<slash dir="\" mode="*"/>
				<xsl:apply-templates select="*[1]" mode="res"/>
				<xsl:variable name="void2" select="java:globalInit($obj)"/>				
			</complexcat>
		</Leafnode>		
	</Treenode>
</xsl:template>

<xsl:template match="atomcat[@type='s']" mode="res">

	<xsl:variable name="id" select="java:getPOS($obj)"/>		

	<atomcat type="s">
		<xsl:choose>
			<xsl:when test="string-length($id) &gt; 0">
				<fs id="1"/>
				<xsl:variable name="void" select="java:initPOS($obj)"/>
			</xsl:when>
			<xsl:otherwise>
				<fs inheritsFrom="1"/>
				<xsl:variable name="void" select="java:storePOS($obj,'1')"/>		
			</xsl:otherwise>
		</xsl:choose>
	</atomcat>
</xsl:template>
<xsl:template match="atomcat[@type='np']" mode="res">
	<atomcat type="np">
		<fs id="2"/>
	</atomcat>
</xsl:template>

<!--Delete the original position of the commas-->
<xsl:template match="Treenode[*[2][@pos1='PUNCT_INIT_ADJ-ARG'  or @pos1='PUNCT_SENT_ADJ']]">
	<xsl:apply-templates select="*[position()>2]"/>
</xsl:template>

<!-- Spl copy rule 2-->
<xsl:template match="@*|node()" mode="res">
   <xsl:copy>
     <xsl:apply-templates select="@*|node()" mode="res"/>
   </xsl:copy>
</xsl:template>

<!-- Spl copy rule -->
  <xsl:template match="@*|node()" mode="vrb-arg">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="vrb-arg"/>
    </xsl:copy>
  </xsl:template>

<!--Default global copy rule-->
<xsl:template match="@*|node()">
	<xsl:copy>
 		<xsl:apply-templates select="@*|node()"/>
	</xsl:copy>
</xsl:template>

</xsl:transform>











