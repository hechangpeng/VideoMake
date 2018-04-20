package com.videocreator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.concurrent.Executors;

/**
 * Date：2018/1/26
 * Author：HeChangPeng
 */

public class VideoMaker {
    public static final String TAG = "VideoMaker";
    private OnFinishListener mListener;

    public VideoMaker(OnFinishListener listener) {
        this.mListener = listener;
    }

    /**
     * 生成视频
     */
    public void makeVideo(final Context context) {
        if (Build.VERSION.SDK_INT < 18) {
            Log.e(TAG, "版本太低");
            return;
        }
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    Log.e(TAG, "SD卡未挂载，结束");
                    return;
                }
                writeImage(context);
                return;
            }
        });
    }

    private Matrix mMatrix = new Matrix();
    private Paint mPaint = new Paint();
    private int counter = 0;
    private int total = 0;

    private void writeImage(Context context) {
        mListener.onVideoMakeStart();
        total = 600;
        counter = 0;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 1;
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap finishBg = null;
        try {
            finishBg = BitmapFactory.decodeStream(context.getAssets().open("images/eg.png"), null, opt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (finishBg == null) {
            mListener.onVideoMakeFinish(false);
            return;
        }

        Bitmap finishBgX = null;
        try {
            finishBgX = BitmapFactory.decodeStream(context.getAssets().open("images/egx.png"), null, opt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (finishBgX == null) {
            mListener.onVideoMakeFinish(false);
            return;
        }

        File file = new File(Environment.getExternalStorageDirectory() + "/haha.mp4");
        VideoCreator videoCreator = VideoCreator.initCreator(file, 500, 500);
        for (int i = 0; i < total; i++) {
            if (i % 32 < 17) {
                Canvas a = videoCreator.lockCancas();
                a.drawColor(-1);
                a.drawBitmap(finishBg, mMatrix, mPaint);
                videoCreator.unLockCanvas(a);
            } else {
                Canvas a = videoCreator.lockCancas();
                a.drawColor(-1);
                a.drawBitmap(finishBgX, mMatrix, mPaint);
                videoCreator.unLockCanvas(a);
            }
            counter++;
            mListener.onProgressIn(new Float((counter / (total * 1.0)) * 100).intValue());
        }
        Log.e(TAG, "视频生成success !");
        if (videoCreator != null) {
            videoCreator.release();
        }
        mListener.onVideoMakeFinish(true);
    }
}
