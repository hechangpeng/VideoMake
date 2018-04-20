package com.videocreator.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.widget.Toast;

import com.videocreator.CommonUtil;
import com.videocreator.example.R;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;

/**
 * Date：2018/4/19
 * Author：HeChangPeng
 */

public class ScreenRecordActivity extends Activity {
    private static final String TAG = "TAG";
    private static final int RECORD_REQUEST_CODE = 101;
    private int width;
    private int height;
    private int dpi;
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private MediaCodec mediaCodec;
    private MediaMuxer mediaMuxer;
    private Surface surface;
    private VirtualDisplay virtualDisplay;
    private MediaCodec.BufferInfo bufferInfo;
    private int videoTrackIndex = -1;
    private String filePath;
    private AtomicBoolean mQuit = new AtomicBoolean(false);
    private boolean isStart = false;
    private boolean isStop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sceen_record);
        initRecorder();
        startRecorder();
    }

    @TargetApi(21)
    private void initRecorder() {
        bufferInfo = new MediaCodec.BufferInfo();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        width = new Float(metrics.widthPixels / 2.0f).intValue();
        height = new Float(metrics.heightPixels / 2.0f).intValue();
        dpi = 1;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videocreator/";
        String fileName = "screen_" + System.currentTimeMillis() + ".mp4";
        CommonUtil.createFileDec(path);
        filePath = path + fileName;
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    @TargetApi(21)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mediaProjection = projectionManager.getMediaProjection(resultCode, data);
                executeRecoder();
            } else {
                Toast.makeText(this, "user cancel !", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @TargetApi(21)
    private void executeRecoder() {
        isStart = true;
        new Thread() {
            @Override
            public void run() {
                try {
                    try {
                        prepareEncoder();
                        CommonUtil.createFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/videocreator/", filePath);
                        mediaMuxer = new MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    virtualDisplay = mediaProjection.createVirtualDisplay(TAG + "-display",
                            width, height, dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                            surface, null, null);
                    recordVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    release();
                }
            }
        }.start();
    }

    @TargetApi(21)
    private void recordVirtualDisplay() {
        while (!mQuit.get()) {
            int index = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                resetOutputFormat();
            } else if (index >= 0) {
                if (!isStop) {
                    encodeToVideoTrack(index);
                }
                mediaCodec.releaseOutputBuffer(index, false);
            }
        }
    }

    long i = 0;

    @TargetApi(21)
    private void encodeToVideoTrack(int index) {
        ByteBuffer encodedData = mediaCodec.getOutputBuffer(index);
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            bufferInfo.size = 0;
        }
        if (bufferInfo.size == 0) {
            encodedData = null;
        }
        if (encodedData != null) {
            encodedData.position(bufferInfo.offset);
            encodedData.limit(bufferInfo.offset + bufferInfo.size);
            this.bufferInfo.presentationTimeUs = this.i;
            this.i += 30000;
            try {
                mediaMuxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(21)
    private void resetOutputFormat() {
        MediaFormat newFormat = mediaCodec.getOutputFormat();
        videoTrackIndex = mediaMuxer.addTrack(newFormat);
        mediaMuxer.start();
    }

    @TargetApi(21)
    private void prepareEncoder() throws IOException {
        MediaFormat format = MediaFormat.createVideoFormat(MIMETYPE_VIDEO_AVC, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1000000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 16);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        mediaCodec = MediaCodec.createEncoderByType(MIMETYPE_VIDEO_AVC);
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        surface = mediaCodec.createInputSurface();
        mediaCodec.start();
    }

    @TargetApi(21)
    public void startRecorder() {
        startActivityForResult(projectionManager.createScreenCaptureIntent(), RECORD_REQUEST_CODE);
    }

    public void stopRecorder() {
        mQuit.set(true);
        if (isStart) {
            Toast.makeText(this, "Record successful !", Toast.LENGTH_SHORT).show();
        }
        isStart = false;
    }

    @TargetApi(21)
    private void release() {
        try {
            if (mediaCodec != null) {
                mediaCodec.stop();
                mediaCodec.release();
                mediaCodec = null;
            }
            if (virtualDisplay != null) {
                virtualDisplay.release();
            }
            if (mediaProjection != null) {
                mediaProjection.stop();
            }
            if (mediaMuxer != null) {
                mediaMuxer.release();
                mediaMuxer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopRecorder();
        release();
        isStart = false;
        super.onDestroy();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
    }
}
