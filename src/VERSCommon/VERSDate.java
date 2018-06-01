/*
 * Copyright Public Record Office Victoria 2015
 * Licensed under the CC-BY license http://creativecommons.org/licenses/by/3.0/au/
 * Author Andrew Waugh
 * Version 1.0 February 2015
 */
package VERSCommon;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class contains utility objects generally used in VEOCreate and
 * VEOAnalysis.
 */
public class VERSDate {

    // default constructor (does nothing at the moment
    public VERSDate() {
    }

    /**
     * Returns a date and time in the standard VERS format (see PROS 99/007
     * (Version 2), Specification 2, p146.
     *
     * @param ms milliseconds since the epoch (if zero, return current date/time)
     * @return The date and time as a string
     */
    public static final String versDateTime(long ms) {
        Date d;
        SimpleDateFormat sdf;
        TimeZone tz;
        String s1;

        tz = TimeZone.getDefault();
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        sdf.setTimeZone(tz);
        if (ms == 0) {
            d = new Date();
        } else {
            d = new Date(ms);
        }
        s1 = sdf.format(d);
        return s1.substring(0, 22) + ":" + s1.substring(22, 24);
    }
    
        /**
     * Check a date/time value against a restricted subset of ISO8601. The
     * date/time pattern is:
     * yyyy['-'mm['-'dd['T'hh[':'mm[':'ss]]('Z'|('+'|'-')hh[':'mm])]]] Note that
     * the time 24:00 is correct - it is midnight and is equivalent to 00:00,
     * but you cannot use '24:01' etc.
     *
     * @param value the value to check
     */
    /*
    public static void testValueAsDate(String value) throws IllegalArgumentException {
        Instant i;
        
        try {
            i = Instant.parse(value);
        } catch (DateTimeParseException dtpe) {
            throw new IllegalArgumentException("Date/time '" + value + "' is invalid: " + dtpe.getMessage());
        }
    } 
    */

    public static void testValueAsDate(String value) throws IllegalArgumentException {
        int year, month, day, hour, min;
        char tz;

        // check year
        if (value.length() < 4) {
            dateFailed(value, 0, 4, "Year must match 'yyyy'");
        }
        if (!(Character.isDigit(value.charAt(0)))
                || !(Character.isDigit(value.charAt(1)))
                || !(Character.isDigit(value.charAt(2)))
                || !(Character.isDigit(value.charAt(3)))) {
            dateFailed(value, 0, 4, "Year must match 'yyyy'");
        }
        year = Character.digit(value.charAt(0), 10) * 1000 + Character.digit(value.charAt(1), 10) * 100 + Character.digit(value.charAt(2), 10) * 10 + Character.digit(value.charAt(3), 10);

        try {
            checkSep(value, 4, "-");
        } catch (IndexOutOfBoundsException e) {
            return;
        }
        month = checkNumber("Month", value, 5, 12);
        try {
            checkSep(value, 7, "-");
        } catch (IndexOutOfBoundsException e) {
            return;
        }
        day = checkNumber("Day", value, 8, 31);
        if (month == 2 && year % 4 != 0 && day > 28) {
            dateFailed(value, 8, 2, "day in February in a non leap year must be in the range '01' to '28'");
        }
        if (month == 2 && year % 4 == 0 && year % 1000 == 0 && day > 28) {
            dateFailed(value, 8, 2, "day in February in a non leap year must be in the range '01' to '28'");
        }
        if (month == 2 && year % 4 == 0 && year % 1000 != 0 && day > 29) {
            dateFailed(value, 8, 2, "day in February in a leap year must be in the range '01' to '29'");
        }
        if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
            dateFailed(value, 8, 2, "day in April, June, September or November must be in the range '01' to '31'");
        }
        try {
            checkSep(value, 10, "T");
        } catch (IndexOutOfBoundsException e) {
            return;
        }
        hour = checkNumber("Hours", value, 11, 24);
        try {
            tz = checkSep(value, 13, ":Z+-");
        } catch (IndexOutOfBoundsException e) {
            return;
        }
        if (tz == 'Z') {
            return;
        }
        if (tz == '+' || tz == '-') {
            checkTimeZoneOffset(value, 14);
            return;
        }
        min = checkNumber("Minutes", value, 14, 59);
        if (hour == 24 && min != 0) {
            dateFailed(value, 11, 5, "time is past midnight (i.e. >'24:00')");
        }
        try {
            tz = checkSep(value, 16, ":Z+-");
        } catch (IndexOutOfBoundsException e) {
            return;
        }
        if (tz == 'Z') {
            return;
        }
        if (tz == '+' || tz == '-') {
            checkTimeZoneOffset(value, 17);
            return;
        }
        checkNumber("Seconds", value, 17, 59);
        try {
            tz = checkSep(value, 19, "Z+-");
        } catch (IndexOutOfBoundsException e) {
            return;
        }
        if (tz == 'Z') {
            return;
        }
        checkTimeZoneOffset(value, 20);
    }

    /**
     * private function to validate a timezone offset: hh[':'mm])].
     * Note that timezones may range up from -12:00 to +14:00
     * @param value the value to check
     * @param i the index in the value where the timezone offset should start
     * @throws IllegalArgumentException if value does not contain a valid offset
     */
    private static void checkTimeZoneOffset(String value, int i)
            throws IllegalArgumentException {
        checkNumber("Timezone hours", value, i, 14);
        try {
            checkSep(value, i + 2, ":");
        } catch (IndexOutOfBoundsException e) {
            return;
        }
        checkNumber("Timezone minutes", value, i + 3, 59);
    }

    /**
     * private function to check that a character is a valid separator in this position.
     * @param value the value to check
     * @param i the index in the value to check for the separator
     * @param sep a string containing the valid separators for this position
     * @return the separator actually found
     * @throws IllegalArgumentException if the character at the index position is not a valid separator
     * @throws IndexOutOfBoundsException if the index position is beyond the end of the value
     */
    private static char checkSep(String value, int i, String sep)
            throws IllegalArgumentException, IndexOutOfBoundsException {
        if (sep.indexOf(value.charAt(i)) == -1) {
            if (sep.length() == 1) {
                dateFailed(value, i, 1, "separator must be '" + sep + "'");
            } else {
                dateFailed(value, i, 1, "separator must be one of '" + sep + "'");
            }
        }
        return value.charAt(i);
    }

    /**
     * private method to check a two digit number.
     * @param item the specific item being checked for (e.g. 'Months')
     * @param value the value being checked
     * @param i the index in the value of the first digit of the number
     * @param max the maximum permitted value of the number (e.g. 59)
     * @return the number actually found
     * @throws IllegalArgumentException if a valid two digit number wasn't found
     */
    private static int checkNumber(String item, String value, int i, int max) throws IllegalArgumentException {
        int v;

        v = 0;
        try {
            if (!(Character.isDigit(value.charAt(i))) || !(Character.isDigit(value.charAt(i + 1)))) {
                dateFailed(value, i, 2, item + " must be two digits");
            }
            v = Character.digit(value.charAt(i), 10) * 10 + Character.digit(value.charAt(i + 1), 10);
            if (v > max) {
                dateFailed(value, i, 2, item + " must be in the range '00' to '" + max + "'");
            }
        } catch (IndexOutOfBoundsException e) {
            dateFailed(value, i, 2, item + " expected to be two digits");
        }

        return v;
    }

    /**
     * Throw an IllegalArgumentException because the date is incorrect.
     * @param value The value being checked
     * @param i The index where the error was found
     * @param len The length of the element being looked for
     * @param err String describing the error
     * @throws IllegalArgumentException 
     */

    private static void dateFailed(String value, int i, int len, String err) throws IllegalArgumentException {
        int ei;

        if (i + len > value.length()) {
            ei = value.length();
        } else {
            ei = i + len;
        }
        throw new IllegalArgumentException("Date/time '" + value + "' is invalid: " + err + ". Saw '" + value.substring(i, ei) + "'");
    }
}