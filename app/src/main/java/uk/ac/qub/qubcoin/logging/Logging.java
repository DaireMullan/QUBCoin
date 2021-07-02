package uk.ac.qub.qubcoin.logging;

import android.util.Log;

import uk.ac.qub.qubcoin.BuildConfig;

public class Logging {

    public static void verbose(String tag, String message) {
        writeLog(tag, message, Log.VERBOSE);
    }

    public static void debug(String tag, String message) {
        writeLog(tag, message, Log.DEBUG);
    }

    public static void info(String tag, String message) {
        writeLog(tag, message, Log.INFO);
    }

    public static void warn(String tag, String message) {
        writeLog(tag, message, Log.WARN);
    }

    public static void error(String tag, String message) {
        writeLog(tag, message, Log.ERROR);
    }

    private static void writeLog(String tag, String message, int logLevel) {
        if (BuildConfig.DEBUG) {
            switch (logLevel) {
                case Log.VERBOSE:
                    Log.v(tag, message);
                    break;
                case Log.DEBUG:
                    Log.d(tag, message);
                    break;
                case Log.INFO:
                    Log.i(tag, message);
                    break;
                case Log.WARN:
                    Log.w(tag, message);
                    break;
                case Log.ERROR:
                    Log.e(tag, message);
                    break;
                default:
                    Log.wtf(tag, message);
                    break;
            }
        }
    }

}
