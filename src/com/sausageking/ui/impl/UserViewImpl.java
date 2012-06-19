package com.sausageking.ui.impl;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sausageking.R;
import com.sausageking.model.User;
import com.sausageking.ui.OverlayView;
import com.sausageking.ui.UserView;

public class UserViewImpl implements UserView {
  private final UserView.Presenter presenter;
  private final OverlayView overlayView;
  private final View view;
  private final ImageView profileImage;
  private final TextView name;
  private final Button thatIsMe;
  private final Button notMe;
  private final TextView getPointView;
  private final ProgressBar pointProgress;
  private final View confirmRoot;
  private final View unredeemableRoot;
  private final View redeemableRoot;
  private final View redeemingRoot;
  private User user;
  private final Handler handler;

  public UserViewImpl(Context context, final UserView.Presenter presenter,
      OverlayView overlayView) {
    this.presenter = presenter;
    this.overlayView = overlayView;
    this.handler = new Handler();
    view = LayoutInflater.from(context).inflate(R.layout.user,
        overlayView.getUserViewRoot(), false);
    overlayView.getUserViewRoot().addView(view);
    profileImage = (ImageView) view.findViewById(R.id.profileImage);
    name = (TextView) view.findViewById(R.id.name);
    confirmRoot = view.findViewById(R.id.confirmUserRoot);
    unredeemableRoot = view.findViewById(R.id.unredeemableRoot);
    redeemableRoot = view.findViewById(R.id.redeemableRoot);
    redeemingRoot = view.findViewById(R.id.redeemingRoot);
    thatIsMe = (Button) view.findViewById(R.id.thatsMeButton);
    notMe = (Button) view.findViewById(R.id.tryAgainButton);
    getPointView = (TextView) view.findViewById(R.id.getPoints);
    pointProgress = (ProgressBar) view.findViewById(R.id.pointProgress);
    setFont(Typeface.createFromAsset(context.getAssets(), "GeosansLight.ttf"));

    presenter.setView(this);
    notMe.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        presenter.getOverlayPresenter().setToRecognitionView();
      }
    });
    thatIsMe.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        setToUnredeemableMode(user);
      }
    });
  }

  private void setFont(Typeface font) {
    name.setTypeface(font);
    thatIsMe.setTypeface(font);
    notMe.setTypeface(font);
    getPointView.setTypeface(font);
  }

  @Override
  public View toView() {
    return view;
  }

  @Override
  public void setToConfirmUserMode(final User user) {
    handler.post(new Runnable() {

      @Override
      public void run() {
        setUser(user);
        confirmRoot.setVisibility(View.VISIBLE);
        unredeemableRoot.setVisibility(View.GONE);
        redeemableRoot.setVisibility(View.GONE);
        redeemingRoot.setVisibility(View.GONE);
      }
    });

  }

  private void setUser(User user) {
    this.user = user;
    profileImage.setImageBitmap(user.getImage());
    name.setText(user.getName());
  }

  @Override
  public void setToUnredeemableMode(final User user) {
    handler.post(new Runnable() {

      @Override
      public void run() {
        setUser(user);
        confirmRoot.setVisibility(View.GONE);
        unredeemableRoot.setVisibility(View.VISIBLE);
        redeemableRoot.setVisibility(View.GONE);
        redeemingRoot.setVisibility(View.GONE);
      }
    });

  }

  @Override
  public void setToRedeemableMode(final User user) {
    handler.post(new Runnable() {

      @Override
      public void run() {
        setUser(user);
        confirmRoot.setVisibility(View.GONE);
        unredeemableRoot.setVisibility(View.GONE);
        redeemableRoot.setVisibility(View.VISIBLE);
        redeemingRoot.setVisibility(View.GONE);
      }
    });

  }

  @Override
  public void setToRedeemingMode(final User user) {
    handler.post(new Runnable() {

      @Override
      public void run() {
        setUser(user);
        confirmRoot.setVisibility(View.GONE);
        unredeemableRoot.setVisibility(View.GONE);
        redeemableRoot.setVisibility(View.GONE);
        redeemingRoot.setVisibility(View.VISIBLE);
      }
    });

  }

}
