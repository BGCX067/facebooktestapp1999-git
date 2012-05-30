package com.sausageking.ui.impl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.view.View;

import com.sausageking.ui.BoundingBoxView;

public class BoundingBoxViewImpl extends View implements BoundingBoxView {

  private RectF faceRect;
  private final Handler handler;
  private final Paint faceBoundaryPaint;

  public BoundingBoxViewImpl(Context context, Presenter presenter) {
    super(context);
    presenter.setView(this);
    handler = new Handler();
    faceBoundaryPaint = new Paint();
    faceBoundaryPaint.setARGB(30, 0, 0, 0);
    faceBoundaryPaint.setStyle(Style.STROKE);
    faceBoundaryPaint.setStrokeWidth(8);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (faceRect != null) {
      canvas.drawRect(faceRect, getFacePaint());
    }
  }

  private Paint getFacePaint() {
    return faceBoundaryPaint;
  }

  @Override
  public View toView() {
    // TODO Auto-generated method stub
    return this;
  }

  @Override
  public void setPreviewArea(Rect previewArea) {
  }

  @Override
  public void showMessage() {
    // TODO Auto-generated method stub
  }

  @Override
  public void setFace(RectF faceRect) {
    this.faceRect = faceRect;
    handler.post(new Runnable() {
      @Override
      public void run() {
        BoundingBoxViewImpl.this.invalidate();
      }
    });
  }

}
