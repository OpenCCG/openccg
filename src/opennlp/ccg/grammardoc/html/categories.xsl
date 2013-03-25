<?xml version="1.0"?>
<!-- 
	$Id: categories.xsl,v 1.12 2007/04/19 22:06:47 coffeeblack Exp $
	Author: Scott Martin (http://www.ling.osu.edu/~scott/)
-->
<xsl:stylesheet version="1.0"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:template match="fs/feat/lf">
		<xsl:value-of select="nomvar/@name"/>
	</xsl:template>
	
	<!--
		an atomic category occuring either within an entry or within a
		complex category	
	-->
	<xsl:template match="complexcat/atomcat|setarg/atomcat">
		<xsl:if test="@type">
			<dt>
				<xsl:value-of select="@type"/>
			</dt>
		</xsl:if>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="complexcat/complexcat|atomcat/complexcat">
		<span class="enclosure">(</span>
		<dd class="category-container">
			<dl class="category">
				<xsl:apply-templates/>
			</dl>
		</dd>
		<span class="enclosure">)</span>
	</xsl:template>
	
	<xsl:template match="fs">
		<dd>
			<dl class="feature-structure">
				<xsl:if test="@id or @inheritsFrom">
					<dt class="fsid">
						<xsl:text>&lt;</xsl:text>
						<xsl:choose>
							<xsl:when test="@id">
								<xsl:value-of select="@id"/>
							</xsl:when>
							<xsl:when test="@inheritsFrom">
								<xsl:text>~</xsl:text><xsl:value-of
									select="@inheritsFrom"/>
							</xsl:when>
						</xsl:choose>
						<xsl:text>&gt;</xsl:text>
					</dt>
				</xsl:if>
				
				<xsl:apply-templates/>
			</dl>
		</dd>
	</xsl:template>
	
	<xsl:template match="fs/feat">
		<dd class="feat">
			<xsl:choose>
				<xsl:when
					test="preceding-sibling::feat[1][@val or featvar or lf]">
						<xsl:text>,</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<span class="enclosure">[</span>
				</xsl:otherwise>
			</xsl:choose>
			
			<acronym title="{@attr}">		
				<xsl:choose>
					<xsl:when test="@val">
						<xsl:value-of select="@val"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates/>
					</xsl:otherwise>
				</xsl:choose>
			</acronym>

			<xsl:if test="not(following-sibling::feat)">
				<span class="enclosure">]</span>
			</xsl:if>
		</dd>
	</xsl:template>
	
	<xsl:template match="fs/feat/featvar">
		<xsl:value-of select="@name"/>
	</xsl:template>
	
	<xsl:template match="fs/feat/lf">
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="fs/feat/lf/nomvar|fs/feat/lf/prop|fs/feat/lf/var">
		<xsl:value-of select="@name"/>
	</xsl:template>
	
	<xsl:template match="complexcat/setarg">
		<span class="enclosure">{</span>
		<xsl:apply-templates/>
		<span class="enclosure">}</span>
	</xsl:template>
	
	<xsl:template match="complexcat/slash|setarg/slash">
		<dt>
			<xsl:attribute name="class">
				<xsl:text>slash</xsl:text>
				<xsl:if test="not(@dir) and following-sibling::node()[
					not(self::text())][1][self::dollar]">
					<xsl:text> redundant</xsl:text>
				</xsl:if>
			</xsl:attribute>
			
			<xsl:variable name="mode">
				<xsl:choose>
					<xsl:when test="@mode">
						<xsl:value-of select="@mode"/>
					</xsl:when>
					<xsl:otherwise>.</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			
			<xsl:choose>
				<xsl:when test="@dir">
					<xsl:value-of select="@dir"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>|</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
			
			<xsl:variable name="mode-text">
				<xsl:choose>
					<xsl:when test="$mode='*'">
						<xsl:text>application only</xsl:text>
					</xsl:when>
					<xsl:when test="$mode='^'">
						<xsl:text>associative</xsl:text>
					</xsl:when>
					<xsl:when test="$mode='x'">
						<xsl:text>permutative</xsl:text>
					</xsl:when>
					<xsl:when test="$mode='x&gt;'">
						<xsl:text>permutative right</xsl:text>
					</xsl:when>
					<xsl:when test="$mode='&lt;x'">
						<xsl:text>permutative left</xsl:text>
					</xsl:when>
					<xsl:when test="$mode='&gt;'">
						<xsl:text>associative permutative right</xsl:text>
					</xsl:when>
					<xsl:when test="$mode='&lt;'">
						<xsl:text>associative permutative left</xsl:text>
					</xsl:when>
					<xsl:when test="$mode='.'">
						<xsl:text>all rules</xsl:text>
					</xsl:when>
				</xsl:choose>
			</xsl:variable>
			
			<span class="mode">
				<acronym title="{$mode-text}">
					<xsl:value-of select="$mode"/>
				</acronym>
			</span>
		</dt>
	</xsl:template>
	
	<xsl:template match="atomcat/lf|complexcat/lf">
		<dt>
			<dl class="lf">
				<span class="divider"> : </span>
				<xsl:apply-templates/>
			</dl>
		</dt>
	</xsl:template>
	
	<xsl:template match="lf/satop">
		<xsl:if test="preceding-sibling::*">
			<span class="divider"> ^ </span>
		</xsl:if>
		<dt>@</dt>
		<dd class="nomvar">
			<xsl:value-of select="@nomvar"/>
		</dd>
		<dt>
			<span class="enclosure">(</span>
			<xsl:apply-templates/>
			<span class="enclosure">)</span>
		</dt>
	</xsl:template>
	
	<xsl:template match="satop/prop">
		<span class="prop">
			<xsl:value-of select="@name"/>
		</span>
	</xsl:template>
	
	<xsl:template match="diamond">
		<xsl:variable name="diamondSingleton"
			select="parent::diamond
				and not(preceding-sibling::* or following-sibling::*)"/>
		<xsl:variable name="satopSibling"
			select="parent::satop and preceding-sibling::*"/>
		<xsl:variable name="enclosedChildren"
			select="child::diamond or child::prop or child::var"/>
	
		<xsl:if test="parent::diamond or $satopSibling">
			<span class="divider"> ^ </span>
			<xsl:if test="$diamondSingleton">
				<span class="enclosure">(</span>
			</xsl:if>
		</xsl:if>
		
		<xsl:text>&lt;</xsl:text>
		<xsl:value-of select="@mode"/>
		<xsl:text>&gt;</xsl:text>
		
		<xsl:if test="$enclosedChildren">
			<span class="enclosure">(</span>
		</xsl:if>
		
		<xsl:apply-templates select="diamond|nomvar|prop|var"/>
		
		<xsl:if test="$enclosedChildren">
			<span class="enclosure">)</span>
		</xsl:if>
		
		<xsl:if test="$diamondSingleton">
			<span class="enclosure">)</span>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="diamond/prop|diamond/nomvar|diamond/var">
		<xsl:if test="preceding-sibling::*">
			<span class="divider"> ^ </span>
		</xsl:if>
	
		<span class="prop">
			<xsl:value-of select="@name"/>
		</span>
	</xsl:template>

	<xsl:template match="complexcat/dollar">
		<dt class="dollar">$</dt>
		<dd>
			<xsl:value-of select="@name"/>
		</dd>
	</xsl:template>

</xsl:stylesheet>