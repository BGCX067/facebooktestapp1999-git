package com.sausageking.capture.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.sausageking.capture.CameraManager;

public class CameraManagerImpl implements CameraManager {
  private final double ASPECT_TOLERANCE = 0.1;
  private final int minSdkVersionForPreviewCallbackBuffer;
  private final Context context;
  private final boolean usePreviewCallbackBuffer;
  private final SizeSelector sizeSelector;
  private final List<ImageCallback> continuousPreviewImageCallbacks;

  private Camera camera;
  private boolean previewing;
  private Point previewSize;
  private byte[] previewCallbackBuffer;

  private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
      if (!continuousPreviewImageCallbacks.isEmpty()) {
        for (ImageCallback callback : continuousPreviewImageCallbacks) {
          callback.onImage(data);
        }
      }
      if (usePreviewCallbackBuffer) {
        addCallbackBuffer();
      }
    }
  };

  public CameraManagerImpl(Context context) {
    this.context = context;
    minSdkVersionForPreviewCallbackBuffer = 11;
    camera = null;
    previewCallbackBuffer = null;
    previewing = false;
    sizeSelector = new SizeSelectorImpl();
    usePreviewCallbackBuffer = usePreviewCallbackBuffer();
    continuousPreviewImageCallbacks = new ArrayList<CameraManager.ImageCallback>();
  }

  @Override
  public void openDriver(SurfaceHolder holder, OnFinishCallback callback) {
    if (camera == null) {
      camera = Camera.open(getFrontFacingCameraId());
      try {
        camera.setPreviewDisplay(holder);
      } catch (IOException e) {
      }
      setCameraParameters();
      callback.onFinish();
    }
  }

  private int getFrontFacingCameraId() {
    int cameraId = -1;
    int numberOfCameras = Camera.getNumberOfCameras();
    CameraInfo info = new CameraInfo();
    for (int i = 0; i < numberOfCameras; i++) {
      Camera.getCameraInfo(i, info);
      if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
        cameraId = i;
      }
    }
    if (cameraId == -1) {
      throw new RuntimeException("Device does not support front facing camera.");
    }
    return cameraId;
  }

  @Override
  public void closeDriver() {
    if (camera != null) {
      setPreviewCallback(null);
      camera.stopPreview();
      previewing = false;
      camera.release();
      camera = null;
    }
  }

  @Override
  public void startPreview(OnFinishCallback callback) {
    if (camera != null && !previewing) {
      camera.startPreview();
      previewing = true;
      setPreviewCallback(previewCallback);
      if (callback != null) {
        callback.onFinish();
      }
    }
  }

  @Override
  public void addContinuousPreviewImageCallback(ImageCallback imageCallback) {
    continuousPreviewImageCallbacks.add(imageCallback);
    // required to kick off
    if (imageCallback != null && usePreviewCallbackBuffer()) {
      addCallbackBuffer();
    }
  }

  @Override
  public void stopPreview() {
    if (camera != null && previewing) {
      setPreviewCallback(null);
      camera.stopPreview();
      previewing = false;
    }
  }

  private void setPreviewCallback(Camera.PreviewCallback previewCallback) {
    if (usePreviewCallbackBuffer) {
      camera.setPreviewCallbackWithBuffer(previewCallback);
    } else {
      camera.setPreviewCallback(previewCallback);
    }
  }

  private boolean usePreviewCallbackBuffer() {
    return android.os.Build.VERSION.SDK_INT >= minSdkVersionForPreviewCallbackBuffer;
  }

  private int getPreviewBufferSize() {
    Camera.Parameters params = camera.getParameters();
    Camera.Size size = params.getPreviewSize();
    int bufferSize = size.width * size.height;
    bufferSize = bufferSize
        * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
    bufferSize += 16;
    return bufferSize;
  }

  private void setCameraParameters() {
    previewSize = sizeSelector.getPreviewSize();
    Camera.Parameters parameters = camera.getParameters();
    parameters.setPreviewSize(previewSize.x, previewSize.y);
    camera.setParameters(parameters);
  }

  private void addCallbackBuffer() {
    if (previewCallbackBuffer == null) {
      previewCallbackBuffer = new byte[getPreviewBufferSize()];
    }
    camera.addCallbackBuffer(previewCallbackBuffer);
  }

  @Override
  public void setOrientation() {
    CameraInfo info = new CameraInfo();
    Camera.getCameraInfo(getFrontFacingCameraId(), info);
    WindowManager windowManager = (WindowManager) (context
        .getSystemService(Context.WINDOW_SERVICE));
    int rotation = windowManager.getDefaultDisplay().getRotation();
    int degrees = 0;
    switch (rotation) {
    case Surface.ROTATION_0:
      degrees = 0;
      break;
    case Surface.ROTATION_90:
      degrees = 90;
      break;
    case Surface.ROTATION_180:
      degrees = 180;
      break;
    case Surface.ROTATION_270:
      degrees = 270;
      break;
    }
    int result;
    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      result = (info.orientation + degrees) % 360;
      result = (360 - result) % 360;
    } else {
      result = (info.orientation - degrees + 360) % 360;
    }
    camera.setDisplayOrientation(result);
  }

  @Override
  public Point getPreviewSize() {
    return previewSize;
  }

  /**
   * Find the supported preview size who's aspect ratio best matches screen or
   * picture size.
   */
  private class SizeSelectorImpl implements SizeSelector {

    public SizeSelectorImpl() {

    }

    /**
     * Lazily calculate preview size.
     */
    @Override
    public Point getPreviewSize() {
      if (previewSize != null) {
        return previewSize;
      }
      Camera.Parameters cameraParams = camera.getParameters();
      Point sizeToMatch;
      sizeToMatch = getScreenResolution();

      List<Size> supportedPreviewSizes = cameraParams
          .getSupportedPreviewSizes();

      previewSize = new Point();
      double targetRatio = (double) sizeToMatch.x / sizeToMatch.y;
      if (supportedPreviewSizes == null) {
        previewSize.x = (sizeToMatch.x >> 3) << 3;
        previewSize.y = (sizeToMatch.y >> 3) << 3;
      } else {
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = sizeToMatch.y;
        for (Size size : supportedPreviewSizes) {
          double ratio = (double) size.width / size.height;
          if (Math.abs(ratio - targetRatio) <= ASPECT_TOLERANCE
              && Math.abs(size.height - targetHeight) < minDiff) {
            optimalSize = size;
            minDiff = Math.abs(size.height - targetHeight);
          }
        }
        if (optimalSize == null) {
          minDiff = Double.MAX_VALUE;
          for (Size size : supportedPreviewSizes) {
            if (Math.abs(size.height - targetHeight) < minDiff) {
              optimalSize = size;
              minDiff = Math.abs(size.height - targetHeight);
            }
          }
        }
        previewSize.set(optimalSize.width, optimalSize.height);
      }
      return previewSize;
    }

    /**
     * Calculates the screen resolution in pixels.
     * 
     * @return The width and height of the screen as a Point.
     */
    private Point getScreenResolution() {
      WindowManager manager = (WindowManager) context
          .getSystemService(Context.WINDOW_SERVICE);
      Display display = manager.getDefaultDisplay();
      return new Point(display.getWidth(), display.getHeight());
    }
  }

  @Override
  public int getPreviewFormat() {
    return camera.getParameters().getPreviewFormat();
  }
}