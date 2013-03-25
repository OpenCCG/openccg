<?xml version="1.0"?>
<!-- 
Copyright (C) 2003-4 University of Edinburgh (Michael White) 
$Revision: 1.34 $, $Date: 2008/12/12 05:09:29 $ 

NB: These namespace declarations seem to work with the version of Xalan 
    that comes with JDK 1.4.  With newer versions of Xalan, 
    different namespace declarations may be required. 
-->
<xsl:transform 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xalan2="http://xml.apache.org/xslt"
  exclude-result-prefixes="xalan xalan2">
  

  <!-- ***** Import Core Dictionary Definitions ***** -->
  <xsl:import href="../core-en/dict.xsl"/>
  
  <xsl:output indent="yes" xalan2:indent-amount="2"/> 
  <xsl:strip-space elements="*"/>

  
  <!-- ***** Start Output Here ***** -->
  <xsl:template match="/">
  <dictionary name="flights"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation="../dict.xsd"
  >
  
  <!-- Add core entries -->
  <xsl:call-template name="add-entries"/>

  <!-- NB: A (temp?) hack: the LL-as-LH family allows LL boundaries to appear in the middle of 
           lists, as if they were LH boundaries.  It might be nicer to use underspecification. -->
  <entry stem="LL%" pos="BT">
    <member-of family="BoundaryTone-LL%-as-LH%"/>
  </entry>
  
  <!-- 'and' as punctuation -->
  <entry stem="and" pos="and">
    <member-of family="And-Punct"/>
  </entry>
  
  <!-- Prepositions, Temporal Adverbials -->
  <entry stem="about" pos="Prep">
    <member-of family="Prep-Nom"/>
  </entry>
  <entry stem="after" pos="TempAdv">
    <member-of family="Prep-TimeRel"/>
  </entry>
  <entry stem="at" pos="Prep">
    <member-of family="Prep-Nom"/>
    <member-of family="Prep-Time"/>
  </entry>
  <entry stem="before" pos="TempAdv">
    <member-of family="Prep-TimeRel"/>
  </entry>
  <entry stem="between" pos="TempAdv">
    <member-of family="Between-TimeRel"/>
  </entry>
  <entry stem="by" pos="TempAdv">
    <member-of family="Prep-TimeRel"/>
  </entry>
  <entry stem="earlier" pos="TempAdv">
    <member-of family="Comparative-TimeRel"/>
  </entry>
  <entry stem="for" pos="Prep">
    <member-of family="Prep-Nom"/>
  </entry>
  <entry stem="from" pos="Prep">
    <member-of family="Prep-Nom"/>
  </entry>
  <entry stem="in" pos="Prep">
    <member-of family="Prep-Nom"/>
    <member-of family="Prep-TimeFrame"/>
  </entry>
  <entry stem="later" pos="TempAdv">
    <member-of family="Comparative-TimeRel"/>
  </entry>
  <entry stem="of" pos="Prep">
    <member-of family="Prep-Nom"/>
  </entry>
  <entry stem="on" pos="Prep">
    <member-of family="Prep-Nom"/>
    <member-of family="Prep-Date"/>
    <member-of family="Prep-Airline"/>
  </entry>
  <entry stem="on" pos="Adj">
    <member-of family="Prep-Loc"/>
  </entry>
  <entry stem="to" pos="Prep">
    <member-of family="Prep-Nom"/>
    <member-of family="To-Infinitive"/>
  </entry>
  <entry stem="until" pos="TempAdv">
    <member-of family="Prep-TimeRel"/>
  </entry>
  <entry stem="with" pos="Prep">
    <member-of family="Prep-Nom"/>
  </entry>


  <!-- Other Adverbs -->
  <entry stem="alternatively" pos="Adv">
    <member-of family="Transitional-Adverb"/>
  </entry>
  <entry stem="however" pos="Adv">
    <member-of family="Transitional-Adverb"/>
  </entry>
  <entry stem="only" pos="Adv">
    <member-of family="Adverb"/>
  </entry>
  <entry stem="on_time" pos="Adv">
    <member-of family="Adverb"/>
  </entry>
  <entry stem="today" pos="Adv">
    <member-of family="Adverb"/>
  </entry>
  <entry stem="yesterday" pos="Adv">
    <member-of family="Adverb"/>
  </entry>

  
  <!-- Verbs -->
  <entry stem="arrive" pos="V">
    <member-of family="Arriving"/>
    <word form="arrive" macros="@base"/>
    <word form="arriving" macros="@ng"/>
    <word form="arrive" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="arrives" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="arrive" macros="@pres @pl-agr"/>
    <word form="arrived" macros="@past"/>
  </entry>

  <entry stem="book" pos="V">
    <member-of family="Booking"/>
    <word form="book" macros="@base"/>
    <word form="booking" macros="@ng"/>
    <word form="book" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="books" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="book" macros="@pres @pl-agr"/>
    <word form="booked" macros="@past"/>
  </entry>

  <entry stem="choose" pos="V">
    <member-of family="Choosing"/>
    <word form="choose" macros="@base"/>
    <word form="choosing" macros="@ng"/>
    <word form="choose" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="chooses" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="choose" macros="@pres @pl-agr"/>
    <word form="chose" macros="@past"/>
  </entry>

  <entry stem="connect" pos="V">
    <member-of family="Connecting"/>
    <word form="connect" macros="@base"/>
    <word form="connecting" macros="@ng"/>
    <word form="connect" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="connects" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="connect" macros="@pres @pl-agr"/>
    <word form="connected" macros="@past"/>
  </entry>

  <entry stem="cost" pos="V">
    <member-of family="Costing"/>
    <word form="cost" macros="@base"/>
    <word form="costing" macros="@ng"/>
    <word form="cost" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="costs" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="cost" macros="@pres @pl-agr"/>
    <word form="cost" macros="@past"/>
  </entry>

  <entry stem="depart" pos="V">
    <member-of family="Departing"/>
    <word form="depart" macros="@base"/>
    <word form="departing" macros="@ng"/>
    <word form="depart" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="departs" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="depart" macros="@pres @pl-agr"/>
    <word form="departed" macros="@past"/>
  </entry>

  <entry stem="find" pos="V">
    <member-of family="Finding"/>
    <word form="find" macros="@base"/>
    <word form="finding" macros="@ng"/>
    <word form="find" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="finds" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="find" macros="@pres @pl-agr"/>
    <word form="found" macros="@past"/>
  </entry>

  <entry stem="fly" pos="V">
    <member-of family="Travel"/>
    <word form="fly" macros="@base"/>
    <word form="flying" macros="@ng"/>
    <word form="fly" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="flies" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="fly" macros="@pres @pl-agr"/>
    <word form="flew" macros="@past"/>
  </entry>

  <entry stem="get" pos="V">
    <member-of family="Conveyance"/>
    <word form="get" macros="@base"/>
    <word form="getting" macros="@ng"/>
    <word form="get" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="gets" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="get" macros="@pres @pl-agr"/>
    <word form="got" macros="@past"/>
  </entry>

  <entry stem="go" pos="V">
    <member-of family="Travel"/>
    <word form="go" macros="@base"/>
    <word form="going" macros="@ng"/>
    <word form="go" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="goes" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="go" macros="@pres @pl-agr"/>
    <word form="went" macros="@past"/>
  </entry>

  <entry stem="hear" pos="V">
    <member-of family="Hearing-About"/>
    <word form="hear" macros="@base"/>
    <word form="hearing" macros="@ng"/>
    <word form="hear" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="hears" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="hear" macros="@pres @pl-agr"/>
    <word form="heard" macros="@past"/>
  </entry>

  <entry stem="leave" pos="V">
    <member-of family="Departing"/>
    <word form="leave" macros="@base"/>
    <word form="leaving" macros="@ng"/>
    <word form="leave" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="leaves" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="leave" macros="@pres @pl-agr"/>
    <word form="left" macros="@past"/>
  </entry>

  <entry stem="match" pos="V">
    <member-of family="Evaluative-Comparison"/>
    <word form="match" macros="@base"/>
    <word form="matching" macros="@ng"/>
    <word form="match" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="matches" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="match" macros="@pres @pl-agr"/>
    <word form="matched" macros="@past"/>
  </entry>

  <entry stem="need" pos="V">
    <member-of family="Needing-To" pred="need-to"/>
    <word form="need" macros="@base"/>
    <word form="needing" macros="@ng"/>
    <word form="need" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="needs" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="need" macros="@pres @pl-agr"/>
    <word form="needed" macros="@past"/>
  </entry>

  <entry stem="offer" pos="V">
    <member-of family="Offering"/>
    <word form="offer" macros="@base"/>
    <word form="offering" macros="@ng"/>
    <word form="offer" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="offers" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="offer" macros="@pres @pl-agr"/>
    <word form="offered" macros="@past"/>
  </entry>

  <!-- nb: 'prefer' in same family as 'like'; not sure about 'want' -->
  <entry stem="prefer" pos="V">
    <member-of family="Experiencer-Subj"/>
    <word form="prefer" macros="@base"/>
    <word form="preferring" macros="@ng"/>
    <word form="prefer" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="prefers" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="prefer" macros="@pres @pl-agr"/>
    <word form="preferred" macros="@past"/>
  </entry>
  
  <entry stem="require" pos="V">
    <member-of family="Requiring"/>
    <word form="require" macros="@base"/>
    <word form="requiring" macros="@ng"/>
    <word form="require" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="requires" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="require" macros="@pres @pl-agr"/>
    <word form="required" macros="@past"/>
  </entry>

  <entry stem="return" pos="V">
    <member-of family="Travel"/>
    <word form="return" macros="@base"/>
    <word form="returning" macros="@ng"/>
    <word form="return" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="returns" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="return" macros="@pres @pl-agr"/>
    <word form="returned" macros="@past"/>
  </entry>

  <entry stem="take" pos="V">
    <member-of family="Taking-Time"/>
    <word form="take" macros="@base"/>
    <word form="taking" macros="@ng"/>
    <word form="take" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="takes" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="take" macros="@pres @pl-agr"/>
    <word form="took" macros="@past"/>
  </entry>

  <entry stem="travel" pos="V">
    <member-of family="Travel"/>
    <word form="travel" macros="@base"/>
    <word form="traveling" macros="@ng"/>
    <word form="travel" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="travels" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="travel" macros="@pres @pl-agr"/>
    <word form="traveled" macros="@past"/>
  </entry>

  <entry stem="want" pos="V">
    <member-of family="Wanting"/>
    <word form="want" macros="@base"/>
    <word form="wanting" macros="@ng"/>
    <word form="want" macros="@pres @sg-agr @non-3rd-agr"/>
    <word form="wants" macros="@pres @sg-or-mass-agr @3rd-agr"/>
    <word form="want" macros="@pres @pl-agr"/>
    <word form="wanted" macros="@past"/>
  </entry>

  <!-- Adjectives -->
  <!-- NB: 'just', 'only', 'other' far too simple (and excl. of Predicative entry a bit of a hack) -->
  <entry stem="afternoon" pos="Adj" class="timeframe">
    <member-of family="Adj-TimeFrame"/>
  </entry>
  <entry stem="available" pos="Adj">
    <member-of family="Adjective"/>
  </entry>
  <!-- 
    other price possibilities: 
      affordable.a, cheap.a, costly.a, exorbitant.a, expensive.a, free.a, inexpensive.a, 
      low-cost.a, low-priced.a, overpriced.a, pricey.a 
  -->
  <entry stem="better" pos="Adj" class="scalar">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="best" pos="Adj" class="scalar">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="cheap" pos="Adj" class="scalar">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="cheaper" pos="Adj" class="scalar">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="cheapest" pos="Adj" class="scalar">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="connecting" pos="Adj">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="departure" pos="Adj">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="destination" pos="Adj">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="direct" pos="Adj">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="early" pos="Adj" class="scalar">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="earlier" pos="Adj" class="scalar">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="earliest" pos="Adj" class="scalar">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="even" pos="Adj">
    <member-of family="Adjective"/>
    <member-of family="Adjective-Measure"/>
    <word form="even" excluded="Predicative"/>
  </entry>
  <entry stem="evening" pos="Adj" class="timeframe">
    <member-of family="Adj-TimeFrame"/>
  </entry>
  <entry stem="first" pos="Adj">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="full" pos="Adj">
    <member-of family="Adj-Full"/>
  </entry>
  <entry stem="good" pos="Adj" class="scalar">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="great" pos="Adj" class="scalar">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="just" pos="Adj">
    <member-of family="Adjective"/>
    <member-of family="Adjective-Measure"/>
    <word form="just" excluded="Predicative"/>
  </entry>
  <entry stem="last" pos="Adj">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="late" pos="Adj" class="scalar">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="later" pos="Adj" class="scalar">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="latest" pos="Adj" class="scalar">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="morning" pos="Adj" class="timeframe">
    <member-of family="Adj-TimeFrame"/>
  </entry>
  <entry stem="next" pos="Adj">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="nonstop" pos="Adj">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="only" pos="Adj">
    <member-of family="Adjective"/>
    <member-of family="Adjective-Measure"/>
    <word form="only" excluded="Predicative"/>
  </entry>
  <entry stem="other" pos="Adj">
    <member-of family="Adjective"/>
    <word form="other" excluded="Predicative"/>
  </entry>
  <entry stem="total" pos="Adj">
    <member-of family="Adjective"/>
  </entry>
  <entry stem="willing" pos="Adj">
    <member-of family="Adj-Willing"/>
  </entry>
  
  <!-- Nouns -->
  <entry stem="afternoon" pos="N" class="timeframe">
    <word form="afternoon" macros="@sg"/>
    <word form="afternoons" macros="@pl"/>
  </entry>
  <entry stem="airline" pos="N" class="airline">
    <word form="airline" macros="@sg"/>
    <word form="airlines" macros="@pl"/>
  </entry>
  <entry stem="availability" pos="N" class="abstraction" macros="@mass">
    <member-of family="Noun-Category"/>
  </entry>
  <entry stem="cheapest" pos="N" class="scalar"/>
  <entry stem="city" pos="N" class="city">
    <word form="city" macros="@sg"/>
    <word form="cities" macros="@pl"/>
  </entry>
  <entry stem="connection" pos="N" class="action">
    <member-of family="Noun-Path"/>
    <word form="connection" macros="@sg"/>
    <word form="connections" macros="@pl"/>
  </entry>
  <entry stem="day" pos="N" class="date">
    <word form="day" macros="@sg"/>
    <word form="days" macros="@pl"/>
  </entry>
  <entry stem="date" pos="N" class="date">
    <word form="date" macros="@sg"/>
    <word form="dates" macros="@pl"/>
  </entry>
  <entry stem="destination" pos="N" class="location">
    <word form="destination" macros="@sg"/>
    <word form="destinations" macros="@pl"/>
  </entry>
  <entry stem="earliest" pos="N" class="scalar"/>
  <entry stem="evening" pos="N" class="timeframe">
    <word form="evening" macros="@sg"/>
    <word form="evenings" macros="@pl"/>
  </entry>
  <entry stem="flight" pos="N" class="phys-obj">
    <member-of family="Noun-Travel"/>
    <word form="flight" macros="@sg"/>
    <word form="flights" macros="@pl"/>
  </entry>
  <entry stem="latest" pos="N" class="scalar"/>
  <entry stem="month" pos="N" class="date">
    <word form="month" macros="@sg"/>
    <word form="months" macros="@pl"/>
  </entry>
  <entry stem="morning" pos="N" class="timeframe">
    <word form="morning" macros="@sg"/>
    <word form="mornings" macros="@pl"/>
  </entry>
  <entry stem="nonstop" pos="N" class="abstraction"/>
  <entry stem="pound" pos="N" class="abstraction">
    <word form="pound" macros="@sg"/>
    <word form="pounds" macros="@pl"/>
  </entry>
  <entry stem="option" pos="N" class="abstraction">
    <word form="option" macros="@sg"/>
    <word form="options" macros="@pl"/>
  </entry>
  <entry stem="price" pos="N" class="abstraction">
    <word form="price" macros="@sg"/>
    <word form="prices" macros="@pl"/>
  </entry>
  <entry stem="requirement" pos="N" class="mental-obj">
    <word form="requirement" macros="@sg"/>
    <word form="requirements" macros="@pl"/>
  </entry>
  <entry stem="seat" pos="N" class="phys-obj">
    <word form="seat" macros="@sg"/>
    <word form="seats" macros="@pl"/>
    <member-of family="Noun-Category"/>
  </entry>
  <entry stem="stopover" pos="N" class="action">
    <member-of family="Noun-Path"/>
    <word form="stopover" macros="@sg"/>
    <word form="stopovers" macros="@pl"/>
  </entry>
  <entry stem="ticket" pos="N" class="mental-obj">
    <word form="ticket" macros="@sg"/>
    <word form="tickets" macros="@pl"/>
  </entry>
  <entry stem="time" pos="N" class="time">
    <word form="time" macros="@sg"/>
    <word form="times" macros="@pl"/>
  </entry>
  <entry stem="travel_time" pos="N" class="abstraction">
    <member-of family="Noun-Duration"/>
    <word form="travel_time" macros="@sg"/>
    <word form="travel_times" macros="@pl"/>
  </entry>

  <!-- named times -->
  <entry stem="noon" pos="NNP" class="time"/>
  <entry stem="midnight" pos="NNP" class="time"/>
  
  <!-- Proper Names (with some also as adjectives, for now) -->
  <entry stem="Bob" pos="NNP" class="person" macros="@sg-2"/>
  <entry stem="Gil" pos="NNP" class="person" macros="@sg-2"/>
  <entry stem="Ted" pos="NNP" class="person" macros="@sg-2"/>

  <entry stem="business" pos="NNP" class="fareclass" macros="@sg-2"/>
  <entry stem="business_class" pos="NNP" class="fareclass" macros="@sg-2"/>
  <entry stem="coach" pos="NNP" class="fareclass" macros="@sg-2"/>
  <entry stem="economy" pos="NNP" class="fareclass" macros="@sg-2"/>
  <entry stem="first" pos="NNP" class="fareclass" macros="@sg-2"/>
  <entry stem="first_class" pos="NNP" class="fareclass" macros="@sg-2"/>
  
  <entry stem="Air_France" pos="NNP" class="airline" macros="@sg-2"/>
  <entry stem="Air_France" pos="Adj" class="airline">
    <member-of family="Adj-Airline"/>
  </entry>
  <entry stem="American" pos="NNP" class="airline" macros="@sg-2"/>
  <entry stem="American" pos="Adj" class="airline">
    <member-of family="Adj-Airline"/>
  </entry>
  <entry stem="BMI" pos="NNP" class="airline" macros="@sg-2"/>
  <entry stem="BMI" pos="Adj" class="airline">
    <member-of family="Adj-Airline"/>
  </entry>
  <entry stem="British_Airways" pos="NNP" class="airline" macros="@sg-2"/>
  <entry stem="British_Airways" pos="Adj" class="airline">
    <member-of family="Adj-Airline"/>
  </entry>
  <entry stem="Delta" pos="NNP" class="airline" macros="@sg-2"/>
  <entry stem="Delta" pos="Adj" class="airline">
    <member-of family="Adj-Airline"/>
  </entry>
  <entry stem="Easyjet" pos="NNP" class="airline" macros="@sg-2"/>
  <entry stem="Easyjet" pos="Adj" class="airline">
    <member-of family="Adj-Airline"/>
  </entry>
  <entry stem="Flybe" pos="NNP" class="airline" macros="@sg-2"/>
  <entry stem="Flybe" pos="Adj" class="airline">
    <member-of family="Adj-Airline"/>
  </entry>
  <entry stem="KLM" pos="NNP" class="airline" macros="@sg-2"/>
  <entry stem="KLM" pos="Adj" class="airline">
    <member-of family="Adj-Airline"/>
  </entry>
  <entry stem="Lufthansa" pos="NNP" class="airline" macros="@sg-2"/>
  <entry stem="Lufthansa" pos="Adj" class="airline">
    <member-of family="Adj-Airline"/>
  </entry>
  <entry stem="SN_Brussels_Airlines" pos="NNP" class="airline" macros="@sg-2"/>
  <entry stem="SN_Brussels_Airlines" pos="Adj" class="airline">
    <member-of family="Adj-Airline"/>
  </entry>
  <entry stem="Ryanair" pos="NNP" class="airline" macros="@sg-2"/>
  <entry stem="Ryanair" pos="Adj" class="airline">
    <member-of family="Adj-Airline"/>
  </entry>
  <entry stem="Scot_Airways" pos="NNP" class="airline" macros="@sg-2"/>
  <entry stem="Scot_Airways" pos="Adj" class="airline">
    <member-of family="Adj-Airline"/>
  </entry>
  <entry stem="United" pos="NNP" class="airline" macros="@sg-2"/>
  <entry stem="United" pos="Adj" class="airline">
    <member-of family="Adj-Airline"/>
  </entry>
  <entry stem="VLM" pos="NNP" class="airline" macros="@sg-2"/>
  <entry stem="VLM" pos="Adj" class="airline">
    <member-of family="Adj-Airline"/>
  </entry>

  <entry stem="Amsterdam_Schiphol" pos="NNP" class="airport" macros="@sg-2"/>
  <entry stem="Boston_Logan" pos="NNP" class="airport" macros="@sg-2"/>
  <entry stem="Helsinki_airport" pos="NNP" class="airport" macros="@sg-2"/>
  <entry stem="London_City_Airport" pos="NNP" class="airport" macros="@sg-2"/>
  <entry stem="London_Gatwick" pos="NNP" class="airport" macros="@sg-2"/>
  <entry stem="London_Heathrow" pos="NNP" class="airport" macros="@sg-2"/>
  <entry stem="Heathrow" pos="NNP" class="airport" macros="@sg-2"/>
  <entry stem="San_Francisco_International" pos="NNP" class="airport" macros="@sg-2"/>

  <entry stem="Aalborg" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Amsterdam" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Barcelona" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Berlin" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Birmingham" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Boston" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Bristol" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Brussels" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Dublin" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Edinburgh" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Frankfurt" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Helsinki" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="London" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Manchester" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Madrid" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Milan" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="Paris" pos="NNP" class="city" macros="@sg-2"/>
  <entry stem="San_Francisco" pos="NNP" class="city" macros="@sg-2"/>


  <!-- Add core macros -->
  <xsl:call-template name="add-macros"/>
  
  </dictionary>
  </xsl:template>
</xsl:transform>

