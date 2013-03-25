<?xml version="1.0"?>
<!-- 
	$Id: rules.xsl,v 1.5 2006/12/13 19:25:22 coffeeblack Exp $
	Author: Scott Martin (http://www.ling.osu.edu/~scott/)
 -->
<xsl:stylesheet version="1.0"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="base.xsl"/>
	<xsl:import href="categories.xsl"/>
	
	<xsl:template match="rules" mode="invoked">
		<dl class="shortcuts box">
			<dt>List of Rules</dt>
			<dd>
				<ul>
					<xsl:if test="application">
						<li>
							<a href="#application">Application</a>
						</li>
					</xsl:if>
					<xsl:if test="composition">
						<li>
							<a href="#composition">Composition</a>
						</li>
					</xsl:if>
					<xsl:if test="substitution">
						<li>
							<a href="#substitution">Substitution</a>
						</li>
					</xsl:if>
					<xsl:if test="typeraising">
						<li>
							<a href="#typeraising">Typeraising</a>
						</li>
					</xsl:if>
					<xsl:if test="typechanging">
						<li>
							<a href="#typechanging">Typechanging</a>
							<ul>
								<xsl:for-each select="typechanging">
									<xsl:sort select="@name"/>
									<li>
										<a href="#typechanging-{@name}">
											<xsl:value-of select="@name"/>
										</a>
									</li>
								</xsl:for-each>
							</ul>
						</li>
					</xsl:if>
				</ul>
			</dd>
		</dl>

		<xsl:if test="application">
			<a name="application">
				<h2>Application</h2>
			</a>

			<dl class="box">
				<dt>Rules</dt>
				<dd>
					<ul class="rules">
						<xsl:apply-templates select="application"/>
					</ul>
				</dd>
			</dl>
			
			<div class="back">
				<a href="#">Back to top</a>
			</div>
		</xsl:if>
		<xsl:if test="composition">
			<a name="composition">
				<h2>Composition</h2>
			</a>

			<dl class="box">
				<dt>Rules</dt>
				<dd>
					<ul class="rules">
						<xsl:apply-templates select="composition"/>
					</ul>
				</dd>
			</dl>
			
			<div class="back">
				<a href="#">Back to top</a>
			</div>
		</xsl:if>
		<xsl:if test="substitution">
			<a name="substitution">
				<h2>Substitution</h2>
			</a>

			<dl class="box">
				<dt>Rules</dt>
				<dd>
					<ul class="rules">
						<xsl:apply-templates select="substitution"/>
					</ul>
				</dd>
			</dl>
			
			<div class="back">
				<a href="#">Back to top</a>
			</div>
		</xsl:if>
		<xsl:if test="typeraising">
			<a name="typeraising">
				<h2>Typeraising</h2>
			</a>

			<dl class="box">
				<dt>Rules</dt>
				<dd>
					<ul class="rules">
						<xsl:apply-templates select="typeraising"/>
					</ul>
				</dd>
			</dl>
			
			<div class="back">
				<a href="#">Back to top</a>
			</div>
		</xsl:if>
		<xsl:if test="typechanging">
			<a name="typechanging">
				<h2>Typechanging</h2>
			</a>

			<xsl:apply-templates select="typechanging"/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="typechanging">
		<xsl:call-template name="comment-handler"/>
	
		<a name="typechanging-{@name}">
			<h3>
				<xsl:value-of select="@name"/>
			</h3>
		</a>
		
		<dl class="box">
			<dd>
				<a class="unaryRuleExpander" href="javascript://"
					title="expand feature structures"
					onclick="toggleFeatures(this.parentNode);">[+]</a>
				<dl class="unaryRule">
					<dt>
						<xsl:apply-templates select="arg"/>
						<xsl:call-template name="arrow-generator"/>
					</dt>
					<dd class="unaryResult">
						<xsl:apply-templates select="result"/>
					</dd>
				</dl>
			</dd>
		</dl>
		
		<div class="back">
			<a href="#">Back to top</a>
		</div>
	</xsl:template>
	
	<xsl:template match="arg|result">
		<xsl:for-each select="atomcat|complexcat">
			<dl class="category">
				<xsl:if test="name()='atomcat'">
					<dt>
						<xsl:value-of select="@type"/>
					</dt>
				</xsl:if>
	
				<xsl:apply-templates/>
			</dl>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="application">
		<li>
			<xsl:call-template name="dir-handler"/>
			<xsl:call-template name="extra-handler"/>
		</li>
	</xsl:template>
	
	<xsl:template match="composition">
		<li>
			<xsl:call-template name="dir-handler"/>	
			<xsl:text>B</xsl:text>
			<xsl:call-template name="extra-handler"/>
		</li>
	</xsl:template>
	
	<xsl:template match="typeraising">
		<xsl:variable name="arg">
			<xsl:choose>
				<xsl:when test="arg">
					<xsl:value-of select="arg/atomcat/@type"/>
				</xsl:when>
				<xsl:otherwise>np</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="result">
			<xsl:choose>
				<xsl:when test="result">
					<xsl:value-of select="result/atomcat/@type"/>
				</xsl:when>
				<xsl:otherwise>s</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="firstSlash">
			<xsl:choose>
				<xsl:when test="@dir='backward'">\</xsl:when>
				<xsl:otherwise>/</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<li>
			<dl class="category">
				<dt>
					<xsl:value-of select="$arg"/>					
				</dt>
				<xsl:call-template name="arrow-generator"/>
				<dd class="category-container">
					<dl class="category">
						<dt>
							<xsl:value-of select="$result"/>
						</dt>
						<xsl:call-template name="dollar-handler"/>
						<dt>
							<xsl:value-of select="$firstSlash"/>
						</dt>
						<xsl:text>(</xsl:text>
						<dd class="category-container">
							<dl class="category">
								<dt>
									<xsl:value-of select="$result"/>
								</dt>
								<xsl:call-template name="dollar-handler"/>
								<dt>
									<xsl:choose>
										<xsl:when
											test="$firstSlash='/'">\</xsl:when>
										<xsl:otherwise>/</xsl:otherwise>
									</xsl:choose>
								</dt>
								<dt>
									<xsl:value-of select="$arg"/>
								</dt>
							</dl>
						</dd>
						<xsl:text>)</xsl:text>
					</dl>
				</dd>
			</dl>
		</li>
	</xsl:template>
	
	<xsl:template name="dollar-handler">
		<xsl:if test="@useDollar='true'">
			<dd>
				<xsl:text>$</xsl:text>
				<span class="ruleLabel">1</span>
			</dd>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="substitution">
		<li>
			<xsl:call-template name="dir-handler"/>	
			<xsl:text>S</xsl:text>
			<xsl:call-template name="extra-handler"/>
		</li>
	</xsl:template>
	
	<xsl:template name="dir-handler">
		<xsl:choose>
			<xsl:when test="@dir='forward'">
				<acronym title="forward">&gt;</acronym>
			</xsl:when>
			<xsl:otherwise>
				<acronym title="backward">&lt;</acronym>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="extra-handler">
		<xsl:if test="@harmonic='false'">
			<span class="ruleLabel"><acronym title="crossing">x</acronym></span>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="arrow-generator">
		<span class="arrow">
			<xsl:text disable-output-escaping="yes">&amp;</xsl:text>nbsp;<xsl:text disable-output-escaping="yes">&amp;</xsl:text>mdash;&gt;<xsl:text disable-output-escaping="yes">&amp;</xsl:text>nbsp;
		</span>
	</xsl:template>

</xsl:stylesheet>
