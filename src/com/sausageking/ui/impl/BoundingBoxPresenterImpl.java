package com.sausageking.ui.impl;

import android.graphics.Rect;
import android.graphics.RectF;

import com.sausageking.capture.CameraManager;
import com.sausageking.ui.BoundingBoxView;
import com.sausageking.ui.BoundingBoxView.Presenter;
import com.sausageking.ui.CameraPreviewView;

public class BoundingBoxPresenterImpl implements Presenter {

  private BoundingBoxView view;
  private final CameraPreviewView.Presenter cameraPreviewPresenter;
  private final CameraManager cameraManager;

  public BoundingBoxPresenterImpl(
      CameraPreviewView.Presenter cameraPreviewPresenter,
      CameraManager cameraManager) {
    this.cameraPreviewPresenter = cameraPreviewPresenter;
    this.cameraManager = cameraManager;
  }

  @Override
  public void setView(BoundingBoxView view) {
    this.view = view;
  }

  @Override
  public void onPreviewLayoutFinalized(Rect previewArea) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setFace(RectF faceRect) {
    view.setFace(faceRect);
  }

}
