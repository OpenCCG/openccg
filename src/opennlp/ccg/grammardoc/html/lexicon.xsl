<?xml version="1.0"?>
<!-- 
	$Id: lexicon.xsl,v 1.6 2006/12/13 19:25:22 coffeeblack Exp $
	Author: Scott Martin (http://www.ling.osu.edu/~scott/)
 -->
<xsl:stylesheet version="1.0"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:import href="base.xsl"/>
	<xsl:import href="categories.xsl"/>

	<xsl:template match="ccg-lexicon" mode="invoked">
		<dl class="shortcuts box">
			<dt>Lexical Families</dt>
			<dd>
				<ul>
					<xsl:for-each select="family">
						<xsl:sort select="@name"/>
						<li>
							<a href="#{@name}">
								<xsl:value-of select="@name"/>
							</a>
						</li>
					</xsl:for-each>
				</ul>
			</dd>
		</dl>
		
		<xsl:apply-templates select="family">
			<xsl:sort select="@name"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="family">
		<div class="family">
			<a name="{@name}">
				<h2>
					<xsl:value-of select="@name"/>
				</h2>
			</a>
			
			<xsl:call-template name="comment-handler"/>
			
			<dl class="attributes">
				<dt>Closed: </dt>
				<dd>
					<xsl:choose>
						<xsl:when test="@closed">
							<xsl:value-of select="@closed"/>
						</xsl:when>
						<xsl:otherwise>false</xsl:otherwise>
					</xsl:choose>
				</dd>
				<dt>Part of Speech: </dt>
				<dd>
					<xsl:value-of select="@pos"/>
				</dd>
			</dl>
			
			<xsl:if test="not(@closed='true')">
				<a href="morph.html#{@pos}">Lexical Items</a>
			</xsl:if>
			
			<xsl:apply-templates select="entry">
				<xsl:sort select="@name"/>
			</xsl:apply-templates>
			
			<xsl:if test="member">
				<h3>Members</h3>
				<ul class="short">
					<xsl:apply-templates select="member[
						not(@stem=preceding-sibling::member/@stem)]">
						<xsl:sort select="@stem"/>
					</xsl:apply-templates>
				</ul>
			</xsl:if>
			
			<div class="back">
				<a href="#">Back to top</a>
			</div>
		</div>
	</xsl:template>
	
	<xsl:template match="member">
		<xsl:variable name="parentPos-stem">
			<xsl:value-of select="parent::family/@pos"/>
			<xsl:text>-</xsl:text>
			<xsl:value-of select="@stem"/>
		</xsl:variable>
		<li>
			<a href="morph.html#{$parentPos-stem}" name="{$parentPos-stem}">
				<xsl:value-of select="@stem"/>
			</a>
		</li>
	</xsl:template>
	
	<xsl:template match="entry">
		<h3>
			<xsl:value-of select="@name"/>
		</h3>
		
		<dl class="box">
			<dt>Categories</dt>
			<dd>
				<ol class="categories">
					<xsl:for-each select="atomcat|complexcat">
						<li>
							<a href="javascript://"
								title="expand feature structures"
								onclick="toggleFeatures(this.parentNode);">
								<xsl:text>[+]</xsl:text>
							</a>
							<dl class="category">
								<xsl:if test="name()='atomcat'">
									<dt>
										<xsl:value-of select="@type"/>
									</dt>
								</xsl:if>
							
								<xsl:apply-templates/>
							</dl>
						</li>
					</xsl:for-each>
				</ol>	
			</dd>
		</dl>
	</xsl:template>

</xsl:stylesheet>
