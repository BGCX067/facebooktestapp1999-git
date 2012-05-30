package com.sausageking.model;

public class Point {
  private double point;
  private long timeInMS;

  public Point(double point, long timeInMS) {
    super();
    this.point = point;
    this.timeInMS = timeInMS;
  }

  public double getPoint() {
    return point;
  }

  public long getTimeInMS() {
    return timeInMS;
  }

}
