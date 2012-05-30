package com.sausageking.ui.impl;

import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;

import com.sausageking.capture.FrameProcessor;
import com.sausageking.ui.OverlayView;
import com.sausageking.ui.OverlayView.Presenter;

public class OverlayPresenterImpl extends Thread implements Presenter {
  private OverlayView view;
  private Handler handler;
  private FrameProcessor frameProcessor;

  @Override
  public void setFrameProcessor(FrameProcessor processor) {
    this.frameProcessor = processor;
  }

  @Override
  public void run() {
    Looper.prepare();
    handler = new Handler();
    Looper.loop();
  }

  @Override
  public void setView(OverlayView view) {
    this.view = view;
  }

  @Override
  public void saveLocal() {
    frameProcessor.setSaveAFaceMode();
  }

  @Override
  public void trainNextDetectedFace(String userId) {
    frameProcessor.setTrainingMode(userId);
  }

  @Override
  public void recognizeNextDetectedFace() {
    frameProcessor.setRecognitionMode();
  }

  @Override
  public void showScanLine() {

    view.showScanLine();
  }

  @Override
  public RectF getFaceArea() {
    return view.getFaceArea();
  }

  @Override
  public void setResult(String result) {
    view.setResult(result);
  }

  @Override
  public void setToRecognitionMode() {
    // TODO Auto-generated method stub

  }

  @Override
  public void setToUserMode(String userId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setToSignupMode() {
    // TODO Auto-generated method stub

  }

  @Override
  public void setToTraningMode(String userId) {
    // TODO Auto-generated method stub

  }
}
