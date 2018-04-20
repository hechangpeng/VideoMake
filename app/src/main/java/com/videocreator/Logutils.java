package com.videocreator;

import android.util.Log;

/**
 * Date：2018/1/17
 * Author：HeChangPeng
 */

public class Logutils {

    public static final boolean ISDEBUG = true;
    private static final String TAG = "hecp";

    public static void e(String message) {
        if (!ISDEBUG) {
            return;
        }
        StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
        Log.e(TAG, stackTraceElements[1].getFileName().replace(".java", "") + " -> " + message);
    }
}
