package com.sausageking.ui;

import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;

import com.sausageking.capture.FrameProcessor;

public interface OverlayView {
  View toView();

  void setResult(String result);

  void showScanLine();

  RectF getFaceArea();

  void setToRecognitionView();

  void setToUserView();

  void setToSignupView();

  void setToTraningView();

  ViewGroup getUserViewRoot();

  ViewGroup getScanningViewRoot();

  interface Presenter {

    void setView(OverlayView view);

    void saveLocal();

    void trainNextDetectedFace(String userId);

    void recognizeNextDetectedFace();

    void showScanLine();

    RectF getFaceArea();

    void setFrameProcessor(FrameProcessor frameProcessor);

    void setResult(String userId);

    void setToRecognitionView();

    void setToUserView();

    void setUserPresenter(UserView.Presenter userPresenter);

  }


}