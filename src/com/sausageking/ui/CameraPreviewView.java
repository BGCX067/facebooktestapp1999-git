package com.sausageking.ui;

import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.View;

import com.sausageking.capture.FrameProcessor;

public interface CameraPreviewView {
    View toView();
    void setPresenter(Presenter presenter);
    void setPreviewPadding(int width, int height);

  interface Presenter extends SurfaceHolder.Callback {
	void setView(CameraPreviewView view);
	Rect getPreviewArea();
	void onPreviewLayoutFinalized(Rect previewArea);
	void addFrameProcessor(FrameProcessor frameProcessor);
    }
}