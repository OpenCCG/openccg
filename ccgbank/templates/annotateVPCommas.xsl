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

<!--Java helper class-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>
<!--Comma cat which deals with comma before/after verbal adjuncts-->
<!--[s\np= , s\np] & [s\np= s\np ,]-->

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="Leafnode[@pos1='PUNCT_PRE-VP_ADJ' or @pos1='PUNCT_POST-VP_ADJ']">
	
	<xsl:if test="@pos1='PUNCT_POST-VP_ADJ'">
		<xsl:variable name="feat" select="preceding-sibling::Treenode/*[1]/*[1]/fs/feat/@val"/>		
		<xsl:call-template name = "punct-cat">
			<xsl:with-param name = "slash-dir" >\</xsl:with-param>
			<xsl:with-param name = "feat"  select="$feat"/>
		</xsl:call-template>
	</xsl:if>	

	<xsl:if test="@pos1='PUNCT_PRE-VP_ADJ'">

		<xsl:variable name="feat" select="following-sibling::Treenode/*[1]/*[1]/fs/feat/@val"/>		
		<xsl:call-template name = "punct-cat">
			<xsl:with-param name = "slash-dir" >/</xsl:with-param>
			<xsl:with-param name = "feat"  select="$feat"/>
		</xsl:call-template>
	</xsl:if>	

</xsl:template>

<xsl:template name="punct-cat">
	<xsl:param name="slash-dir"/>
	<xsl:param name="feat"/>

	<Leafnode>
		<xsl:copy-of select="@*"/>	
		<complexcat>
     	<atomcat type="s">
       	<fs inheritsFrom="1">
         	<feat attr="form" val="{$feat}"/>
         </fs>
      </atomcat>
      <slash dir="\" mode="&lt;"/>
    	<atomcat type="np">
      	<fs id="2">
       	</fs>
      </atomcat>
      <slash dir="{$slash-dir}" mode="*"/>
      <complexcat>
      	<atomcat type="s">
	      	<fs id="1">
       	 		<feat attr="form" val="{$feat}"/>
       		</fs>
       	</atomcat>
       	<slash dir="\" mode="&lt;"/>
       	<atomcat type="np">
       		<fs id="2">
					</fs>
       	</atomcat>
     	</complexcat>
    </complexcat>
	</Leafnode>
</xsl:template>


<!--Emph final commas-->
<xsl:template match="Treenode[@cat0='(S\NP)\(S\NP)' and preceding::Leafnode[1]/@pos1='PUNCT_EMPH_FINAL']">

	<xsl:variable name="comma" select="preceding::Leafnode[1]"/>

	<Treenode>
		<xsl:apply-templates select="@*|*[1]"/>
		<Leafnode>
			<xsl:apply-templates select="$comma/@*"/>
			<complexcat>
				<xsl:apply-templates select="*[1]/*" mode="res"/>
				<xsl:variable name="void1" select="java:globalInit($obj)"/>				
				<slash dir="/" mode="*"/>
				<xsl:apply-templates select="*[1]" mode="res"/>
				<xsl:variable name="void2" select="java:globalInit($obj)"/>				
			</complexcat>
		</Leafnode>
		<Treenode>
			<xsl:apply-templates select="@*|node()"/>
		</Treenode>
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

<!--Emph final commas anchoring a verbal frame-->
<xsl:template match="Treenode[@cat0='(S\NP)\(S\NP)' and preceding::Leafnode[1]/@pos1='PUNCT_EMPH_FINAL_VRB']">

	<xsl:variable name="comma" select="preceding::Leafnode[1]"/>

	<Treenode>
		<xsl:apply-templates select="@*|*[1]"/>
		<Leafnode>
			<xsl:apply-templates select="$comma/@*"/>
			<complexcat>
				<xsl:apply-templates select="*[1]/*" mode="res"/>
				<xsl:variable name="void1" select="java:globalInit($obj)"/>				
				<slash dir="/" mode="*"/>
				<xsl:apply-templates select="*[2]/*[1]" mode="arg"/>
				<xsl:variable name="id" select="java:globalInit($obj)"/>		
			</complexcat>
		</Leafnode>
		<xsl:apply-templates select="*[2]"/>
	</Treenode>

</xsl:template>

<xsl:template match="atomcat[@type='s']" mode="arg">

	<xsl:variable name="id0" select="number(java:getglobalId($obj))-1"/>		

	<xsl:choose>
		<xsl:when test="$id0 &lt; 3">
			<xsl:variable name="void" select="java:setglobalId($obj,2)"/>		
		</xsl:when>	
		<xsl:otherwise>
			<xsl:variable name="void" select="java:setglobalId($obj,number($id0))"/>		
		</xsl:otherwise>
	</xsl:choose>

	<atomcat type="s">
		<xsl:variable name="id" select="java:getglobalId($obj)"/>		
		<fs id="{$id}"/>
	</atomcat>
	
</xsl:template>
<xsl:template match="atomcat[@type='np']" mode="arg">
	<atomcat type="np">
		<fs id="2"/>
	</atomcat>
</xsl:template>


<!--Delete orig CCGbank comma-->
<xsl:template match="Treenode[*[3][starts-with(@pos1,'PUNCT_EMPH_FINAL')]]">
	<xsl:apply-templates select="*[2]"/>
</xsl:template>


<!-- Spl copy rule 1-->
  <xsl:template match="@*|node()" mode="arg">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="arg"/>
    </xsl:copy>
  </xsl:template>

<!-- Spl copy rule 2-->
<xsl:template match="@*|node()" mode="res">
   <xsl:copy>
     <xsl:apply-templates select="@*|node()" mode="res"/>
   </xsl:copy>
</xsl:template>

<!--Default global copy rule-->
<xsl:template match="@*|node()">
	<xsl:copy>
 		<xsl:apply-templates select="@*|node()"/>
	</xsl:copy>
</xsl:template>

</xsl:transform>











