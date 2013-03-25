<?xml version="1.0"?>
<!-- 
	$Id: grammar.xsl,v 1.2 2006/12/13 19:25:22 coffeeblack Exp $
	Author: Scott Martin (http://www.ling.osu.edu/~scott/)
 -->
<xsl:stylesheet version="1.0"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:import href="base.xsl" />
	
	<xsl:template match="grammar" mode="invoked">
		<xsl:apply-imports/>
	</xsl:template>
	
	<xsl:template name="content-generator"/>
</xsl:stylesheet>