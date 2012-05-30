package com.sausageking.model;

import java.util.List;

import android.graphics.Bitmap;

public class User {
  private int id;
  private String name;
  private Bitmap image;
  private List<Point> points;

  public User(int id, String name, Bitmap image, List<Point> points) {
    this.id = id;
    this.name = name;
    this.image = image;
    this.points = points;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Bitmap getImage() {
    return image;
  }

  public List<Point> getPoints() {
    return points;
  }

}
