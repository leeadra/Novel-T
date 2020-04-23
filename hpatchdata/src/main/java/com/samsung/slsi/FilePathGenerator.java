package com.samsung.slsi;

/**
 * Created by ch36.park on 2017. 6. 19..
 */

public class FilePathGenerator {

    private String prefix;
    private String postfix;

    private String targetPath;
    private String targetName;

    private long firstTime;
    private int firstSequence;
    private long milliSecondsPerSignalChunk;

    private int index;

    public FilePathGenerator(String prefix, String postfix, long milliSecondsPerSignalChunk) {
        this.prefix = prefix;
        this.postfix = postfix;
        this.milliSecondsPerSignalChunk = milliSecondsPerSignalChunk;

        firstTime = 0;
    }

    public void update(int sequence) {
        String path;

        long t;
        if (firstTime == 0) {
            firstTime = System.currentTimeMillis() / 1000;
            firstTime *= 1000;

            firstSequence = sequence;

            t = firstTime;
        } else {
            int sequenceGap = sequence - firstSequence;
            t = firstTime + sequenceGap * milliSecondsPerSignalChunk;
        }

        String year = TimeUtils.getYear(t);
        String month = TimeUtils.getMonth(t);
        String day = TimeUtils.getDay(t);

        String hour = TimeUtils.getHour(t);
        String minute = TimeUtils.getMinute(t);

        int m = Integer.parseInt(minute);
        minute = "" + (m / 10);

        String second = TimeUtils.getSecond(t);
        int s = Integer.parseInt(second);

        String milli = TimeUtils.getMillisecond(t);
        int ms = Integer.parseInt(milli);

        index = (int)((((m % 10) * 60 + s) * 1000 + ms) / milliSecondsPerSignalChunk);
        targetPath = year + "/" + month + "/" + day;
        targetName = prefix + hour + minute + postfix;
    }

    public String getFullPath() {
        return targetPath + "/" + targetName;
    }

    public String getPath() {
        return targetPath;
    }

    public int getIndex() {
        return index;
    }
}
