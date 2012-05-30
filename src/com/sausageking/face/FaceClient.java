package com.sausageking.face;

import java.io.File;

import com.github.mhendred.face4j.exception.FaceClientException;
import com.github.mhendred.face4j.exception.FaceServerException;
import com.github.mhendred.face4j.model.Guess;

public interface FaceClient {
  void addForTraining(File imageFile, String userId)
      throws FaceClientException, FaceServerException;

  void train(String userId) throws FaceClientException, FaceServerException;

  Guess recognize(File imageFile) throws FaceClientException,
      FaceServerException;
}