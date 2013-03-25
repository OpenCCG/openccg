<?xml version="1.0"?>
<!-- 
	$Id: types.xsl,v 1.2 2006/12/13 19:25:22 coffeeblack Exp $
	Author: Scott Martin (http://www.ling.osu.edu/~scott/)
-->
<xsl:stylesheet version="1.0"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="base.xsl"/>
	
	<xsl:template match="types" mode="invoked">
		<dl class="box">
			<dt>Type Hierarchy</dt>
			<dd>
				<ul>
					<xsl:apply-templates select="type[
							not(@parents) or @parents='']">
						<xsl:sort select="@name"/>
					</xsl:apply-templates>
				</ul>
			</dd>
		</dl>
	</xsl:template>
	
	<xsl:template match="type">
		<xsl:variable name="name" select="@name"/>
		<xsl:variable name="children" select="//type[@parents=$name
				or starts-with(@parents, concat($name, ' '))
				or contains(@parents, concat(' ', $name, ' '))
				or substring(@parents,
					(string-length(@parents) - (string-length($name) + 1)) + 1)
						= concat(' ', $name)]"/>
		
		<li>
			<xsl:value-of select="$name"/>
			
			<xsl:if test="count($children) &gt; 0">
				<ul>
					<xsl:apply-templates select="$children">
						<xsl:sort select="@name"/>
					</xsl:apply-templates>
				</ul>
			</xsl:if>
		</li>
	</xsl:template>
	
</xsl:stylesheet>