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

import opennlp.ccg.grammar.Grammar;
import opennlp.ccg.util.*;

import java.text.*;
import java.util.*;
import javax.xml.datatype.*;
import gnu.trove.*;

/**
 * DefaultTokenizer provides a default implementation of the 
 * Tokenizer interface.
 *
 * @author      Michael White
 * @version     $Revision: 1.32 $, $Date: 2010/12/09 04:58:12 $
 **/

public class DefaultTokenizer implements Tokenizer {

    // date format with pattern yyyy.MM.dd, strict parsing
    private DateFormat dateFormat = null;

    // date format with pattern *.MM.dd, strict parsing
    private DateFormat dateFormatNoYear = null;

    // time format with pattern HH:mm, strict parsing
    private DateFormat timeFormat = null;
    
    // factory for parsing durations, in format "PnYnMnDTnHnMnS", as defined in XML Schema 1.0 section 3.2.6.1
    private DatatypeFactory datatypeFactory = null;
    
    /**
     * Map from special token semantic classes to special token constants. 
     * The map is initialized in the constructor, where 
     * the standard constants (eg Tokenizer.DATE_CLASS and Tokenizer.DATE_VAL) are added.
     */
    protected Map<String, String> specialTokenMap = null;
    
    /** 
     * A set containing semantic classes to replace words with for language models.
     * Equality is checked with identity, for use with interned strings.
     */
    @SuppressWarnings("unchecked")
	protected Set<String> replacementSemClasses = new THashSet(new TObjectIdentityHashingStrategy());


    /**
     * Constructor.
     */
    public DefaultTokenizer() {
        // init date, time formats
        dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH);
        dateFormat.setLenient(false);
        dateFormatNoYear = new SimpleDateFormat("*.MM.dd", Locale.ENGLISH);
        dateFormatNoYear.setLenient(false);
        timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        timeFormat.setLenient(false);
        // init data type factory
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException exc) {
            throw (RuntimeException) new RuntimeException().initCause(exc);
        }
        // init special token map
        specialTokenMap = new HashMap<String, String>();
        specialTokenMap.put(Tokenizer.DATE_CLASS, Tokenizer.DATE_VAL);
        specialTokenMap.put(Tokenizer.TIME_CLASS, Tokenizer.TIME_VAL);
        specialTokenMap.put(Tokenizer.NUM_CLASS, Tokenizer.NUM_VAL);
        specialTokenMap.put(Tokenizer.AMT_CLASS, Tokenizer.AMT_VAL);
        specialTokenMap.put(Tokenizer.DUR_CLASS, Tokenizer.DUR_VAL);
        specialTokenMap.put(Tokenizer.NE_CLASS, Tokenizer.NE_VAL);
    }
    
    
    /**
     * Adds a semantic class to replace words with for language models.
     */
    public void addReplacementSemClass(String semClass) {
        replacementSemClasses.add(semClass.intern());
    }
    
    /** 
     * Returns whether the given semantic class is one to replace words with for language models.
     * The sem class is assumed to have been interned.
     */
    public boolean isReplacementSemClass(String semClass) {
        return replacementSemClasses.contains(semClass);
    }

    
    /**
     * Parses an input string into a list of words, 
     * including any explicitly given factors, 
     * and the semantic class of special tokens.
     * Tokens are parsed into words using parseToken with the strictFactors
     * flag set to false.
     */
    public List<Word> tokenize(String s) { return tokenize(s, false); }

    /**
     * Parses an input string into a list of words, 
     * including any explicitly given factors, 
     * and the semantic class of special tokens.
     * Tokens are parsed into words using parseToken, according to the given 
     * flag for whether to parse factors strictly.
     * The string is assumed to have white-space delimited tokens.
     */
    public List<Word> tokenize(String s, boolean strictFactors) {
        List<Word> retval = new ArrayList<Word>();
        StringTokenizer st = new StringTokenizer(s);
        while (st.hasMoreTokens()) { retval.add(parseToken(st.nextToken(), strictFactors)); }
        return retval;
    }

    
    /** 
     * Parses a token into a word, including any explicitly given factors 
     * and the semantic class of special tokens.
     * Parsing is performed using parseToken with the strictFactors
     * flag set to false.
     */
    public Word parseToken(String token) { return parseToken(token, false); }
    
    /** 
     * Parses a token into a word, including any explicitly given factors 
     * and the semantic class of special tokens, according to the given 
     * flag for whether to parse factors strictly.
     * Recognized pitch accents may be appended to the word form with an underscore.
     * If the strictFactors flag is true, then colons are always assumed to 
     * separate attribute-value pairs, and hyphens are always assumed to 
     * separate attributes from values, and thus colons or hyphens not used as separators 
     * must be escaped. 
     * If the strictFactors flag is false, then there must be at least one colon 
     * and at least one hyphen in the token to trigger parsing of factors, 
     * in which case colons or hyphens not used as separators 
     * must again be escaped; otherwise, colons or hyphens on their own may 
     * appear without escaping in the word form.
     * After splitting the token into factors, it is unescaped.
     */
    public Word parseToken(String token, boolean strictFactors) {
        // init
        String form = token;
        String pitchAccent = null;
        List<Pair<String, String>> attrValPairs = null;
        String stem = null;
        String POS = null;
        String supertag = null;
        String semClass = null;
        // handle colon-separated attr-val pairs
        int colonPos = token.indexOf(':');
        int hyphenPos = token.indexOf('-');
        if (strictFactors || (colonPos > 0 && hyphenPos > 0)) {
            // deal with special cases before first colon, if any
            String suffix;
            if (colonPos > 0 && hyphenPos > colonPos) {
                form = token.substring(0, colonPos);
                suffix = token.substring(colonPos+1);
            }
            else if (colonPos < 0 && hyphenPos < 0) {
                form = token;
                suffix = null;
            }
            else {
                form = null;
                suffix = token;
            }
            while (suffix != null) {
                hyphenPos = suffix.indexOf('-');
                String attr = suffix.substring(0, hyphenPos);
                String val =  suffix.substring(hyphenPos+1);
                colonPos = suffix.indexOf(':');
                if (colonPos > 0) {
                    val = suffix.substring(hyphenPos+1, colonPos);
                    suffix = suffix.substring(colonPos+1);
                }
                else suffix = null;
                attr = unescape(attr); val = unescape(val);
                if (attr.equals(Tokenizer.WORD_ATTR)) { form = val; continue; }
                if (attr.equals(Tokenizer.STEM_ATTR)) { stem = val; continue; }
                if (attr.equals(Tokenizer.POS_ATTR)) { POS = val; continue; }
                if (attr.equals(Tokenizer.SUPERTAG_ATTR)) { supertag = val; continue; }
                if (attr.equals(Tokenizer.SEM_CLASS_ATTR)) { semClass = val; continue; }
                if (attr.equals(Tokenizer.PITCH_ACCENT_ATTR)) { pitchAccent = val; continue; }
                if (attrValPairs == null) attrValPairs = new ArrayList<Pair<String,String>>(5);
                attrValPairs.add(new Pair<String,String>(attr, val));
            }
        }
        // check for pitch accent preceded by an underscore
        int pos = (form != null) ? form.lastIndexOf("_") : -1;
        if (pos > 0) {
            String suffix = form.substring(pos+1);
            if (Grammar.isPitchAccent(suffix)) {
                pitchAccent = suffix;
                form = form.substring(0, pos);
            }
        }
        // unescape form (unless it happens to be "null")
        if (!"null".equals(form)) form = unescape(form);
        // check for special token
        String specialTokenClass = isSpecialToken(form);
        if (specialTokenClass != null) semClass = specialTokenClass;
        // done
        return Word.createWord(form,pitchAccent,attrValPairs,stem,POS,supertag,semClass);
    }
    
    
    /**
     * Returns a string (eg Tokenizer.DATE_CLASS) indicating the semantic class  
     * of special token, if the given token is recognized as a special 
     * token; otherwise returns null. 
     */
    public String isSpecialToken(String token) {
        if (token == null) return null;
        if (isDate(token)) return Tokenizer.DATE_CLASS;
        if (isTime(token)) return Tokenizer.TIME_CLASS;
        if (isNum(token)) return Tokenizer.NUM_CLASS;
        if (isAmt(token)) return Tokenizer.AMT_CLASS;
        if (isDur(token)) return Tokenizer.DUR_CLASS;
        if (isNamedEntity(token)) return Tokenizer.NE_CLASS;
        return null;
    }
    
    /**
     * Returns the special token constant for the given special token class, 
     * or null if none.
     */
    public String getSpecialTokenConstant(String semClass) {
        if (semClass == null) return null;
        return specialTokenMap.get(semClass);
    }
    
    /** 
     * Returns true iff the given string is a special token constant 
     * (eg Tokenizer.DATE_VAL).
     */
    public boolean isSpecialTokenConstant(String s) {
        return specialTokenMap.containsValue(s);
    }
    
    
    /** 
     * Returns true iff the token is recognized as a date.
     * The default implementation recognizes dates in the 
     * format yyyy.MM.dd, e.g. "2004.05.07", or *.MM.dd, e.g. "*.05.07", 
     * which is taken to mean the 5th of May (in the contextually 
     * appropriate year).  Note that by including the "*." prefix, 
     * the format avoids being ambiguous between a date or number; 
     * that is, with this format, something like "10.01" is 
     * unambiguously a number, whereas "*.10.01" means the 1st of 
     * October.
     */
    public boolean isDate(String token) {
        ParsePosition pos = new ParsePosition(0);
        Date date = dateFormat.parse(token, pos);
        if (date != null && pos.getIndex() == token.length()) return true;
        pos = new ParsePosition(0);
        date = dateFormatNoYear.parse(token, pos);
        return (date != null && pos.getIndex() == token.length());
    }
    
    /** 
     * Returns true iff the token is recognized as a time. 
     * The default implementation recognizes times in the 
     * 24-hour format HH:mm, e.g. "00:12" or "15:03".
     */
    public boolean isTime(String token) {
        ParsePosition pos = new ParsePosition(0);
        Date time = timeFormat.parse(token, pos);
        return (time != null && pos.getIndex() == token.length());
    }
    
    /** 
     * Returns true iff the token is recognized as a number.
     * The default implementation returns true if the token 
     * can be parsed as an integer or double, though not one 
     * in scientific notation.
     */
    public boolean isNum(String token) {
        try {
            Integer.parseInt(token);
            return true; 
        }
        catch (NumberFormatException exc) { 
            try {
                Double.parseDouble(token);
                if (token.indexOf('E') != -1) return false;
                if (token.indexOf('e') != -1) return false;
                return true;
            }
            catch (NumberFormatException exc2) {
                return false; 
            }
        }
    }
    
    /** 
     * Returns true iff the token is recognized as an amount.
     * The default implementation only handles currency amounts.
     * The token is recognized as an amount if it begins with 
     * a number and ends with an ISO-4217 currency code.
     * (See http://www.xe.com/iso4217.htm.)
     */
    public boolean isAmt(String token) { 
        if (token.length() < 4) return false;
        String code = token.substring(token.length()-3);
        try { Currency.getInstance(code); }
        catch (IllegalArgumentException exc) { return false; }
        String num = token.substring(0,token.length()-3).trim();
        return isNum(num);
    }

    /**
     * Returns true iff the token is recognized as a duration.
     * The format is "PnYnMnDTnHnMnS", as defined in XML Schema 1.0 section 3.2.6.1.
     * For example, "PT5H30" is 5 hours and 30 minutes.
     */
    public boolean isDur(String token) {
        try { datatypeFactory.newDuration(token); return true; }
        catch (Exception exc) { return false; }
    }
    
    /** 
     * Returns true iff the token is recognized as a named entity (not listed in lexicon). 
     * The default implementation always returns false.
     */
    public boolean isNamedEntity(String token) {
        return false;
    }
    
    
    /**
     * Returns a string for the given list of words.
     * A space separates the string for each word, as determined by getOrthography(Word,false).
     */
    public String getOrthography(List<Word> words) {
        return getOrthography(words, false);
    }
    
    /**
     * Returns a string for the given list of words, optionally with semantic class replacement.
     * A space separates the string for each word, as determined by getOrthography(Word,semClassReplacement).
     */
    public String getOrthography(List<Word> words, boolean semClassReplacement) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < words.size(); i++) {
            Word w = (Word) words.get(i);
            sb.append(getOrthography(w, semClassReplacement));
            if (i < words.size()-1) sb.append(" ");
        }
        return sb.toString();
    }
    

    /**
     * Returns a string for the given word, optionally with semantic class replacement.
     * The default implementation returns the word's form - or semantic class, if apropos - 
     * followed by its pitch accent (if non-null) separated by an underscore, 
     * followed by any further attribute values, also separated by underscores.
     * With the semantic class replacement option, the word form is replaced with 
     * the semantic class, uppercased, if the class is listed as one to replace words with for 
     * language models.
     */
    public String getOrthography(Word w, boolean semClassReplacement) {
        StringBuffer sb = new StringBuffer();
        String semClass = w.getSemClass();
        if (semClassReplacement && semClass != null && replacementSemClasses.contains(semClass))
            sb.append(semClass.toUpperCase());
        else
            sb.append(w.getForm());
        if (w.getPitchAccent() != null)
            sb.append("_").append(w.getPitchAccent());
        for (Iterator<Pair<String,String>> it = w.getAttrValPairs(); it.hasNext(); ) {
            Pair<String,String> p = it.next();
            sb.append("_").append(p.b);
            
        }
        return sb.toString();
    }
    
    /**
     * Returns a string for the given list of words, 
     * in the format expected by the SRILM tool for factored language models.
     * A space separates the string for each word, determined by format(Word).
     */
    public String format(List<Word> words) { return format(words, false); }

    /**
     * Returns a string for the given list of words, 
     * in the format expected by the SRILM tool for factored language models, 
     * optionally with semantic class replacement.
     * A space separates the string for each word, determined by format(Word,boolean).
     */
    public String format(List<Word> words, boolean semClassReplacement) {
        StringBuffer sb = new StringBuffer();
        sb.append("<s> ");
        for (int i = 0; i < words.size(); i++) {
            Word w = words.get(i);
            if (w.getForm() == "<s>" || w.getForm() == "</s>") continue; // skip <s> or </s>
            sb.append(format(w, semClassReplacement));
            sb.append(" ");
        }
        sb.append("</s>");
        return sb.toString();
    }
    
    /**
     * Returns a string for the given word,  
     * in the format expected by the SRILM tool for factored language models.
     * All factors are escaped.
     */
    public String format(Word w) { return format(w, false); }
    
    /**
     * Returns a string for the given word,  
     * in the format expected by the SRILM tool for factored language models, 
     * optionally with semantic class replacement.
     * All factors are escaped.
     * With the semantic class replacement option, the word form and stem are replaced with 
     * the semantic class, uppercased, if the class is listed as one to replace words with for 
     * language models.
     */
    public String format(Word w, boolean semClassReplacement) {
        StringBuffer sb = new StringBuffer();
        String form = w.getForm();
        String pitchAccent = w.getPitchAccent();
        String stem = w.getStem();
        String POS = w.getPOS();
        String supertag = w.getSupertag();
        String semClass = w.getSemClass();
        if (semClassReplacement && semClass != null && replacementSemClasses.contains(semClass)) {
            form = escape(semClass.toUpperCase()); stem = form; 
        }
        sb.append(escape(form));
        if (pitchAccent != null) sb.append(":").append(Tokenizer.PITCH_ACCENT_ATTR).append("-").append(escape(pitchAccent));
        for (Iterator<Pair<String,String>> it = w.getAttrValPairs(); it.hasNext(); ) {
            Pair<String,String> p = it.next();
            String attr = p.a; String val = p.b;
            if (val != null) sb.append(":").append(escape(attr)).append("-").append(escape(val));
        }
        if (stem != null) sb.append(":").append(Tokenizer.STEM_ATTR).append("-").append(escape(stem));
        if (POS != null) sb.append(":").append(Tokenizer.POS_ATTR).append("-").append(escape(POS));
        if (supertag != null) sb.append(":").append(Tokenizer.SUPERTAG_ATTR).append("-").append(escape(supertag));
        if (semClass != null) sb.append(":").append(Tokenizer.SEM_CLASS_ATTR).append("-").append(escape(semClass));
        return sb.toString();
    }

    
    /**
     * Returns an encoding of the given string where 
     * the characters for ampersand, less-than, greater-than, 
     * apostrophe, quote, colon and hyphen are escaped 
     * using HTML conventions.
     * Null is returned for the null string.
     * An initial substring 'null' is doubled.
     */
    public static String escape(String s) {
        if (s == null) return null;
        StringBuffer output = null; // instantiate only if needed
        if (s.startsWith("null")) {
            output = new StringBuffer();
            output.append("null");
        }
        for(int i=0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (output == null && (c=='<' || c=='>' || c=='&' || c=='\'' || c=='"' || c==':' || c=='-')) {
                output = new StringBuffer();
                output.append(s.substring(0,i));
            }
            if (output != null) {
                switch(c) {
                    case '<': output.append("&lt;"); break;
                    case '>': output.append("&gt;"); break;
                    case '&': output.append("&amp;"); break;
                    case '\'': output.append("&apos;"); break;
                    case '"': output.append("&quot;"); break;
                    case ':': output.append("&#").append((int)':').append(";"); break;
                    case '-': output.append("&#").append((int)'-').append(";"); break;
                    default: output.append(c);
                }
            }
        }
        return (output != null) ? output.toString() : s;
    }
    
    /**
     * Returns a decoding of the given string where 
     * the characters for ampersand, less-than, greater-than, 
     * apostrophe, quote, colon and hyphen (and any other 
     * character whose code is given numerically) are unescaped 
     * using HTML conventions.
     * An exception is that ampersands may be left unescaped 
     * for convenience, when there is no following semicolon 
     * in the string.
     * Null is returned for the null string and for the string "null".
     * An initial substring 'nullnull' is halved.
     */
    public static String unescape(String s) {
        if (s == null || s.equals("null")) return null;
        StringBuffer output = null; // instantiate only if needed
        if (s.startsWith("nullnull")) {
        	s = s.substring(4);
            output = new StringBuffer();
        }
        for (int i=0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&') {
                int endPos = s.indexOf(";", i);
                if (endPos < 0) {
                	// allow unescaped ampersands
                	if (output != null) output.append(c);
                	continue;
                }
                if (output == null) {
                    output = new StringBuffer();
                    output.append(s.substring(0,i));
                }
                String escaped = s.substring(i+1, endPos);
                if (escaped.equals("lt")) { output.append('<'); i = endPos; continue; }
                if (escaped.equals("gt")) { output.append('>'); i = endPos; continue; }
                if (escaped.equals("amp")) { output.append('&'); i = endPos; continue; }
                if (escaped.equals("apos")) { output.append('\''); i = endPos; continue; }
                if (escaped.equals("quot")) { output.append('"'); i = endPos; continue; }
                if (s.charAt(i+1) == '#') {
                    escaped = s.substring(i+2, endPos);
                    output.append((char)Integer.parseInt(escaped)); i = endPos; continue;
                }
                throw new RuntimeException(
                    "Unable to unescape " + s.substring(i,endPos+1) + "at position " + i + " in: " + s
                );
            }
            else if (output != null) output.append(c);
        }
        return (output != null) ? output.toString() : s;
    }

    
    /**
     * Returns one or more orthographic words for the given word's form.
     * This method is called from within Sign.getWordsInXml as 
     * part of producing the textual output of realization.
     * The default implementation checks the semantic class 
     * for a special token class, and if true, returns the result 
     * of expandDate, expandTime, expandNum, expandAmt, or 
     * expandNamedEntity, as appropriate, after first checking that 
     * the corresponding isDate, ..., isNamedEntity method returns true.
     * Otherwise, it splits the word form using underscore as a delimiter.
     */
    public List<String> expandWord(Word word) {
        String token = word.getForm();
        String sc = word.getSemClass();
        if (sc == Tokenizer.DATE_CLASS && isDate(token)) return expandDate(token);
        if (sc == Tokenizer.TIME_CLASS && isTime(token)) return expandTime(token);
        if (sc == Tokenizer.NUM_CLASS && isNum(token)) return expandNum(token);
        if (sc == Tokenizer.AMT_CLASS && isAmt(token)) return expandAmt(token);
        if (sc == Tokenizer.DUR_CLASS && isDur(token)) return expandDur(token);
        if (sc == Tokenizer.NE_CLASS && isNamedEntity(token)) return expandNamedEntity(token);
        String[] words = token.split("_");
        return Arrays.asList(words);
    }
    
    /**
     * Returns one or more orthographic words for the given date token.
     * The default implementation expands the date with 
     * EnglishExpander.expandDate, using the long option if the year is 
     * present, and the medium option if not.
     */
    public List<String> expandDate(String date) {
        ArrayList<String> retval = new ArrayList<String>();
        try {
            ParsePosition pos = new ParsePosition(0);
            Date dateObj = dateFormat.parse(date, pos);
            if (dateObj != null && pos.getIndex() == date.length()) {
                EnglishExpander.expandDate(dateObj, DateFormat.LONG, retval);
            }
            else {
                dateObj = dateFormatNoYear.parse(date);
                EnglishExpander.expandDate(dateObj, DateFormat.MEDIUM, retval);
            }
        }
        // shouldn't happen if isDate called first
        catch (ParseException exc) {
            // just add date string as a fall-back option
            retval.add(date);
        }
        return retval;
    }
    
    /**
     * Returns one or more orthographic words for the given time token.
     * The default implementation expands the time using 
     * EnglishExpander.expandTime.
     */
    public List<String> expandTime(String time) {
        ArrayList<String> retval = new ArrayList<String>();
        try {
            EnglishExpander.expandTime(timeFormat.parse(time), retval);
        }
        // shouldn't happen if isTime called first
        catch (ParseException exc) {
            // just add time string as a fall-back option
            retval.add(time);
        }
        return retval;
    }
    
    /**
     * Returns one or more orthographic words for the given number token.
     * The default implementation expands the number using 
     * EnglishExpander.expandNumber.
     */
    public List<String> expandNum(String num) {
        ArrayList<String> retval = new ArrayList<String>();
        EnglishExpander.expandNumber(num, retval);
        return retval;
    }
    
    /**
     * Returns one or more orthographic words for the given amount token.
     * The default implementation expands the number using 
     * EnglishExpander.expandAmount.
     */
    public List<String> expandAmt(String amt) {
        String code = amt.substring(amt.length()-3);
        String num = amt.substring(0,amt.length()-3).trim();
        ArrayList<String> retval = new ArrayList<String>();
        EnglishExpander.expandAmount(num, code, retval);
        return retval;
    }
    
    /**
     * Returns one or more orthographic words for the given duration token.
     * The default implementation expands the number using 
     * EnglishExpander.expandDuration.
     */
    public List<String> expandDur(String dur) {
        Duration duration = null;
        try {
            duration = datatypeFactory.newDuration(dur);
        } catch (Exception exc) {
            // parsing not expected to fail
            throw (RuntimeException) new RuntimeException().initCause(exc);
        }
        ArrayList<String> retval = new ArrayList<String>();
        EnglishExpander.expandDuration(duration, retval);
        return retval;
    }
    
    /**
     * Returns one or more orthographic words for the given named entity token.
     * The default implementation just splits the token using underscore as a delimiter.
     */
    public List<String> expandNamedEntity(String namedEntity) {
        String[] words = namedEntity.split("_");
        return Arrays.asList(words);
    }
    
    /** Test: tokenize args[0], expand each token; and optionally do parseToken(args[1],true). */
    public static void main(String[] args) {
        Tokenizer tk = new DefaultTokenizer();
        String s = args[0];
        List<Word> words = tk.tokenize(s);
        String expw = "";
        System.out.println("words: ");
        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);
            System.out.print(word + " ");
            List<String> orthWords = tk.expandWord(word);
            for (int j = 0; j < orthWords.size(); j++) {
                expw += orthWords.get(j) + " ";
            }
        }
        System.out.println();
        System.out.println("expanded: " + expw);
        System.out.println("formatted: " + tk.format(words));
        if (args.length > 1) {
            System.out.println();
            Word strictlyParsed = tk.parseToken(args[1], true);
            System.out.println("strictly parsed word: " + strictlyParsed);
            System.out.println("formatted: " + tk.format(strictlyParsed));
        }
    }
}

