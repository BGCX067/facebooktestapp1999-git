package com.sausageking.ui;

import android.view.View;

public interface SignupView {
  View toView();

  interface Presenter {

    void setView(SignupView view);

    void setOverlayPresenter(
        com.sausageking.ui.OverlayView.Presenter overlayPresenter);

    OverlayView.Presenter getOverlayPresenter();
  }
}