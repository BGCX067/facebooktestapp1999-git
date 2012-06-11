package com.sausageking;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.sausageking.capture.CameraManager;
import com.sausageking.capture.FrameProcessor;
import com.sausageking.capture.impl.CameraManagerImpl;
import com.sausageking.capture.impl.FrameProcessorImpl;
import com.sausageking.face.FaceClient;
import com.sausageking.face.impl.FaceClientImpl;
import com.sausageking.ui.CameraPreviewView;
import com.sausageking.ui.OverlayView;
import com.sausageking.ui.UserView;
import com.sausageking.ui.impl.CameraPreviewPresenterImpl;
import com.sausageking.ui.impl.CameraPreviewViewImpl;
import com.sausageking.ui.impl.OverlayPresenterImpl;
import com.sausageking.ui.impl.OverlayViewImpl;
import com.sausageking.ui.impl.UserPresenterImpl;
import com.sausageking.ui.impl.UserViewImpl;

public class FaceActivity extends Activity {
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.main);
    findViewById(R.id.root).setSystemUiVisibility(View.STATUS_BAR_HIDDEN);

    CameraManager cameraManager = new CameraManagerImpl(this);
    CameraPreviewView.Presenter cameraPreviewPresenter = new CameraPreviewPresenterImpl(
        cameraManager);
    FaceClient faceClient = new FaceClientImpl(
        "623a4d00f837174dd39a3674bfa01702", "90572043fa466ca29ac784a8f81c5c74",
        "lotsofspacelessspam", "2");
    OverlayView.Presenter overlayPresenter = new OverlayPresenterImpl();
    OverlayView overlayView = new OverlayViewImpl(this,
        ((ViewGroup) findViewById(R.id.overlay)), overlayPresenter);

    UserView.Presenter userPresenter = new UserPresenterImpl(overlayPresenter);
    UserView userView = new UserViewImpl(this, userPresenter, overlayView);

    FrameProcessor frameProcessor = new FrameProcessorImpl(cameraManager,
        overlayPresenter, cameraPreviewPresenter, userPresenter, faceClient);
    cameraPreviewPresenter.addFrameProcessor(frameProcessor);
    CameraPreviewView cameraPreviewView = new CameraPreviewViewImpl(this,
        cameraPreviewPresenter);

    ((FrameLayout) findViewById(R.id.overlay)).addView(overlayView.toView());
    ((FrameLayout) findViewById(R.id.preview)).addView(cameraPreviewView
        .toView());
  }
}