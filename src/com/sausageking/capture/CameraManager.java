package com.sausageking.capture;

import android.graphics.Point;
import android.view.SurfaceHolder;

public interface CameraManager {
  void openDriver(SurfaceHolder holder, OnFinishCallback callback);

  void closeDriver();

  void startPreview(OnFinishCallback callback);

  void stopPreview();

  void setOrientation();

  Point getPreviewSize();

  int getPreviewFormat();

  void addContinuousPreviewImageCallback(ImageCallback imageCallback);

  interface ImageCallback {
    void onImage(byte[] data);
  }

  interface OnFinishCallback {
    void onFinish();
  }

  interface SizeSelector {
    Point getPreviewSize();
  }
}
