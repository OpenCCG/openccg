<?xml version="1.0"?>
<!-- 
	$Id: morph.xsl,v 1.12 2006/12/13 19:25:22 coffeeblack Exp $
	Author: Scott Martin (http://www.ling.osu.edu/~scott/)
 -->
<xsl:stylesheet version="1.0"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="base.xsl"/>
	<xsl:import href="categories.xsl"/>
	
	<xsl:template match="morph" mode="invoked">
		<dl class="shortcuts entries box">
			<dt>Entries</dt>
			<dd>
				<dl>
					<xsl:for-each select="entry[
							not(@pos=preceding-sibling::entry/@pos)]">
						<!-- sort by pos -->
						<xsl:sort select="@pos"/>
						<xsl:variable name="entryPos" select="@pos"/>
						
						<dt>
							<a href="#{$entryPos}">
								<xsl:value-of select="$entryPos"/>
							</a>
						</dt>
						<dd>
							<!-- show all stems in pos -->
							<ul class="short">
								<xsl:for-each select="//entry[@pos=$entryPos]">
									<xsl:sort select="@stem"/>	
									<xsl:sort select="@word"/>
									
									<xsl:variable name="entryStem"
										select="@stem"/>
								
									<xsl:variable name="linkContent">
										<xsl:choose>
											<xsl:when test="not(@stem or @word
												=preceding-sibling::entry[
													@pos=$entryPos]/@word)">
												<xsl:value-of select="@word"/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:choose>
													<xsl:when test="@stem and
														not(preceding-sibling::
															entry[
																@pos=$entryPos
																and (@word
																	=$entryStem
																or @stem
																	=$entryStem)
																]
															)">
														<xsl:value-of
															select="@stem"/>
													</xsl:when>
												</xsl:choose>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:variable>
									
									<xsl:if test="not($linkContent='')">
										<li>
											<a>
												<xsl:attribute name="href">
													<xsl:text>#</xsl:text>
													<xsl:value-of
														select="$entryPos"/>
													<xsl:text>-</xsl:text>
													<xsl:value-of
														select="$linkContent"/>
												</xsl:attribute>
												<xsl:value-of
													select="$linkContent"/>
											</a>
										</li>
									</xsl:if>
								</xsl:for-each>
							</ul>
						</dd>
					</xsl:for-each>
				</dl>
			</dd>
		</dl>
					
		<dl class="shortcuts box">
			<dt>Macros</dt>
			<dd>
				<ul>
					<xsl:for-each select="macro">
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
		
		<h2>Entries</h2>
		<xsl:for-each select="entry[not(@pos=preceding-sibling::entry/@pos)]">
			<xsl:sort select="@pos"/>
			
			<xsl:variable name="entryPos" select="@pos"/>
			<xsl:variable name="entryStem" select="@stem"/>
			
			<xsl:call-template name="comment-handler"/>
			
			<h2>
				<a name="{$entryPos}">
					<xsl:value-of select="$entryPos"/>
				</a>
			</h2>
			
			<xsl:apply-templates select="//entry[@pos=$entryPos]">
				<xsl:sort select="@word"/>
			</xsl:apply-templates>
		</xsl:for-each>
		
		<h2>Macros</h2>
		<xsl:apply-templates select="macro">
			<xsl:sort select="@name" data-type="text"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="entry">
		<xsl:variable name="entryPos" select="@pos"/>
		<xsl:variable name="entryWord" select="@word"/>
		<xsl:variable name="entryStem" select="@stem"/>
		<xsl:variable name="entryName">
			<xsl:choose>
				<xsl:when test="@stem
					and not(preceding-sibling::entry[@pos=$entryPos
						and (@word=$entryStem or @stem=$entryStem)])">
					<xsl:value-of select="$entryStem"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$entryWord"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:call-template name="comment-handler"/>
		
		<xsl:if test="not($entryStem=preceding-sibling::entry[
			@pos=$entryPos]/@word
				or $entryStem=preceding-sibling::entry[@pos=$entryPos]/@stem
				or $entryWord=preceding-sibling::entry[@pos=$entryPos]/@word)">
			<h3>
				<a name="{$entryPos}-{$entryName}">
					<xsl:value-of select="$entryName"/>
				</a>
			</h3>
			
			<xsl:if test="document('lexicon.xml')//family[@pos=$entryPos]">
				<strong>Member of: </strong>
				<ul class="short">
					<xsl:for-each select="document('lexicon.xml')//family[
						@pos=$entryPos and 
						((not(@closed) or @closed='false')
							or (@closed='true' and 
								member[@stem=$entryName]))]">
						<li>
							<a href="lexicon.html#{@name}">
								<xsl:value-of select="@name"/>
							</a>
						</li>
					</xsl:for-each>
				</ul>
			</xsl:if>
	
			<dl class="shortcuts box">
				<xsl:apply-templates select="//entry[@pos=$entryPos
						and(@word=$entryName or @stem=$entryName)
						and not(@word=preceding-sibling::entry[
							@pos=$entryPos]/@word)]"
					mode="invoked">
					<xsl:sort select="@word"/>
				</xsl:apply-templates>
			</dl>
			
			<div class="back">
				<a href="#">Back to top</a>
			</div>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="entry" mode="invoked">
		<dt>
			<xsl:value-of select="@word"/>
		</dt>
		<dd>
			<xsl:call-template name="macros-handler"/>
		</dd>
	</xsl:template>
	
	<xsl:template name="macros-handler">
		<xsl:if test="@macros and not(@macros='')">
			<ul class="short">
				<xsl:call-template name="macro-link-generator">
					<xsl:with-param name="macro-list" select="@macros"/>
				</xsl:call-template>
			</ul>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="macro-link-generator">
		<xsl:param name="macro-list"/>
		
		<xsl:if test="not($macro-list='')">
			<xsl:variable name="macro">
				<xsl:choose>
					<xsl:when test="contains($macro-list, ' ')">
						<xsl:value-of select="substring-before(
							$macro-list, ' ')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$macro-list"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<li>
				<a href="#{$macro}">
					<xsl:value-of select="$macro"/>
				</a>
			</li>
			
			<!-- call self recursively with remainder of list, if any -->
			<xsl:if test="contains($macro-list, ' ')">
				<xsl:call-template name="macro-link-generator">
					<xsl:with-param name="macro-list"
						select="substring-after($macro-list, ' ')"/>
				</xsl:call-template>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="macro">
		<h3>
			<a name="{@name}">
				<xsl:value-of select="@name"/>
			</a>
		</h3>
		<xsl:if test="fs">
			<dl class="box">
				<dt>Feature Structures</dt>
				<dd>
					<dl class="category">
						<xsl:apply-templates select="fs"/>
					</dl>
				</dd>
			</dl>
		</xsl:if>
		<xsl:if test="lf">
			<dl class="box">
				<dt>Logical Form</dt>
				<xsl:apply-templates select="lf"/>
			</dl>
		</xsl:if>
		
		<div class="back">
			<a href="#">Back to top</a>
		</div>
	</xsl:template>
	
	<xsl:template match="macro/fs">
		<dt>
			<xsl:value-of select="@id"/>
		</dt>
		<dd>
			<xsl:choose>
				<xsl:when test="feat">
					<xsl:apply-templates/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@attr"/>
					<xsl:text>=</xsl:text>
					<xsl:value-of select="@val"/>
				</xsl:otherwise>
			</xsl:choose>
		</dd>
	</xsl:template>
	
	<xsl:template match="macro/fs/feat">
		<xsl:if test="preceding-sibling::feat">, </xsl:if>
		<xsl:value-of select="@attr"/>
		<xsl:if test="@val or lf">
			<xsl:text>=</xsl:text>
			<xsl:choose>
				<xsl:when test="@val">
					<xsl:value-of select="@val"/>
				</xsl:when>
				<xsl:when test="lf">
					<xsl:choose>
						<xsl:when test="lf/prop">
							<xsl:value-of select="lf/prop/@name"/>
						</xsl:when>
						<xsl:when test="lf/var">
							<em>
								<xsl:value-of select="lf/var/@name"/>
							</em>
						</xsl:when>
					</xsl:choose>
				</xsl:when>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="lf">
		<dd>
			<dl class="category lf">
				<xsl:apply-templates/>
			</dl>
		</dd>
	</xsl:template>	
	
</xsl:stylesheet>
