package com.videocreator;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.List;

/**
 * Date：2018/4/20
 * Author：HeChangPeng
 */

public class CameraInstance {
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private CameraListener mListener;

    public interface CameraListener {
        void getPreviewFrame(byte[] data);
    }

    public CameraInstance(Context context, SurfaceView surfaceView, CameraListener listener) {
        this.mSurfaceView = surfaceView;
        this.mListener = listener;
        initSurfaceHolder(context);
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    private void initSurfaceHolder(final Context context) {
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (hasBackFacingCamera()) {
                    initCamera(0);
                    return;
                }
                if (hasFrontFacingCamera()) {
                    initCamera(1);
                    return;
                }
                Toast.makeText(context, "sorry,no available camera !", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    private void initCamera(int cameraId) {
        destoryCamera();
        openCamera(cameraId);
        setParameters();
        startPreview();
    }

    private void setParameters() {
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mSurfaceHolder);
            Camera.Parameters parameters = mCamera.getParameters();
           /* List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            Logutils.e("mSurfaceView.getWidth()=" + mSurfaceView.getWidth() + "，mSurfaceView.getHeight()=" + mSurfaceView.getHeight());
            final Camera.Size optimalSize = getOptimalPreviewSize(sizes, mSurfaceView.getWidth(), mSurfaceView.getHeight());
            int previewWidth = optimalSize.width;
            int previewHeight = optimalSize.height;
            Logutils.e("optimalSize.width=" + previewWidth + "，optimalSize.height=" + previewHeight);*/
            parameters.setPreviewSize(screenWidth, screenHeight);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.setPreviewFpsRange(4, 10);
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality(90);
            List<Camera.Size> picsizes = parameters.getSupportedPictureSizes();
            Camera.Size picSize = getOptimalPreviewSize(picsizes, mSurfaceView.getWidth(), mSurfaceView.getHeight());
            parameters.setPictureSize(picSize.width, picSize.height);
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    mListener.getPreviewFrame(data);
                }
            });
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        try {
            if (mCamera != null) {
                mCamera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPreview() {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destoryCamera() {
        stopPreviewCall();
        stopPreview();
        releaseCamera();
        mCamera = null;
    }

    private void stopPreviewCall() {
        try {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        try {
            if (mCamera != null) {
                mCamera.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openCamera(int cameraId) {
        try {
            mCamera = Camera.open(cameraId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkCameraFacing(final int facing) {
        final int cameraCount = Camera.getNumberOfCameras();
        Logutils.e("相机个数=" + cameraCount);
        Camera.CameraInfo info = new Camera.CameraInfo();
        boolean flag = false;
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (facing == info.facing) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 支持后置摄像头
     */
    public boolean hasBackFacingCamera() {
        try {
            return checkCameraFacing(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 支持前置摄像头
     */
    public boolean hasFrontFacingCamera() {
        try {
            return checkCameraFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
