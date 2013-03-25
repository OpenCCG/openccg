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
  
  <!-- Transform which adds semantics to hitherto unmarked cats-->

	<xsl:variable name="obj" select="java:opennlp.ccgbank.extract.DefaultLFHelper.new()"/>
  
  <!-- Check for unmatched-->

	<xsl:template match="family[@unmatched='true']">
		<family repaired="true">
			<xsl:apply-templates select="@*[not(name()='unmatched')]|node()" mode="lf-insert"/>
		</family>
	</xsl:template>

	<!--<xsl:template match="atomcat[parent::entry]" mode="lf-insert">

		<atomcat>
			<xsl:apply-templates mode="lf-insert"/>
			<lf>
				<satop nomvar="{descendant::nomvar/@name}">
					<prop name="[*DEFAULT*]"/>
				</satop>
			</lf>
		</atomcat>
	</xsl:template>-->

  <!--<xsl:template match="family[@unmatched='true']/entry/complexcat">-->

	<xsl:template match="complexcat[parent::entry]" mode="lf-insert">
		<xsl:variable name="void1" select="java:init($obj)"/>

		<xsl:for-each select="descendant::fs">
			<xsl:variable name="void1" select="java:storeCat($obj,../@type,@id,'id')"/>
			<xsl:variable name="void2" select="java:storeCat($obj,../@type,@inheritsFrom,'inherits')"/>
		</xsl:for-each>

		<!--Find args-->
		<xsl:variable name="args" select="descendant::atomcat[descendant::fs[@id] and java:isArg($obj,descendant::fs/@id) and preceding::slash[1][parent::complexcat[parent::entry]]]"/>

		<xsl:variable name="lf-type" select="java:getType($obj)"/>
	
		<complexcat>
			<xsl:apply-templates mode="lf-insert"/>
			<lf>
				<satop nomvar="{*[1]/descendant::nomvar/@name}">
					<xsl:choose>
						<xsl:when test="$lf-type='mod' or $lf-type='mod-mod'">
							<diamond mode="Mod">
								<xsl:if test="$lf-type='mod'">					
									<nomvar name="M"/>
								</xsl:if>
								<xsl:if test="$lf-type='mod-mod'">					
									<nomvar name="R"/>
								</xsl:if>
								<prop name="[*DEFAULT*]"/>
								<xsl:call-template name="printArgs">
									<xsl:with-param name = "args" select="$args"/>
								</xsl:call-template>		
							</diamond>
						</xsl:when>
						<xsl:otherwise>
							<prop name="[*DEFAULT*]"/>
							<xsl:call-template name="printArgs">
								<xsl:with-param name = "args" select="$args"/>
							</xsl:call-template>		
						</xsl:otherwise>
					</xsl:choose>
				</satop>
			</lf>
		</complexcat>
  </xsl:template>

	<xsl:template name="printArgs">
		<xsl:param name="args"/>

		<xsl:variable name="argCount" select="number(count($args))"/>

		<xsl:for-each select="$args">
			<diamond mode="Arg{java:getArgNo($obj,$argCount)}">
				<nomvar name="{./descendant::nomvar/@name}"/>
   		</diamond>
		</xsl:for-each>
	</xsl:template>		
  
	<!--LF-insertion copy rule-->
	<xsl:template match="@*|node()" mode="lf-insert">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="lf-insert"/>
    </xsl:copy>
  </xsl:template>

  <!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:transform>