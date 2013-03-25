<?xml version="1.0"?>
<!-- 
	$Id: navigation.xsl,v 1.3 2007/03/19 19:07:14 coffeeblack Exp $
	Author: Scott Martin (http://www.ling.osu.edu/~scott/)
 -->
<xsl:stylesheet version="1.0"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!--
		Default value; actual value set by HTMLDocumenter.
	-->
	<xsl:param name="sections">grammar|lexicon|morph|rules|types</xsl:param>
	

	<xsl:template name="navigation-generator">
		<xsl:param name="current"/>
	
		<ul id="navigation">
			<xsl:call-template name="section-handler">
				<xsl:with-param name="current" select="$current"/>
			</xsl:call-template>
		</ul>
	</xsl:template>

	<xsl:template name="section-handler">
		<xsl:param name="sects" select="$sections"/>
		<xsl:param name="current"/>
		
		<xsl:variable name="sect">
			<xsl:choose>
				<xsl:when test="contains($sects, '|')">
					<xsl:value-of select="substring-before($sects, '|')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$sects"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<li>
			<a>
				<xsl:choose>
					<xsl:when test="$sect=$current">
						<xsl:attribute name="class">current</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="href">
							<xsl:choose>
								<xsl:when test="$sect='grammar'">index</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$sect"/>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:text>.html</xsl:text>
						</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				
				<xsl:value-of select="$sect"/>
			</a>
		</li>
		
		<xsl:if test="contains($sects, '|')">
			<xsl:call-template name="section-handler">
				<xsl:with-param name="current" select="$current"/>
				<xsl:with-param name="sects" select="substring-after($sects, '|')"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>