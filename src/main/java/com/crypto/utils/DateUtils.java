package com.crypto.utils;


import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateUtils {

    /**
     * Finds the next value of "Year" such that the date will be in the future.
     * For example
     *  -if dateWithoutYear is "1 Feb" and today's date is "26 Jan 2018", this function will return 2018.
     *  -if dateWithoutYear is "1 Feb" and today's date is "1 Mar 2018", this function will return 2019/
     * @param dateWithoutYear
     * @return
     */
    public static Integer findSubsequentYear(String dateWithoutYear) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("d MMM y");

        DateTime today = new DateTime();
        Integer currentYear = today.getYear();

        DateTime currentYearDate = formatter.parseDateTime(dateWithoutYear + " " + currentYear.toString());

        return currentYearDate.isBeforeNow() ? currentYear + 1 : currentYear;
    }
}
