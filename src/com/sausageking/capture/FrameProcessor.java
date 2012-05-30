package com.sausageking.capture;

import java.io.IOException;

public interface FrameProcessor {
  void start();

  void stop();

  void processFrame(byte[] data) throws IOException;

  void setTrainingMode(String userId);

  void setRecognitionMode();

  void setIdleMode();

  void setSaveAFaceMode();

  enum Mode {
    RECOGNITION, TRAINING, IDLE, SAVE_A_FACE
  }

}
