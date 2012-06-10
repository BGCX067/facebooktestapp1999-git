package com.sausageking.ui.impl;

import com.sausageking.model.User;
import com.sausageking.ui.OverlayView;
import com.sausageking.ui.UserView;
import com.sausageking.ui.UserView.Presenter;

public class UserPresenterImpl implements Presenter {

  private UserView view;
  private OverlayView.Presenter overlayPresenter;

  public UserPresenterImpl(OverlayView.Presenter overlayPresenter) {
    this.view = view;
    setOverlayPresenter(overlayPresenter);
    overlayPresenter.setUserPresenter(this);
  }

  @Override
  public void setOverlayPresenter(
      com.sausageking.ui.OverlayView.Presenter overlayPresenter) {
    this.overlayPresenter = overlayPresenter;
  }

  @Override
  public void setView(UserView view) {
    this.view = view;
  }

  @Override
  public void setUserId(String userId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setToConfirmUserMode(User user) {
    view.setToConfirmUserMode(user);
  }

  @Override
  public void setToUnredeemableMode(User user) {
    view.setToUnredeemableMode(user);
  }

  @Override
  public void setToRedeemableMode(User user) {
    view.setToRedeemableMode(user);
  }

  @Override
  public void setToRedeemingMode(User user) {
    view.setToRedeemingMode(user);
  }

}
