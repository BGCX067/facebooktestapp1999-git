package com.sausageking.ui;

import android.graphics.RectF;
import android.view.View;

import com.sausageking.capture.FrameProcessor;
import com.sausageking.model.User;

public interface OverlayView {
  View toView();

  void setResult(String result);

  void showScanLine();

  RectF getFaceArea();

  void setToRecognitionMode();

  void setToUserMode(User user);

  void setToSignupMode();

  void setToTraningMode(User user);

  interface Presenter {

    void setView(OverlayView view);

    void saveLocal();

    void trainNextDetectedFace(String userId);

    void recognizeNextDetectedFace();

    void showScanLine();

    RectF getFaceArea();

    void setFrameProcessor(FrameProcessor frameProcessor);

    void setResult(String userId);

    void setToRecognitionMode();

    void setToUserMode(String userId);

    void setToSignupMode();

    void setToTraningMode(String userId);

  }
}