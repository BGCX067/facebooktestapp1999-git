package com.sausageking.ui.impl;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.sausageking.ui.CameraPreviewView;

public class CameraPreviewViewImpl extends ViewGroup implements
    CameraPreviewView {
  private Point cameraPreviewSize;
  private boolean previewAreaFinalized = false;
  private Presenter presenter;
  private final SurfaceView previewView;

  public CameraPreviewViewImpl(Context context, Presenter presenter) {
    super(context);
    previewView = new SurfaceView(context);
    addView(previewView);
    setPresenter(presenter);
    presenter.setView(this);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // We purposely disregard child measurements because act as a
    // wrapper to a SurfaceView that centers the camera preview instead
    // of stretching it.
    final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
    final int height = resolveSize(getSuggestedMinimumHeight(),
        heightMeasureSpec);
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    final View child = getChildAt(0);
    final int width = r - l;
    final int height = b - t;
    int previewWidth = (cameraPreviewSize != null) ? cameraPreviewSize.x
        : width;
    int previewHeight = (cameraPreviewSize != null) ? cameraPreviewSize.y
        : height;

    // Rule to layout preview area:
    // 1. align at top
    // 2. fill the screen horizontally
    Rect previewArea = new Rect(0, 0, width, previewHeight * width
        / previewWidth);
    child.layout(previewArea.left, previewArea.top, previewArea.right,
        previewArea.bottom);

    if (!previewAreaFinalized && cameraPreviewSize != null) {
      previewAreaFinalized = true;
      presenter.onPreviewLayoutFinalized(previewArea);
    }
  }

  @Override
  public void setPreviewPadding(int width, int height) {
    cameraPreviewSize = new Point(width, height);
    requestLayout();
  }

  @Override
  public View toView() {
    return this;
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
    SurfaceHolder mHolder = previewView.getHolder();
    mHolder.addCallback(presenter);
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }
}