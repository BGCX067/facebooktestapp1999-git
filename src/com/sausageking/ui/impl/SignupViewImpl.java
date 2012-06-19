package com.sausageking.ui.impl;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sausageking.R;
import com.sausageking.ui.OverlayView;
import com.sausageking.ui.SignupView;

public class SignupViewImpl implements SignupView {
  private final SignupView.Presenter presenter;
  private final OverlayView overlayView;
  private final View view;
  private final EditText email;
  private final EditText pin;
  private final EditText name;
  private final Button continueButton;
  private final Button cancelButton;
  private final Handler handler;

  public SignupViewImpl(Context context, final SignupView.Presenter presenter,
      OverlayView overlayView) {
    this.presenter = presenter;
    this.overlayView = overlayView;
    this.handler = new Handler();
    view = LayoutInflater.from(context).inflate(R.layout.signup,
        overlayView.getUserViewRoot(), false);
    overlayView.getSignupViewRoot().addView(view);
    email = (EditText) view.findViewById(R.id.email);
    pin = (EditText) view.findViewById(R.id.pin);
    name = (EditText) view.findViewById(R.id.name);
    continueButton = (Button) view.findViewById(R.id.continueButton);
    cancelButton = (Button) view.findViewById(R.id.cancelButton);
    setFont(Typeface.createFromAsset(context.getAssets(), "GeosansLight.ttf"));
    presenter.setView(this);
  }

  private void setFont(Typeface font) {
    name.setTypeface(font);
    email.setTypeface(font);
    pin.setTypeface(font);
    name.setTypeface(font);
    cancelButton.setTypeface(font);
    continueButton.setTypeface(font);
  }

  @Override
  public View toView() {
    return view;
  }
}
