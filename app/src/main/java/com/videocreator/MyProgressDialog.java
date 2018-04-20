package com.videocreator;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Date：2018/4/19
 * Author：HeChangPeng
 */

public class MyProgressDialog {
    private ProgressDialog dialog;

    public MyProgressDialog(Context context) {
        if (dialog == null) {
            dialog = new ProgressDialog(context);
            dialog.setTitle("正在生成中");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(true);
            dialog.setMax(100);
        }
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void setProgress(int progress) {
        dialog.setProgress(progress);
    }
}
