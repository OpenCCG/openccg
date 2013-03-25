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
import java.text.*;
import javax.xml.datatype.*;

/**
 * EnglishExpander provides methods for expanding numbers, amounts, durations, 
 * dates and times as English words.
 * The class com.sun.speech.freetts.en.us.NumberExpander served 
 * as a reference point in part, but the implementation has been 
 * rewritten from scratch, streamlined and extended.
 *
 * @author      Michael White
 * @version     $Revision: 1.7 $, $Date: 2005/10/20 17:30:30 $
 **/
public class EnglishExpander {

    //--------------------------------------------
    // dates 
    //
    
    // formats date in month
    private static final DateFormat dateInMonthFormat = new SimpleDateFormat("dd", Locale.ENGLISH);

    // formats month in full
    private static final DateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);

    // formats year in full
    private static final DateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);
    
    /**
     * Expands a date to English words in short, 
     * medium or long forms, adding the words to 
     * the given list.  The style is given by 
     * DateFormat.SHORT (e.g., "the first"), 
     * DateFormat.MEDIUM (e.g., "the first of May"), and 
     * DateFormat.LONG (e.g., "the first of May, two thousand and four").
     */
    public static void expandDate(Date date, int style, List<String> list) {
        list.add("the");
        expandOrdinal(dateInMonthFormat.format(date), list);
        if (style == DateFormat.MEDIUM || style == DateFormat.LONG) {
            list.add("of");
            list.add(monthFormat.format(date));
        }
        if (style == DateFormat.LONG) {
            list.add(",");
            String year = yearFormat.format(date);
            // x00y
            if (year.charAt(1) == '0' && year.charAt(2) == '0') {
                expandNumber(year, list);
            }
            // xxyy
            else {
                expandNumber(year.substring(0,2), list);
                expandNumber(year.substring(2), list);
            }
        }
    }

    //--------------------------------------------
    // times
    //
    
    // formats hours in range 1-12
    private static final DateFormat hoursFormat = new SimpleDateFormat("hh", Locale.ENGLISH);

    // formats minutes
    private static final DateFormat minutesFormat = new SimpleDateFormat("mm", Locale.ENGLISH);

    // formats am/pm
    private static final DateFormat amPmFormat = new SimpleDateFormat("a", Locale.ENGLISH);

    /**
     * Expands a time to English words, 
     * adding the words to the given list.
     * For example, a date object with time set to 23:02 
     * is expanded to "eleven oh two PM".
     */
    public static void expandTime(Date time, List<String> list) {
        // add hours
        String hours = hoursFormat.format(time);
        expandNDigitNumber(hours, list);
        // add minutes, with special case for 'oh'
        String minutes = minutesFormat.format(time);
        if (minutes.charAt(0) == '0' && minutes.charAt(1) != '0') {
            list.add("oh");
            expandNDigitNumber(minutes.substring(1), list);
        }
        else {
            expandNDigitNumber(minutes, list);
        }
        // add AM or PM
        list.add(amPmFormat.format(time));
    }

    //--------------------------------------------
    // amounts
    //
    
    /**
     * Expands a digit string and currency code to 
     * number words and the currency name, which 
     * are added to the given list.
     * For example, "12.50" and "GBP" are expanded to 
     * "twelve pounds and fifty pence".
     * The codes GBP, USD and EUR are converted to 
     * names, while other codes are left as is.
     */
    public static void expandAmount(String digitString, String currencyCode, List<String> list) {
        // establish names for currency (singular and plural), 
        // and fractional parts
        String currSing = null; String currPlur = null;
        String fracSing = null; String fracPlur = null;
        if (currencyCode.equals("GBP")) {
            currSing = "pound"; currPlur = "pounds";
            fracSing = "penny"; fracPlur = "pence";
        }
        else if (currencyCode.equals("USD")) {
            currSing = "dollar"; currPlur = "dollars";
            fracSing = "cent"; fracPlur = "cents";
        }
        else if (currencyCode.equals("EUR")) {
            currSing = "euro"; currPlur = "euros";
            fracSing = "cent"; fracPlur = "cents";
        }
        // if none, just expand digit string and append code
        if (currSing == null) {
            expandNumber(digitString, list);
            list.add(currencyCode);
            return;
        }
        // otherwise, get whole and fractional parts of digit string
        String whole = digitString; String frac = null;
        int dotIndex = digitString.indexOf(".");
        if (dotIndex != -1) {
            whole = digitString.substring(0,dotIndex); 
            frac = digitString.substring(dotIndex+1);
        }
        // expand whole
        expandNumber(whole, list);
        // add currency name
        if (whole.equals("1")) 
            list.add(currSing);
        else 
            list.add(currPlur);
        // add fractional part, if any
        if (frac != null) {
            // add "and"
            list.add("and");
            // expand frac
            expandNDigitNumber(frac, list);
            // add fractional name
            if (frac.equals("01"))
                list.add(fracSing);
            else
                list.add(fracPlur);
        }
    }


    //--------------------------------------------
    // durations
    //
    
    /**
     * Expands a duration into a string of words.
     * NB: Fractions of seconds are ignored at present.
     */
    public static void expandDuration(Duration duration, List<String> list) {
        int[] durationFields = { 
            duration.getYears(), duration.getMonths(), duration.getDays(), 
            duration.getHours(), duration.getMinutes(), duration.getSeconds()
        };
        int counter = 0;
        for (int i = 0; i < durationFields.length; i++) {
            if (durationFields[i] > 0) counter++;
        }
        for (int i = 0; i < durationFields.length; i++) {
            if (durationFields[i] > 0) {
                counter--;
                String str = Integer.toString(durationFields[i]);
                expandNumber(str, list);
                String unit = durUnits[i];
                if (durationFields[i] != 1) unit += "s";
                list.add(unit);
                if (counter > 1) list.add(",");
                if (counter == 1) list.add("and");
            }
        }
    }
    
    // duration units
    private static String[] durUnits = { "year", "month", "day", "hour", "minute", "second" };
    
    
    //--------------------------------------------
    // numbers
    //
    
    /**
     * Expands a digit string to a sequence of digit words, 
     * which are added to the given list.
     * For example, "1234" is expanded to "one two three four".
     */
    public static void expandDigits(String digitString, List<String> list) {
        for (int i = 0; i < digitString.length(); i++) {
            list.add(zeroToNine[digitString.charAt(i)-'0']);
        }
    }
    
    /**
     * Expands a digit string to number words, 
     * which are added to the given list.
     * For example, "1234" is expanded to 
     * "one thousand two hundred and thirty four".
     * The digit string may contain a single dot in it, 
     * as well as an initial plus or minus character.
     * For example, "-100.011" is expanded to 
     * "minus one hundred point zero one one".
     * Scientific notation is not currently handled.
     */
    public static void expandNumber(String digitString, List<String> list) {
        // do nothing with empty strings
        if (digitString.length() == 0) return;
        // handle plus or minus
        char c0 = digitString.charAt(0);
        if (c0 == '+' || c0 == '-') {
            list.add((c0 == '+') ? "plus" : "minus");
            digitString = digitString.substring(1);
        }
        // check for dot
        int dotIndex = digitString.indexOf(".");
        if (dotIndex != -1) {
            // add numbers "point" digits
            expandNDigitNumber(digitString.substring(0,dotIndex), list);
            list.add("point");
            expandDigits(digitString.substring(dotIndex+1), list);
        }
        else {
            // add numbers
            expandNDigitNumber(digitString, list);
        }
    }
    
    /**
     * Expands a digit string to words for an ordinal number, 
     * which are added to the given list.
     * For example, "1234" is expanded to 
     * "one thousand two hundred and thirty fourth".
     */
    public static void expandOrdinal(String digitString, List<String> list) {
        // expand number
        expandNDigitNumber(digitString, list);
        // replace last one
        int lastPos = list.size() - 1;
        String ordinal = getOrdinal(list.get(lastPos));
        list.set(lastPos, ordinal);
    }

    // n-digit number    
    public static void expandNDigitNumber(String digitString, List<String> list) {
        int numDigits = digitString.length();
        if (numDigits == 2) 
            expand2DigitNumber(digitString, list); 
        else if (numDigits == 3) 
            expand3DigitNumber(digitString, list); 
        else if (numDigits >= 4 && numDigits <= 12) 
            expand4to12DigitNumber(digitString, list);
        else 
            expandDigits(digitString, list);
    }
    
    // 2 digit numbers
    private static void expand2DigitNumber(String digitString, List<String> list) {
        // 0x case
        if (digitString.charAt(0) == '0') {
            // do nothing for 00 case
            if (digitString.charAt(1) != '0')
                expandDigits(digitString.substring(1), list);
        }
        // 1x case
        else if (digitString.charAt(0) == '1') {
            list.add(tenToNineteen[digitString.charAt(1)-'0']);
        }
        // xy case, x >= 2
        else {
            list.add(zeroToNinety[digitString.charAt(0)-'0']);
            // do nothing for x0 case
            if (digitString.charAt(1) != '0')
                expandDigits(digitString.substring(1), list);
        }
    }

    // 3 digit numbers
    private static void expand3DigitNumber(String digitString, List<String> list) {
        // add hundreds if non-zero
        if (digitString.charAt(0) != '0') {
            // add hundreds digit
            expandDigits(digitString.substring(0,1), list);
            // add unit ("hundred")
            list.add("hundred");
        }
        // add "and", if final two digits non-zero
        if (digitString.charAt(1) != '0' || digitString.charAt(2) != '0')
            list.add("and");
        // expand final two digits
        expand2DigitNumber(digitString.substring(1), list);
    }
    
    // 4-12 digit numbers
    private static void expand4to12DigitNumber(String digitString, List<String> list) {
        int numDigitsModThree = digitString.length() % 3;
        int numInitialDigits = (numDigitsModThree != 0) ? numDigitsModThree : 3;
        int unitsIndex = ((digitString.length() - 1) / 3) - 1;
        // add initial digits
        expandNDigitNumber(digitString.substring(0, numInitialDigits), list);
        // add unit
        list.add(thousandToBillion[unitsIndex]);
        // add rest
        expandNDigitNumber(digitString.substring(numInitialDigits), list);
    }
    
    
    //------------------------
    // arrays of number words
    //
    
    private static final String[] zeroToNine = {
        "zero", "one", "two", "three", "four", 
        "five", "six", "seven", "eight", "nine" 
    };
    
    private static final String[] tenToNineteen = {
        "ten",  "eleven", "twelve", "thirteen", "fourteen", 
        "fifteen", "sixteen", "seventeen", "eighteen", "nineteen" 
    };
    
    private static final String[] zeroToNinety = {
        "zero",  "ten", "twenty", "thirty", "forty", 
        "fifty", "sixty", "seventy", "eighty", "ninety" 
    };
    
    private static final String[] thousandToBillion = {
        "thousand", "million", "billion"
    };
    
    private static final String[] zerothToNinth = {
        "zeroth", "first", "second", "third", "fourth", 
        "fifth", "sixth", "seventh", "eighth", "ninth" 
    };
    
    private static final String[] tenthToNineteenth = {
        "tenth",  "eleventh", "twelfth", "thirteenth", "fourteenth", 
        "fifteenth", "sixteenth", "seventeenth", "eighteenth", "nineteenth" 
    };
    
    private static final String[] zerothToNinetieth = {
        "zeroth", "tenth", "twentieth", "thirtieth", "fortieth", 
        "fiftieth", "sixtieth", "seventieth", "eightieth", "ninetieth" 
    };
    
    private static final String[] thousandthToBillionth = {
        "thousandth", "millionth", "billionth"
    };

    
    //--------------------------------------------
    // corresponding ordinals
    //
    
    // map from numbers to corresponding ordinals
    private static Map<String, String> ordinalMap = null;
    
    // returns corresponding ordinal
    private static String getOrdinal(String number) {
        // ensure ordinalMap instantiated
        if (ordinalMap == null) {
            ordinalMap = new HashMap<String, String>();
            for (int i = 0; i < zeroToNine.length; i++) {
                ordinalMap.put(zeroToNine[i], zerothToNinth[i]);
            }
            for (int i = 0; i < tenToNineteen.length; i++) {
                ordinalMap.put(tenToNineteen[i], tenthToNineteenth[i]);
            }
            for (int i = 0; i < zeroToNinety.length; i++) {
                ordinalMap.put(zeroToNinety[i], zerothToNinetieth[i]);
            }
            ordinalMap.put("hundred", "hundredth");
            for (int i = 0; i < thousandToBillion.length; i++) {
                ordinalMap.put(thousandToBillion[i], thousandthToBillionth[i]);
            }
        }
        return ordinalMap.get(number);
    }
}

