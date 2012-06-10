package com.sausageking.ui.impl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.sausageking.R;
import com.sausageking.model.User;
import com.sausageking.ui.OverlayView;
import com.sausageking.ui.UserView;

public class UserViewImpl implements UserView {
  private final UserView.Presenter presenter;
  private final OverlayView overlayView;
  private final View view;

  public UserViewImpl(Context context, UserView.Presenter presenter,
      OverlayView overlayView) {
    this.presenter = presenter;
    this.overlayView = overlayView;
    view = LayoutInflater.from(context).inflate(R.layout.user,
        overlayView.getUserViewRoot(), false);
    overlayView.getUserViewRoot().addView(view);

    presenter.setView(this);
  }

  @Override
  public View toView() {
    return view;
  }

  @Override
  public void setToConfirmUserMode(User user) {


  }

  @Override
  public void setToUnredeemableMode(User user) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setToRedeemableMode(User user) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setToRedeemingMode(User user) {
    // TODO Auto-generated method stub

  }

}
