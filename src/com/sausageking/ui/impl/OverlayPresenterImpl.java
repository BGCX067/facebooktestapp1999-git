package com.sausageking.ui.impl;

import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;

import com.sausageking.capture.FrameProcessor;
import com.sausageking.ui.OverlayView;
import com.sausageking.ui.OverlayView.Presenter;
import com.sausageking.ui.UserView;

public class OverlayPresenterImpl extends Thread implements Presenter {
  private OverlayView view;
  private Handler handler;
  private FrameProcessor frameProcessor;
  private UserView.Presenter userPresenter;

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
  public void setToRecognitionView() {
    setToRecognitionView();
  }

  @Override
  public void setToUserView() {
    view.setToUserView();
  }

  @Override
  public void setUserPresenter(
      com.sausageking.ui.UserView.Presenter userPresenter) {
    this.userPresenter = userPresenter;
  }
}
