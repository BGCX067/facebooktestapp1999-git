package com.sausageking.ui.impl;

import java.util.ArrayList;
import java.util.Collection;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;

import com.sausageking.capture.CameraManager;
import com.sausageking.capture.FrameProcessor;
import com.sausageking.ui.CameraPreviewView;
import com.sausageking.ui.CameraPreviewView.Presenter;

public class CameraPreviewPresenterImpl implements Presenter {
  private final CameraManager cameraManager;
  private final Collection<FrameProcessor> frameProcessors;
  private CameraPreviewView view;
  private boolean previewing = false;
  private Rect previewArea;

  public CameraPreviewPresenterImpl(CameraManager cameraManager) {
    this.cameraManager = cameraManager;
    frameProcessors = new ArrayList<FrameProcessor>();
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width,
      int height) {
    if (!previewing) {
      try {
        cameraManager.openDriver(holder, new CameraManager.OnFinishCallback() {
          @Override
          public void onFinish() {
            Point previewSize = cameraManager.getPreviewSize();

            view.setPreviewPadding(previewSize.x, previewSize.y);

            cameraManager.setOrientation();
            cameraManager.startPreview(new CameraManager.OnFinishCallback() {
              @Override
              public void onFinish() {

                for (FrameProcessor frameProcessor : frameProcessors) {
                  frameProcessor.start();
                }
              }
            });
          }
        });
        previewing = true;
      } catch (RuntimeException e) {
        e.printStackTrace();
        handleCameraException(e);
      }
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    // no-op
  }

  private void handleCameraException(Exception e) {
    Log.e(getClass().getSimpleName(), e.toString());
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    for (FrameProcessor frameProcessor : frameProcessors) {
      frameProcessor.stop();
    }
    cameraManager.closeDriver();
    previewing = false;
  }

  @Override
  public void setView(CameraPreviewView view) {
    this.view = view;
  }

  @Override
  public void addFrameProcessor(FrameProcessor frameProcessor) {
    frameProcessors.add(frameProcessor);
  }

  @Override
  public Rect getPreviewArea() {
    return previewArea;
  }

  @Override
  public void onPreviewLayoutFinalized(Rect previewArea) {
    this.previewArea = previewArea;
  }

}
