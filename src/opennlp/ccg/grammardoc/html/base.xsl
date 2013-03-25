<?xml version="1.0"?>
<!-- 
	$Id: base.xsl,v 1.2 2007/04/18 22:43:07 coffeeblack Exp $
	Author: Scott Martin (http://www.ling.osu.edu/~scott/)
-->
<xsl:stylesheet version="1.0"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:strip-space elements="*"/>
	
	<xsl:import href="navigation.xsl"/>
	<xsl:import href="comments.xsl"/>
	
	<xsl:output
		method="xml"
		omit-xml-declaration="yes"
		media-type="text/html"
		encoding="ISO-8859-1"
		indent="yes"	
		doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"/>
	
	<xsl:template match="grammar|ccg-lexicon|morph|types|rules">
		<xsl:variable name="grammar-name"
			select="document('grammar.xml')/grammar/@name"/>
		<xsl:variable name="page-name">
			<xsl:choose>
				<xsl:when test="self::grammar">grammar</xsl:when>
				<xsl:when test="self::ccg-lexicon">lexicon</xsl:when>
				<xsl:when test="self::morph">morph</xsl:when>
				<xsl:when test="self::types">types</xsl:when>
				<xsl:when test="self::rules">rules</xsl:when>
			</xsl:choose>
		</xsl:variable>
	
		<html lang="en">
			<head>
				<title>
					<xsl:text>Documentation for CCG grammar </xsl:text>
					<xsl:value-of
						select="$grammar-name"/>
					<xsl:if test="not($page-name='grammar')">
						<xsl:text> : </xsl:text>
						<xsl:call-template name="capitalize">
							<xsl:with-param name="word" select="$page-name"/>
						</xsl:call-template>
					</xsl:if>
				</title>
				<meta name="author" content="Scott Martin"/>
				<meta name="description">
					<xsl:attribute name="content">
						<xsl:text>Generated documentation for </xsl:text>
						<xsl:text>the CCG grammar </xsl:text>
						<xsl:value-of select="$grammar-name"/>
					</xsl:attribute>
				</meta>
				<link rel="author" title="Author: Scott Martin"
					href="http://www.ling.osu.edu/~scott/" type="text/html"/>
				<link rel="stylesheet" name="GrammarDoc" href="grammardoc.css"/>
				<xsl:if test="$page-name='lexicon' or $page-name='rules'">
					<script type="text/javascript" src="lexicon.js">
						<xsl:text> </xsl:text>
					</script>
				</xsl:if>
			</head>
			<body>
				<h1>
					<xsl:call-template name="capitalize">
						<xsl:with-param name="word" select="$page-name"/>
					</xsl:call-template>
					<xsl:text> </xsl:text>
					<xsl:value-of select="$grammar-name"/>
				</h1>
				
				<xsl:call-template name="navigation-generator">
					<xsl:with-param name="current" select="$page-name"/>
				</xsl:call-template>
				
				<div id="container">
					<xsl:call-template name="comment-handler"/>
					<xsl:apply-templates
						select="current()"
						mode="invoked"/>
				</div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template name="capitalize">
		<xsl:param name="word"></xsl:param>
		<xsl:if test="not(word='')">
			<xsl:value-of select="translate(substring($word, 1, 1),
				'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
			<xsl:value-of select="substring($word, 2)"/>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>