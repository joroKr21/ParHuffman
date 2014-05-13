package test;

/**
 * @author Georgi Krastev <joro.kr.21@gmail.com>
 */
public class TimeLogger {

    private static TimeLogger logger; // singleton instance
    private long time;                // in ms
    private boolean quiet;            // logging mode

    private TimeLogger(long time) {
        this.time = time;
    }

    public static TimeLogger getLogger() {
        if (logger == null) {
            logger = new TimeLogger(System.currentTimeMillis());
        }

        return logger;
    }

    public void log(String msg) {
        System.out.printf("%7.3f", (System.currentTimeMillis() - getTime()) / 1000.0);
        if (!isQuiet()) {
            System.out.println(" - " + msg);
        } else {
            System.out.println();
        }
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void resetTime() {
        time = System.currentTimeMillis();
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }
}
