<?xml version="1.0"?>
<!-- 
	$Id: comments.xsl,v 1.3 2006/12/13 19:25:22 coffeeblack Exp $
	Author: Scott Martin (http://www.ling.osu.edu/~scott/)
-->
<xsl:stylesheet version="1.0"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:template name="comment-handler">
		<xsl:if test="preceding-sibling::node()[
				not(self::text())][1][self::comment()]">
			<xsl:call-template name="comment-generator">
				<xsl:with-param name="text"
					select="preceding-sibling::node()[not(self::text())][1]"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="comment-generator">
		<xsl:param name="text"/>
		<xsl:if test="not(text='')">
			<pre>
				<xsl:value-of select="$text"/>
			</pre>
		</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>