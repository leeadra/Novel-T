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
 * @file		FileLogUtil.java
 * @brief		File Logging Utility on Android Device
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2017/1/5
 *
 * <b>revision history :</b>
 * - 2017/1/5 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FileLogUtil {
    public static boolean isInteralRelease = true; //Updated by BuildConfig class DEBUG
    public static boolean isStoreEnabled = true;
    public static boolean isShowLog = true; //Enable/Disable ADB Logging

    private static FileLogUtil globalLogger;
    private static String globalLogFile;

    public static void setGlobalLogger(String path, String fileName) {
        if (globalLogger == null) {
            globalLogger = new FileLogUtil(path);
            globalLogFile = fileName + "_" + getDateTextForFile(System.currentTimeMillis()) + ".txt";
        }
    }
    public static void log(String text) {
        if (globalLogger != null) {
            globalLogger.logging(globalLogFile, text);
        }
    }

    private String pathName;
    private boolean isDataFile;
    public void setDataFile() {
        isDataFile = true;
    }

    public FileLogUtil(String path) {
        init(path);
    }

    private void init(String path) {
        if (path != null) {
            if (path.length() > 0) {
                pathName = path;
            }
        }
        if (pathName.charAt(pathName.length() - 1) != '/') {
            pathName += "/";
        }
    }

    public static String getDateTextForFile(long t) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
        return sdf.format(new Date(t));
    }

    public static String getDateText(long t) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(new Date(t));
    }

    private File makeDirectory(String dirPath) throws FileNotFoundException {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new FileNotFoundException(dirPath);
            }
        }
        return dir;
    }

    private void storeFile(String path, String fileName, byte[] data, boolean isAppend)
            throws IOException {
        if (isStoreEnabled || isDataFile) {
            makeDirectory(path);
            FileOutputStream fos;

            fos = new FileOutputStream(path + fileName, isAppend);
            fos.write(data);
            fos.close();
        }
    }

    private Map<String, Boolean> logMap = new HashMap<String, Boolean>();

    public void logging(String fileName, String text) {
        String message = "";

        StackTraceElement ste = Thread.currentThread().getStackTrace()[4];
        if (!isDataFile) {
            message += "[" + ste.getFileName().replace(".java", "") + "::" + ste.getMethodName() + "] ";
        }
        message += text + "\n";
        loggingWithoutPrefix(fileName, message);
    }

    public void loggingWithoutPrefix(String fileName, String text) {
        if (isShowLog) {
            System.out.println(text);
        }
        logging(fileName, text.getBytes());
    }

    public void logging(String fileName, byte[] data) {
        try {
            boolean isAppend;
            isAppend = logMap.containsKey(fileName);
            storeFile(pathName, fileName, data, isAppend);
            logMap.put(fileName, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
