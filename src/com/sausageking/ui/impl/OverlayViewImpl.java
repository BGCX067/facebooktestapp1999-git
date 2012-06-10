package com.sausageking.ui.impl;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sausageking.R;
import com.sausageking.model.User;
import com.sausageking.ui.OverlayView;

public class OverlayViewImpl implements OverlayView {
  private final Handler handler;
  private final Presenter presenter;
  private final View view;
  private final Button saveLocalButton;
  private final Button trainNextFaceButton;
  private final EditText userIdToTrain;
  private final TextView result;
  private final Context context;
  private final Typeface font;
  private final Typeface handWritingFont;
  private final ImageView scanLine;
  private final View midMiddle;
  private final View mid;
  private final ImageView boardImage;
  private boolean scanLineMoving = false;
  private final ViewGroup userRoot;
  private final ViewGroup scanningRoot;

  public OverlayViewImpl(final Context context, ViewGroup root,
      final Presenter presenter) {
    this.context = context;
    font = Typeface.createFromAsset(context.getAssets(), "GeosansLight.ttf");
    handWritingFont = Typeface.createFromAsset(context.getAssets(),
        "aescrawl.ttf");
    handler = new Handler();
    view = LayoutInflater.from(context).inflate(R.layout.overlay, root, false);
    setFont(view);
    saveLocalButton = (Button) view.findViewById(R.id.saveLocal);
    trainNextFaceButton = (Button) view.findViewById(R.id.trainNextFace);
    userIdToTrain = (EditText) view.findViewById(R.id.userIdToTrain);
    result = (TextView) view.findViewById(R.id.result);
    this.presenter = presenter;
    midMiddle = view.findViewById(R.id.midMid);
    userRoot = (ViewGroup) view.findViewById(R.id.userRoot);
    scanningRoot = (ViewGroup) view.findViewById(R.id.mid);
    mid = view.findViewById(R.id.mid);
    scanLine = (ImageView) view.findViewById(R.id.scanLine);
    boardImage = (ImageView) view.findViewById(R.id.boardImage);
    saveLocalButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        presenter.saveLocal();
        // showScanLine();
      }
    });
    trainNextFaceButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        if (userIdToTrain.getText() != null
            && userIdToTrain.getText().length() != 0
            && userIdToTrain.getText().length() >= 3) {
          presenter.trainNextDetectedFace(userIdToTrain.getText().toString());
        } else {
          setResult("Invalid user id");
        }
      }
    });

    presenter.setView(this);
  }

  @Override
  public void showScanLine() {
    handler.post(new Runnable() {

      @Override
      public void run() {
        if (scanLineMoving) {
          return;
        } else {
          scanLineMoving = true;
        }
        scanLine.setX(-59);
        scanLine.setVisibility(View.VISIBLE);
        ObjectAnimator animation = ObjectAnimator.ofFloat(scanLine, "x",
            midMiddle.getRight());

        animation.setDuration(900);
        animation.start();
        animation.addListener(new AnimatorListener() {
          @Override
          public void onAnimationStart(Animator animation) {
          }

          @Override
          public void onAnimationRepeat(Animator animation) {
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            scanLine.setVisibility(View.GONE);
            scanLineMoving = false;
          }

          @Override
          public void onAnimationCancel(Animator animation) {
          }
        });
      }
    });
  }

  void setFont(View view) {
    ((TextView) view.findViewById(R.id.rewardText)).setTypeface(font);
    ((TextView) view.findViewById(R.id.logoText)).setTypeface(font);
    ((TextView) view.findViewById(R.id.messageText)).setTypeface(font);
    ((Button) view.findViewById(R.id.checkinWithEmailButton)).setTypeface(font);
    ((Button) view.findViewById(R.id.firstTimeUserButton)).setTypeface(font);
    ((TextView) view.findViewById(R.id.boardMessage)).setTypeface(font);
  }

  @Override
  public View toView() {
    return view;
  }

  @Override
  public void setResult(final String userId) {
    handler.post(new Runnable() {

      @Override
      public void run() {
        result.setText(userId);
      }
    });
  }

  @Override
  public RectF getFaceArea() {
    return new RectF(midMiddle.getLeft(), mid.getTop(), midMiddle.getRight(),
        mid.getBottom());
  }

  @Override
  public void setToRecognitionView() {
    handler.post(new Runnable() {

      @Override
      public void run() {
        getUserViewRoot().setVisibility(View.GONE);
        getScanningViewRoot().setVisibility(View.VISIBLE);

      }
    });

  }

  @Override
  public void setToUserView(User user) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        getUserViewRoot().setVisibility(View.VISIBLE);
        getScanningViewRoot().setVisibility(View.GONE);
      }
    });
  }

  @Override
  public void setToSignupView() {
    // TODO Auto-generated method stub
  }

  @Override
  public void setToTraningView(User user) {
    // TODO Auto-generated method stub

  }

  @Override
  public ViewGroup getUserViewRoot() {
    return userRoot;
  }

  @Override
  public ViewGroup getScanningViewRoot() {
    return scanningRoot;
  }

}
