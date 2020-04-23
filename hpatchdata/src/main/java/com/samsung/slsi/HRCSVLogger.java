package com.samsung.slsi;

/**
 * Created by ch36.park on 2017. 2. 21..
 */

public class HRCSVLogger {
    private String fileName;
    private String path;
    private FileLogUtil logger;

    public HRCSVLogger(String path, String fileName) {
        this.path = path;
        this.fileName = fileName;

        //logger = new FileLogUtil(path);
    }

    public void add(int hr) {
        String text = TimeUtils.getDateText(System.currentTimeMillis()) + ", " + hr + "\n";
        //logger.logging(fileName, text);
    }
}
