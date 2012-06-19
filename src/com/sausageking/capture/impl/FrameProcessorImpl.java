package com.sausageking.capture.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import com.github.mhendred.face4j.exception.FaceClientException;
import com.github.mhendred.face4j.exception.FaceServerException;
import com.github.mhendred.face4j.model.Guess;
import com.sausageking.capture.CameraManager;
import com.sausageking.capture.FrameProcessor;
import com.sausageking.face.FaceClient;
import com.sausageking.model.User;
import com.sausageking.ui.CameraPreviewView;
import com.sausageking.ui.OverlayView;
import com.sausageking.ui.UserView;

public class FrameProcessorImpl implements FrameProcessor {
  private static final String TAG = FrameProcessorImpl.class.getSimpleName();
  private static int NUM_TRAINING_FACES = 5;
  private final int minProcessIntervalInMs;
  private final ThreadPoolExecutor executor;
  private final AtomicLong lastTaskTime;
  private final CameraManager cameraManager;
  private final OverlayView.Presenter overlayPresenter;
  private final UserView.Presenter userPresenter;
  private final CameraPreviewView.Presenter cameraPreviewPresenter;
  private final UserModeTrigger userModeTrigger;
  private final FaceClient faceClient;
  private int nextNFacesForTraining;
  private Mode mode;
  private String userIdToTrainFor;

  // cache
  private final Map<Pair<Integer, Integer>, FaceDetector> widthHeightToFaceDetectorMap = new HashMap<Pair<Integer, Integer>, FaceDetector>();
  private final Face[] faces = new Face[1];

  public FrameProcessorImpl(CameraManager cameraManager,
      OverlayView.Presenter overlayPresenter,
      CameraPreviewView.Presenter cameraPreviewPresenter,
      UserView.Presenter userPresenter, FaceClient faceClient) {
    this(cameraManager, 1000, 1, 1, 100, 1, overlayPresenter,
        cameraPreviewPresenter, userPresenter, faceClient);
  }

  public FrameProcessorImpl(CameraManager cameraManager,
      int minProcessIntervalInMs, int taskPoolCoreSize, int taskPoolMaxSize,
      int keepIdleAliveTime, int taskQueueMaxSize,
      OverlayView.Presenter overlayPresenter,
      CameraPreviewView.Presenter cameraPreviewPresenter,
      UserView.Presenter userPresenter, FaceClient faceClient) {
    this.faceClient = faceClient;
    this.cameraManager = cameraManager;
    this.minProcessIntervalInMs = minProcessIntervalInMs;
    this.overlayPresenter = overlayPresenter;
    this.userPresenter = userPresenter;
    overlayPresenter.setFrameProcessor(this);
    this.cameraPreviewPresenter = cameraPreviewPresenter;
    mode = Mode.RECOGNITION;
    lastTaskTime = new AtomicLong(0);
    executor = new ThreadPoolExecutor(taskPoolCoreSize, taskPoolMaxSize,
        keepIdleAliveTime, TimeUnit.MILLISECONDS,
        new ArrayBlockingQueue<Runnable>(taskQueueMaxSize),
        new DiscardOldestPolicy());
    userModeTrigger = new UserModeTrigger(2, 15 * 1000);
  }

  @Override
  public void start() {
    cameraManager
        .addContinuousPreviewImageCallback(new CameraManager.ImageCallback() {
          @Override
          public void onImage(byte[] data) {
            long timeSinceLastTaskExecution = System.currentTimeMillis()
                - lastTaskTime.get();
            if (timeSinceLastTaskExecution < minProcessIntervalInMs) {
              return;
            }
            executor.execute(new FrameProcessorRunnable(data.clone()));
          }
        });
  }

  @Override
  public void stop() {
    cameraManager.stopPreview();
    executor.shutdown();
  }

  private class FrameProcessorRunnable implements Runnable {
    private final byte[] data;

    public FrameProcessorRunnable(byte[] data) {
      this.data = data;
    }

    @Override
    public void run() {
      if (executor.isShutdown()) {
        Log.d(TAG, "Process stopped, drop executing tasks");
        return;
      }
      lastTaskTime.set(System.currentTimeMillis());
      processFrame(data);
    }
  }

  @Override
  public void processFrame(byte[] data) {
    if (mode == Mode.IDLE) {
      return;
    }
    long now = System.currentTimeMillis();
    Point previewSize = cameraManager.getPreviewSize();
    int width = previewSize.x;
    int height = previewSize.y;
    YuvImage yuvImage = new YuvImage(data, cameraManager.getPreviewFormat(),
        width, height, null);
    ByteArrayOutputStream jpegOut = new ByteArrayOutputStream();
    Rect faceAreaInPreviewImage = getFaceAreaInPreviewImage();
    yuvImage.compressToJpeg(new Rect(faceAreaInPreviewImage.left,
        faceAreaInPreviewImage.top, faceAreaInPreviewImage.right,
        faceAreaInPreviewImage.bottom), 50, jpegOut);
    byte[] jpegBytes = jpegOut.toByteArray();
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inPreferredConfig = Bitmap.Config.RGB_565;
    Bitmap image = BitmapFactory.decodeByteArray(jpegBytes, 0,
        jpegBytes.length, opts);
    Log.d(TAG, "image conversion: " + (System.currentTimeMillis() - now));
    now = System.currentTimeMillis();
    FaceDetector faceDetector = getFaceDetector(image.getWidth(),
        image.getHeight());
    int numFaces = faceDetector.findFaces(image, faces);
    Log.d(TAG, "face detection: " + (System.currentTimeMillis() - now));
    Log.d(TAG, numFaces + " face detectd");
    if (numFaces == 0) {
      return;
    }
    if (!isFaceInRange(faces[0], image.getWidth(), image.getHeight())) {
      return;
    }

    overlayPresenter.showScanLine();
    if (numFaces != 0) {
      switch (mode) {
      case RECOGNITION:
        recognize(jpegBytes);
        break;
      case TRAINING:
        train(jpegBytes);
        break;
      case SAVE_A_FACE:
        try {
          saveFrame(jpegBytes);
        } catch (IOException e) {
          e.printStackTrace();
        }
        mode = Mode.RECOGNITION;
        break;
      default:
      }
    }
  }

  private boolean isFaceInRange(Face face, int width, int height) {
    float eyeDistance = face.eyesDistance();
    PointF midPoint = new PointF();
    face.getMidPoint(midPoint);
    RectF faceRect = new RectF((midPoint.x - 1 * eyeDistance),
        (midPoint.y - 1 * eyeDistance), (midPoint.x + 1 * eyeDistance),
        midPoint.y + (float) (eyeDistance * 1.29));
    long faceArea = (int) faceRect.width() * (int) faceRect.height();
    long boundingBoxArea = width * height;
    boolean faceCloseEnough = faceArea >= boundingBoxArea * 0.3;
    return new RectF(0, 0, width, height).contains(faceRect) && faceCloseEnough;
  }

  private void recognize(byte[] jpegBytes) {
    File file = null;
    try {
      file = saveFrame(jpegBytes);
      Guess guess = faceClient.recognize(file);
      if (guess != null) {
        String userId = guess.getUid();
        userId = userId.substring(0, userId.indexOf("@"));
        FrameProcessorImpl.this.overlayPresenter.setResult(userId
            + " (confidence: " + guess.getConfidence() + ")");
        if (userModeTrigger.shouldTrigger(userId)) {
          FrameProcessorImpl.this.overlayPresenter.setResult(userId
              + " confirmed");
          setIdleMode();
          Bitmap profileImage = BitmapFactory.decodeFile(file.getPath());
          overlayPresenter.setToUserView();
          userPresenter.setToConfirmUserMode(new User(1, userId, profileImage,
              null));
        }
      } else {
        FrameProcessorImpl.this.overlayPresenter
            .setResult("Can not recognize user");
      }
    } catch (FaceClientException e) {
      e.printStackTrace();
    } catch (FaceServerException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (file != null) {
        file.delete();
      }
    }
  }

  private void train(byte[] jpegBytes) {
    if (nextNFacesForTraining > 0) {
      nextNFacesForTraining--;
      overlayPresenter.setResult("Collecting face images: "
          + (NUM_TRAINING_FACES - nextNFacesForTraining) + "/"
          + NUM_TRAINING_FACES);
      File file = null;
      try {
        file = saveFrame(jpegBytes);
        faceClient.addForTraining(file, userIdToTrainFor);
        if (nextNFacesForTraining == 0) {
          overlayPresenter.setResult("Start training...");
          faceClient.train(userIdToTrainFor);
          overlayPresenter.setResult("Finished training");
          mode = Mode.RECOGNITION;
        }
      } catch (FaceClientException e) {
        e.printStackTrace();
      } catch (FaceServerException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (file != null) {
          file.delete();
        }
      }
    }
  }

  private File saveFrame(byte[] jpegBytes) throws IOException {
    File imageStorageDir = new File(
        Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "face");
    if (!imageStorageDir.exists()) {
      if (!imageStorageDir.mkdirs()) {
        String message = String.format("Failed to create directory %s",
            imageStorageDir.getPath());
        throw new IOException(message);
      }
    }
    String file = String.format("%s%s%s.jpg", imageStorageDir.getPath(),
        File.separator,
        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
    File output = new File(file);
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(output);
      fos.write(jpegBytes);
    } finally {
      if (fos != null) {
        fos.close();
      }
    }
    return output;
  }

  private FaceDetector getFaceDetector(int width, int height) {
    Pair<Integer, Integer> key = new Pair<Integer, Integer>(width, height);
    FaceDetector faceDetector;
    if (widthHeightToFaceDetectorMap.containsKey(key)) {
      faceDetector = widthHeightToFaceDetectorMap.get(key);
    } else {
      faceDetector = new FaceDetector(width, height, 1);
      widthHeightToFaceDetectorMap.put(key, faceDetector);
    }
    return faceDetector;
  }

  @Override
  public void setTrainingMode(String userId) {
    if (userId == null || userId.isEmpty()) {
      return;
    }
    mode = Mode.TRAINING;
    nextNFacesForTraining = NUM_TRAINING_FACES;
    this.userIdToTrainFor = userId;
  }

  @Override
  public void setRecognitionMode() {
    mode = Mode.RECOGNITION;
  }

  @Override
  public void setIdleMode() {
    mode = Mode.IDLE;
  }

  @Override
  public void setSaveAFaceMode() {
    mode = Mode.SAVE_A_FACE;
  }

  private Rect getFaceAreaInPreviewImage() {
    Rect previewArea = cameraPreviewPresenter.getPreviewArea();
    RectF faceArea = overlayPresenter.getFaceArea();
    float leftRatio = faceArea.left / previewArea.width();
    float topRatio = faceArea.top / previewArea.height();
    float rightRatio = faceArea.right / previewArea.width();
    float bottomRatio = faceArea.bottom / previewArea.height();
    int previewWidth = cameraManager.getPreviewSize().x;
    int previewHeight = cameraManager.getPreviewSize().y;

    Rect rect = new Rect((int) (leftRatio * previewWidth),
        (int) (topRatio * previewHeight), (int) (rightRatio * previewWidth),
        (int) (bottomRatio * previewHeight));
    Log.i("blah", "face in image" + rect.toString() + ", preview size: "
        + cameraManager.getPreviewSize() + ", faceArea: " + faceArea.toString()
        + ", previewArea: " + previewArea.toString());

    return rect;
  }

  class UserModeTrigger {
    private final int minConsecutiveHit;
    private final long maxTimeForLastNHitInMs;

    private String lastRecognizedUser;
    private int consecutiveUserHit;
    private List<Long> hitTime;

    public UserModeTrigger(int minConsecutiveHit, long maxTimeForLastNHitInMs) {
      this.maxTimeForLastNHitInMs = maxTimeForLastNHitInMs;
      this.minConsecutiveHit = minConsecutiveHit;
      consecutiveUserHit = 0;
      hitTime = new ArrayList<Long>();
      lastRecognizedUser = "";
    }

    public boolean shouldTrigger(String user) {
      long now = System.currentTimeMillis();
      if (!user.equals(lastRecognizedUser)) {
        lastRecognizedUser = user;
        consecutiveUserHit = 1;
        hitTime.clear();
        hitTime.add(now);
        return false;
      } else {
        consecutiveUserHit++;
        hitTime.add(now);
        long timeForLastNHits = hitTime
            .get(minConsecutiveHit > hitTime.size() ? 0 : hitTime.size()
                - minConsecutiveHit);
        if (consecutiveUserHit >= minConsecutiveHit
            && (now - timeForLastNHits) <= maxTimeForLastNHitInMs) {
          lastRecognizedUser = "";
          consecutiveUserHit = 0;
          hitTime.clear();
          return true;
        } else {
          return false;
        }
      }
    }
  }
}