package com.videocreator;

import java.io.File;

/**
 * Date：2018/4/19
 * Author：HeChangPeng
 */

public class CommonUtil {

    public static void createFileDec(String fileDec) {
        try {
            File f = new File(fileDec);
            if (!f.exists()) {
                f.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createFile(String fileDec, String path) {
        try {
            File f = new File(fileDec);
            if (!f.exists()) {
                f.mkdirs();
            }
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
