///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004-5 University of Edinburgh (Michael White)
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

package opennlp.ccg.lexicon;

import java.util.*;

/**
 * The Tokenizer interface provides a way to customize tokenization 
 * and handling of special tokens.
 * A custom tokenizer may be specified in the grammar file.
 * DefaultTokenizer provides a default implementation, which can also 
 * be subclassed for custom behavior.
 *
 * @author      Michael White
 * @version     $Revision: 1.14 $, $Date: 2005/10/20 17:30:30 $
 **/

public interface Tokenizer {

    /** Name used to tokenize word form attribute (usually left implicit). */
    public static final String WORD_ATTR = "W";
    
    /** Name used to tokenize stem attribute. */
    public static final String STEM_ATTR = "S";
    
    /** Name used to tokenize part-of-speech attribute. */
    public static final String POS_ATTR = "P";
    
    /** Name used to tokenize supertag attribute. */
    public static final String SUPERTAG_ATTR = "T";
    
    /** Name used to tokenize semantic class attribute. */
    public static final String SEM_CLASS_ATTR = "C";
    
    /** Name used to tokenize pitch accent attribute. */
    public static final String PITCH_ACCENT_ATTR = "A";
    
    
    /** Constant used to signal the substitution of a date as the pred. */
    public static final String DATE_VAL = "[*DATE*]";
    
    /** Constant used to represent the semantic class date. */
    public static final String DATE_CLASS = "date";
    
    /** Constant used to signal the substitution of a time as the pred. */
    public static final String TIME_VAL = "[*TIME*]";
    
    /** Constant used to represent the semantic class time. */
    public static final String TIME_CLASS = "time";
    
    /** Constant used to signal the substitution of a number as the pred. */
    public static final String NUM_VAL = "[*NUM*]";
    
    /** Constant used to represent the semantic class number. */
    public static final String NUM_CLASS = "num";
    
    /** Constant used to signal the substitution of an amount as the pred. */
    public static final String AMT_VAL = "[*AMT*]";
    
    /** Constant used to represent the semantic class amount. */
    public static final String AMT_CLASS = "amt";
    
    /** Constant used to signal the substitution of a duration as the pred. */
    public static final String DUR_VAL = "[*DUR*]";
    
    /** Constant used to represent the semantic class duration. */
    public static final String DUR_CLASS = "dur";
    
    /** 
     * Constant used to signal the substitution of a named entity 
     * (not listed in lexicon) as the pred. 
     */
    public static final String NE_VAL = "[*NE*]";
    
    /** Constant used to represent the semantic class (other) named entity. */
    public static final String NE_CLASS = "ne";
    

    /**
     * Parses an input string into a list of words, 
     * including any explicitly given factors, 
     * and the semantic class of special tokens.
     * Tokens are parsed into words using parseToken.
     */
    public List<Word> tokenize(String s);

    /**
     * Parses an input string into a list of words, 
     * including any explicitly given factors, 
     * and the semantic class of special tokens.
     * Tokens are parsed into words using parseToken, according to the given 
     * flag for whether to parse factors strictly.
     */
    public List<Word> tokenize(String s, boolean strictFactors);
    
    /** 
     * Parses a token into a word, including any explicitly given factors 
     * and the semantic class of special tokens.
     */
    public Word parseToken(String token);
 
    /** 
     * Parses a token into a word, including any explicitly given factors 
     * and the semantic class of special tokens, according to the given 
     * flag for whether to parse factors strictly.
     */
    public Word parseToken(String token, boolean strictFactors);
 
 
    /**
     * Returns a string (eg Tokenizer.DATE_CLASS) indicating the semantic class  
     * of special token, if the given token is recognized as a special 
     * token; otherwise returns null. 
     */
    public String isSpecialToken(String token);
    
    /**
     * Returns the special token constant for the given special token class, 
     * or null if none.
     */
    public String getSpecialTokenConstant(String semClass);
    
    /** 
     * Returns true iff the given string is a special token constant 
     * (eg Tokenizer.DATE_VAL).
     */
    public boolean isSpecialTokenConstant(String s);
    
    
    /** Returns true iff the token is recognized as a date. */
    public boolean isDate(String token);
    
    /** Returns true iff the token is recognized as a time. */
    public boolean isTime(String token);
    
    /** Returns true iff the token is recognized as a number. */
    public boolean isNum(String token);
    
    /** Returns true iff the token is recognized as an amount. */
    public boolean isAmt(String token);
    
    /** 
     * Returns true iff the token is recognized as a named entity (not listed in lexicon). 
     */
    public boolean isNamedEntity(String token);
    
    
    /**
     * Adds a semantic class to replace words with for language models.
     */
    public void addReplacementSemClass(String semClass);
    
    /** 
     * Returns whether the given semantic class is one to replace words with for language models.
     * The sem class is assumed to have been interned.
     */
    public boolean isReplacementSemClass(String semClass);

    
    /**
     * Returns a string for the given list of words.
     */
    public String getOrthography(List<Word> words);
    
    /**
     * Returns a string for the given list of words, optionally with semantic class replacement.
     */
    public String getOrthography(List<Word> words, boolean semClassReplacement);
    
    /**
     * Returns a string for the given word, optionally with semantic class replacement.
     */
    public String getOrthography(Word w, boolean semClassReplacement);
    
    /**
     * Returns a string for the given list of words, 
     * in the format expected by the SRILM tool for factored language models.
     */
    public String format(List<Word> words);
    
    /**
     * Returns a string for the given word, 
     * in the format expected by the SRILM tool for factored language models.
     */
    public String format(Word word);
    
    /**
     * Returns a string for the given list of words, 
     * in the format expected by the SRILM tool for factored language models, 
     * optionally with semantic class replacement.
     */
    public String format(List<Word> words, boolean semClassReplacement);
    
    /**
     * Returns a string for the given word, 
     * in the format expected by the SRILM tool for factored language models, 
     * optionally with semantic class replacement.
     */
    public String format(Word word, boolean semClassReplacement);

    /**
     * Returns one or more orthographic words for the given word.
     * This method is called from within Sign.getWordsInXml as 
     * part of producing the textual output of realization.
     */
    public List<String> expandWord(Word word);
    
    /**
     * Returns one or more orthographic words for the given date token.
     */
    public List<String> expandDate(String date);
    
    /**
     * Returns one or more orthographic words for the given time token.
     */
    public List<String> expandTime(String time);
    
    /**
     * Returns one or more orthographic words for the given number token.
     */
    public List<String> expandNum(String num);
    
    /**
     * Returns one or more orthographic words for the given amount token.
     */
    public List<String> expandAmt(String amt);
    
    /**
     * Returns one or more orthographic words for the given named entity token.
     */
    public List<String> expandNamedEntity(String namedEntity);
}

