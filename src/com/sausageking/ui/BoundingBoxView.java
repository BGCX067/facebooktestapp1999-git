package com.sausageking.ui;

import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

public interface BoundingBoxView {
  View toView();

  void setPreviewArea(Rect previewArea);

  void showMessage();

  void setFace(RectF faceRect);

  interface Presenter {

    void setView(BoundingBoxView view);

    void onPreviewLayoutFinalized(Rect previewArea);

    void setFace(RectF faceRect);
  }
}