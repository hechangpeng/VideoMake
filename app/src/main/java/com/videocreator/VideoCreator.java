package com.videocreator;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.view.Surface;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Date：2018/4/17
 * Author：HeChangPeng
 */
@TargetApi(18)
public class VideoCreator {
    private long frameGap;
    private MediaCodec.BufferInfo bufferInfo;
    private MediaCodec mediaCodec;
    private MediaMuxer mediaMuxer;
    private Surface surface;
    private int trackIndex;
    private boolean isStarted;
    private int width;
    private int height;

    private VideoCreator(File file, int w, int h) {
        this.width = w;
        this.height = h;
        try {
            setParams(file);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static VideoCreator initCreator(File file, int w, int h) {
        return new VideoCreator(file, w, h);
    }

    public Canvas lockCancas() {
        Canvas canvas = null;
        queueData(false);
        try {
            canvas = this.surface.lockCanvas(null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Surface.OutOfResourcesException e2) {
            e2.printStackTrace();
        }
        return canvas;
    }

    public void unLockCanvas(Canvas canvas) {
        this.surface.unlockCanvasAndPost(canvas);
    }

    public void release() {
        try {
            queueData(true);
            releaseMedia();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setParams(File file) {
        this.bufferInfo = new MediaCodec.BufferInfo();
        MediaFormat createVideoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, this.width, this.height);
        createVideoFormat.setInteger("color-format", 2130708361);
        createVideoFormat.setInteger("bitrate", 4000000);
        createVideoFormat.setInteger("frame-rate", 16);
        createVideoFormat.setInteger("i-frame-interval", 5);
        try {
            this.mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mediaCodec.configure(createVideoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        this.surface = this.mediaCodec.createInputSurface();
        this.mediaCodec.start();
        try {
            this.mediaMuxer = new MediaMuxer(file.toString(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.trackIndex = -1;
        this.isStarted = false;
    }

    private void releaseMedia() {
        destroyMediaCodec();
        destroySurface();
        destroyMediaMuxer();
    }

    private void destroyMediaCodec() {
        try {
            if (this.mediaCodec != null) {
                this.mediaCodec.stop();
                this.mediaCodec.release();
                this.mediaCodec = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void destroySurface() {
        try {
            if (this.surface != null) {
                this.surface.release();
                this.surface = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void destroyMediaMuxer() {
        try {
            if (this.mediaMuxer != null) {
                this.mediaMuxer.stop();
                this.mediaMuxer.release();
                this.mediaMuxer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void queueData(boolean z) {
        if (z) {
            this.mediaCodec.signalEndOfInputStream();
        }
        ByteBuffer[] outputBuffers = this.mediaCodec.getOutputBuffers();
        while (true) {
            int dequeueOutputBuffer = this.mediaCodec.dequeueOutputBuffer(this.bufferInfo, 10000);
            if (dequeueOutputBuffer == -1) {
                if (z) {
                } else {
                    return;
                }
            } else if (dequeueOutputBuffer == -3) {
                outputBuffers = this.mediaCodec.getOutputBuffers();
            } else if (dequeueOutputBuffer == -2) {
                if (this.isStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat outputFormat = this.mediaCodec.getOutputFormat();
                this.trackIndex = this.mediaMuxer.addTrack(outputFormat);
                this.mediaMuxer.start();
                this.isStarted = true;
            } else if (dequeueOutputBuffer < 0) {
            } else {
                ByteBuffer byteBuffer = outputBuffers[dequeueOutputBuffer];
                if (byteBuffer == null) {
                    throw new RuntimeException("encoderOutputBuffer " + dequeueOutputBuffer + " was null");
                }
                if ((this.bufferInfo.flags & 2) != 0) {
                    this.bufferInfo.size = 0;
                }
                if (this.bufferInfo.size != 0) {
                    if (this.isStarted) {
                        byteBuffer.position(this.bufferInfo.offset);
                        byteBuffer.limit(this.bufferInfo.offset + this.bufferInfo.size);
                        this.bufferInfo.presentationTimeUs = this.frameGap;
                        this.frameGap += 62500;
                        this.mediaMuxer.writeSampleData(this.trackIndex, byteBuffer, this.bufferInfo);
                    } else {
                        throw new RuntimeException("muxer hasn't started");
                    }
                }
                this.mediaCodec.releaseOutputBuffer(dequeueOutputBuffer, false);
                if ((this.bufferInfo.flags & 4) != 0) {
                    break;
                }
            }
        }
    }
}
