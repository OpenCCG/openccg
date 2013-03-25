<!--Copyright (C) 2005-2009 Scott Martin, Rajakrishan Rajkumar and Michael White
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.-->


<xsl:transform
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xalan xalan2 java">

<xsl:output method="xml" indent="yes" xalan2:indent-amount="2" omit-xml-declaration = "yes"/>

<xsl:strip-space elements="*"/>

<!--Java helper class-->
<xsl:variable name="obj" select="java:opennlp.ccgbank.convert.PunctHelper.new()"/>
<!--Transform which identifies & labels reported speech constr (both sentence medial as well as sentence final-->

<xsl:template match="/">
    <xsl:apply-templates/>
</xsl:template>

<xsl:template match="Treenode[@Header]">
	<xsl:variable name="dummy0" select="java:storePOS($obj,@Header)"/>
	<Treenode>
		<xsl:apply-templates select="@*|node()"/>
	</Treenode>
</xsl:template>

<!--Cat 1: Say verb is the anchor-->
<!--Treenode above the comma in question: s\np/s , ==> s\np/s:17-->

<xsl:template match="Treenode[java:removeFeats($obj,@cat0)='(S\NP)/S' and java:removeFeats($obj,*[2][self::Leafnode]/@cat0)='(S\NP)/S' and *[3]/@pos1=',']">

	<Treenode>
		<xsl:copy-of select="@*"/>
		<xsl:apply-templates select="*[1]"/>
		<Leafnode>
  		<xsl:copy-of select="*[2]/@*"/>
      <xsl:attribute name="cat0"><xsl:value-of select="concat(*[2]/@cat0,'_SAY-VERB')"/></xsl:attribute>
			<complexcat>
				<xsl:apply-templates select="*[2]/*[1]/*[position() &lt; last()-1]"/>
        <slash dir="/" mode="&gt;"/>
        <atomcat type="s">
        	<fs id="3">
        		<feat attr="form" val="{*[2]/*[1]/atomcat[3]/fs/feat[@attr='form']/@val}"/>
          </fs>
        </atomcat>
        <slash dir="/" mode="*"/>
        <atomcat type="punct">
        	<fs id="4">
          	<feat attr="lex" val=","/>
          </fs>
        </atomcat>
      </complexcat>
		</Leafnode>
		<Leafnode cat="punct[,]_1" lexeme="," pos="," pos1="PUNCT_COMMA" cat0=",">
			<xsl:copy-of select="*[3]/@*[not(name()='pos1')]"/>
    	<atomcat type="punct">
      	<fs id="1">
					<feat attr="lex" val=","/>
      	</fs>
      </atomcat>
    </Leafnode>
	</Treenode>
</xsl:template>

<!--Cat 2: Labels inv reported speech parenthetical-->
<xsl:template match="Treenode[((@cat0='S[dcl]' and starts-with(@ptb-tag0,'SINV')) or  (@cat0='S[dcl]/S[dcl]' or @cat0='S[dcl]\S[dcl]')) and ../*[1][self::complexcat] and preceding::Leafnode[1][@pos1=',' and ancestor::Treenode[@Header=(java:getPOS($obj))]]]">

	<xsl:variable name="comma1" select="preceding::Leafnode[1]"/>
	<xsl:variable name="comma2" select="following::Leafnode[1][ancestor::Treenode[@Header=(java:getPOS($obj))]]"/>
	<xsl:variable name="res" select="../*[1]"/>
	<xsl:variable name="arg" select="*[1]"/>

	<Treenode>
		<xsl:apply-templates select="../@*"/>
		<!--<complexcat>
			<xsl:call-template name = "res-choice">
				<xsl:with-param name = "res"  select="$res"/>
 	    	<xsl:with-param name = "arg" select="$arg"/>
			</xsl:call-template>
		</complexcat>-->
		<xsl:apply-templates select="$res"/>
		
		<!--Dealing with balanced and unbalanced cases-->
		<xsl:choose>
			<xsl:when test="$comma2/@pos1=','">
				<Treenode>
					<xsl:apply-templates select="../@*"/>
					<complexcat>
						<xsl:apply-templates select="$res/*"/>
						<slash dir="/" mode="*"/>
						<atomcat type="punct">
      				<fs id="1">
      	  			<feat attr="lex" val=","/>
        			</fs>
      			</atomcat>
					</complexcat>
					<xsl:call-template name = "punct-cat">
						<xsl:with-param name = "comma1"  select="$comma1"/>
 	    			<xsl:with-param name = "comma2" select="$comma2"/>
					</xsl:call-template>
				</Treenode>
				<Leafnode pos="," pos1="PUNCT_SAY_BAL">
					<xsl:apply-templates select="$comma2/@*[not(name()='pos1')]"/>
					<atomcat type="punct">
      			<fs id="1">
      				<feat attr="lex" val=","/>
        		</fs>
    			</atomcat>
				</Leafnode>	
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name = "punct-cat">
					<xsl:with-param name = "comma1"  select="$comma1"/>
 	    		<xsl:with-param name = "comma2" select="$comma2"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</Treenode>		
</xsl:template>

<xsl:template name="punct-cat">
	<xsl:param name="comma1"/>
	<xsl:param name="comma2"/>

	<xsl:variable name="res" select="../*[1]"/>
	<xsl:variable name="arg" select="*[1]"/>

	<Leafnode pos="," pos1="PUNCT_SAY">
		<xsl:apply-templates select="$comma1/@*[not(name()='pos1')]"/>
		<complexcat>
	
			<xsl:variable name="void1" select="java:globalInit($obj)"/>
			<xsl:variable name="void" select="java:initPOS($obj)"/>
			<xsl:call-template name = "res-choice">
				<xsl:with-param name = "res"  select="$res"/>
 	    	<xsl:with-param name = "arg" select="$arg"/>
			</xsl:call-template>

			<slash dir="/" mode="*"/>
			<xsl:if test="$comma2/@pos1=','">
				<atomcat type="punct">
      		<fs id="7">
      	  	<feat attr="lex" val=","/>
        	</fs>
      	</atomcat>
				<slash dir="/" mode="*"/>
			</xsl:if>
			<xsl:apply-templates select="$arg" mode="arg"/>
			<xsl:variable name="void2" select="java:globalInit($obj)"/>		
		</complexcat>
	</Leafnode>
	<Treenode constr="INV-REPORTED-SPEECH">
		<xsl:apply-templates select="@*|node()"/>
	</Treenode>		
</xsl:template>

<!--Provision for choice of result cat-->

<xsl:template name="res-choice">
	<xsl:param name="res"/>
	<xsl:param name="arg"/>
		
	<xsl:choose>
		<!--<xsl:when test="$res[self::atomcat]">
			<xsl:apply-templates select="$arg/*" mode="res"/>
		</xsl:when>-->
		<xsl:when test="parent::Treenode[starts-with(@cat0,'(S\NP)')]">
			<xsl:apply-templates select="$res/*" mode="res"/>
			<xsl:variable name="id" select="java:setglobalId($obj,3)"/>		
		</xsl:when>
		<xsl:otherwise>
			<xsl:apply-templates select="$res/*" mode="res"/>
			<xsl:variable name="id" select="java:globalInit($obj)"/>				
		</xsl:otherwise>
	</xsl:choose>	
</xsl:template>

<xsl:template match="atomcat[@type='s']" mode="res">

	<xsl:variable name="id" select="java:getPOS($obj)"/>		

	<atomcat type="s">
		<xsl:choose>
			<xsl:when test="string-length($id) &gt; 0">
				<fs id="2"/>
				<xsl:variable name="void" select="java:initPOS($obj)"/>
			</xsl:when>
			<xsl:otherwise>
				<fs inheritsFrom="2"/>
				<xsl:variable name="void" select="java:storePOS($obj,'1')"/>		
			</xsl:otherwise>
		</xsl:choose>
	</atomcat>
</xsl:template>
<xsl:template match="atomcat[@type='np']" mode="res">
	<atomcat type="np">
		<fs id="3"/>
	</atomcat>
</xsl:template>

<xsl:template match="atomcat[@type='s']" mode="arg">

	<xsl:variable name="id" select="java:getglobalId($obj)"/>		
	<atomcat type="s">
		<fs id="{$id}">
		</fs>
	</atomcat>
	
</xsl:template>

<!-- Spl copy rule 1-->
  <xsl:template match="@*|node()" mode="res">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="res"/>
    </xsl:copy>
  </xsl:template>

<!-- Spl copy rule 2-->
  <xsl:template match="@*|node()" mode="arg">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="arg"/>
    </xsl:copy>
  </xsl:template>

<!--Default global copy rule-->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>