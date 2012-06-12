package com.sausageking.ui;

import android.view.View;

import com.sausageking.model.User;

public interface UserView {
  View toView();

  void setToConfirmUserMode(User user);

  void setToUnredeemableMode(User user);

  void setToRedeemableMode(User user);

  void setToRedeemingMode(User user);

  interface Presenter {

    void setView(UserView view);

    void setUserId(String userId);

    void setOverlayPresenter(
        com.sausageking.ui.OverlayView.Presenter overlayPresenter);

    void setToConfirmUserMode(User user);

    void setToUnredeemableMode(User user);

    void setToRedeemableMode(User user);

    void setToRedeemingMode(User user);

    OverlayView.Presenter getOverlayPresenter();
  }

  enum Mode {
    CONFIRM_USER, UNREDEEMABLE, REDEEMABLE, REDEEMING
  }
}