/*
 * Sensor Product Development Team, System LSI division.
 * Copyright (c) 2014-2017 Samsung Electronics, Inc.
 * All right reserved.
 *
 * This software is the confidential and proprietary information
 * of Samsung Electronics, Inc. (Confidential Information). You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Samsung Electronics.
*/
/**
 *******************************************************************************
 * @file		TimeUtils.java
 * @brief		Time to Date/Time String Converting Utility Functions
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		1.0
 * @date		2016/12/26
 *
 * <b>revision history :</b>
 * - 2016/12/26 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
    private static SimpleDateFormat sdfDateTimeForFile = new SimpleDateFormat("yyyy_MM_dd_HHmmss", Locale.US);
    public static String getDateTextForFile(long t) {
        return sdfDateTimeForFile.format(new Date(t));
    }

    private static SimpleDateFormat sdfDateText = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public static String getDateText(long t) {
        return sdfDateText.format(new Date(t));
    }

    private static SimpleDateFormat sdfTimeOnlyText = new SimpleDateFormat("HH:mm:ss", Locale.US);
    public static String getTimeOnlyText(long t) {
        return sdfTimeOnlyText.format(new Date(t));
    }

    public static String getTimeText(long timeMS) {
        int days;
        int hours;
        int minutes;
        int seconds;
        int millis;

        long t = timeMS;
        millis = (int)((t % 1000));
        t = t / 1000;
        seconds = (int)(t % 60);
        t = t / 60;
        minutes = (int)(t % 60);
        t = t / 60;
        hours = (int)(t % 24);
        t = t / 24;
        days = (int)(t);

        String dateFormatted = "";
        if (days > 0) {
            dateFormatted += days + "d ";
        }
        //if (hours > 0)
        {
            dateFormatted += String.format(Locale.getDefault(), "%02d:", hours);
        }
        dateFormatted += String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        return dateFormatted;
    }

    public static String getLapTimeText(long ms) {
        int days;
        int hours;
        int minutes;
        int seconds;
        int millis;

        long t = ms;
        millis = (int)((t % 1000));
        t = t / 1000;
        seconds = (int)(t % 60);
        t = t / 60;
        minutes = (int)(t % 60);
        t = t / 60;
        hours = (int)(t % 24);
        t = t / 24;
        days = (int)(t);

        String dateFormatted = "";
        if (days > 0) {
            dateFormatted += days + "d ";
        }
        if (hours > 0) {
            if (dateFormatted.length() > 0) {
                dateFormatted += String.format(Locale.getDefault(), "%02d:", hours);
            } else {
                dateFormatted += String.format(Locale.getDefault(), "%d:", hours);
            }
        }
        if (minutes > 0) {
            if (dateFormatted.length() > 0) {
                dateFormatted += String.format(Locale.getDefault(), "%02d:", minutes);
            } else {
                dateFormatted += String.format(Locale.getDefault(), "%d:", minutes);
            }
        }
        if (dateFormatted.length() > 0) {
            dateFormatted += String.format(Locale.getDefault(), "%02d.%03d", seconds, millis);
        } else {
            dateFormatted += String.format(Locale.getDefault(), "%d.%03d", seconds, millis);
        }
        return dateFormatted;
    }

    private static SimpleDateFormat sdfYear = new SimpleDateFormat("y", Locale.US);
    public static String getYear(long timeMS) {
        return sdfYear.format(new Date(timeMS));
    }

    private static SimpleDateFormat sdfMonth = new SimpleDateFormat("M", Locale.US);
    public static String getMonth(long timeMS) {
        return sdfMonth.format(new Date(timeMS));
    }

    private static SimpleDateFormat sdfDay = new SimpleDateFormat("d", Locale.US);
    public static String getDay(long timeMS) {
        return sdfDay.format(new Date(timeMS));
    }

    private static SimpleDateFormat sdfHour = new SimpleDateFormat("HH", Locale.US);
    public static String getHour(long timeMS) {
        return sdfHour.format(new Date(timeMS));
    }

    private static SimpleDateFormat sdfMinute = new SimpleDateFormat("mm", Locale.US);
    public static String getMinute(long timeMS) {
        return sdfMinute.format(new Date(timeMS));
    }

    private static SimpleDateFormat sdfSecond = new SimpleDateFormat("ss", Locale.US);
    public static String getSecond(long timeMS) {
        return sdfSecond.format(new Date(timeMS));
    }

    private static SimpleDateFormat sdfMillisecond = new SimpleDateFormat("SSS", Locale.US);
    public static String getMillisecond(long timeMS) {
        return sdfMillisecond.format(new Date(timeMS));
    }
}
