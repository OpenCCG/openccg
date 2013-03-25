<?xml version="1.0"?>
<xsl:transform
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xalan xalan2 java">

  <xsl:output method="xml" indent="yes" xalan2:indent-amount="2" omit-xml-declaration = "yes"/>
  <xsl:strip-space elements="*"/>
  
  
  <!--Transform which extracts unary rules (incl those for conjs) into rules.xml. -->
  
  <!--Declaring java class to store rules,their freqs-->
  <xsl:variable name="obj" select="java:opennlp.ccgbank.extract.RulesTally.new()"/>
    
  <xsl:template match="Derivation">
    <xsl:apply-templates />
  </xsl:template>

  
  <xsl:template match = "Treenode">
    
    <!--Store ccgbank id for recording example sentences-->	
    <xsl:if test="@Header">
      <xsl:variable name="id" select="java:storeId($obj,@Header)"/>
      <!-- nb: need to pretend to use result to avoid having above call "optimized" away -->
    <xsl:variable name="boo" select="$id"/>
    </xsl:if>
    
    <!--Store unary rule in a freq tally-->
    <!--Unary rules present in the xml derivation-->		  
    <xsl:if test="count(*)=2"> <!--and not(@cat0=./*[2]/@cat0)">-->

      <xsl:variable name="rule" select="java:loadTally($obj,@cat,./*[2]/@cat)"/>
      <!--Check if this rule has not been detected hitherto-->		  
      <xsl:if test="java:checkRuleStatus($obj,string($rule))"> 

      	<typechanging name="{$rule}">
        	<arg>
						<xsl:choose>
							<xsl:when test="contains(*[2]/@cat,'$')">
								<xsl:apply-templates select="./*[2]/*[2]/*[1]" mode="unaryRule"/> 
							</xsl:when>
							<xsl:otherwise>
           			<xsl:apply-templates select="./*[2]/*[1]" mode="unaryRule"/> 
							</xsl:otherwise>
						</xsl:choose>
						<!--<xsl:apply-templates select="./*[2]/*[1]" mode="unaryRule"/>--> 
          </arg>
          <result>
           	<xsl:apply-templates select="./*[1]" mode="unaryRule"/> 
          </result>
        </typechanging>
      </xsl:if> 
    </xsl:if>
    <xsl:apply-templates/>
  
  </xsl:template>
  
  
  <!--Special template for extracting unary rules in the xml-->
  <xsl:template match="@*|node()" mode="unaryRule">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="unaryRule"/>
    </xsl:copy>
  </xsl:template>
      
</xsl:transform>


