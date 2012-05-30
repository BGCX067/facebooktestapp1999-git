package com.sausageking.face.impl;

import java.io.File;

import android.util.Log;

import com.github.mhendred.face4j.DefaultFaceClient;
import com.github.mhendred.face4j.exception.FaceClientException;
import com.github.mhendred.face4j.exception.FaceServerException;
import com.github.mhendred.face4j.model.Face;
import com.github.mhendred.face4j.model.Guess;
import com.github.mhendred.face4j.model.Photo;
import com.sausageking.face.FaceClient;

public class FaceClientImpl implements FaceClient {
  private final static String TAG = FaceClientImpl.class.getSimpleName();
  private final static int CONFIDENCE_SCORE_THRESHOLD = 50;
  private final com.github.mhendred.face4j.FaceClient apiClient;
  private final String domain;
  private final String merchantId;

  public FaceClientImpl(String apiKey, String apiSecret, String domain,
      String merchantId) {
    this.domain = domain;
    apiClient = new DefaultFaceClient(apiKey, apiSecret);
    this.merchantId = merchantId;
  }

  @Override
  public void addForTraining(File imageFile, String userId)
      throws FaceClientException, FaceServerException {
    long start = System.currentTimeMillis();
    Photo photo = apiClient.detect(imageFile);
    long detected = System.currentTimeMillis();
    Log.d(TAG, "training photo detection time:" + (detected - start));
    Face face = photo.getFace();
    if (face == null) {
      Log.w(TAG, "face not detected");
      return;
    }

    apiClient.saveTags(face.getTID(), getNameSpacedUserId(userId), merchantId);
    Log.d(TAG, "training photo save time:"
        + (System.currentTimeMillis() - detected));
  }

  private String getNameSpacedUserId(String userId) {
    return userId + "@" + domain;
  }

  @Override
  public void train(String userId) throws FaceClientException,
      FaceServerException {
    long start = System.currentTimeMillis();
    apiClient.train(getNameSpacedUserId(userId));
    Log.d(TAG, "training photo training time:"
        + (System.currentTimeMillis() - start));
  }

  @Override
  public Guess recognize(File imageFile) throws FaceClientException,
      FaceServerException {
    long start = System.currentTimeMillis();
    Photo photo = apiClient.recognize(imageFile, "all@" + domain);
    Log.d(TAG, "recognition time:" + (System.currentTimeMillis() - start));
    Face mostLikelyFace = photo.getFace();
    if (mostLikelyFace != null
        && mostLikelyFace.getGuess() != null
        && !mostLikelyFace.getGuess().getUid().equals("Unknown")
        && mostLikelyFace.getGuess().getConfidence() > CONFIDENCE_SCORE_THRESHOLD) {
      Log.d(TAG, mostLikelyFace.getGuess().getUid());
      Log.d(TAG, "recognized: " + mostLikelyFace.getGuess().toString());
      return mostLikelyFace.getGuess();
    } else {
      Log.d(TAG, "can not recognize user.");
      return null;
    }
  }
}
