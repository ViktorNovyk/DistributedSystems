package com.example.leader;

import java.time.Instant;

public class FollowerHealth {
  private final Follower follower;
  private Instant lastestSuccess;
  private HealthStatus status;

  public FollowerHealth(Follower follower, Instant lastestSuccess, HealthStatus status) {
    this.follower = follower;
    this.lastestSuccess = lastestSuccess;
    this.status = status;
  }

  public Follower follower() {
    return follower;
  }

  public Instant lastSuccess() {
    return lastestSuccess;
  }

  public HealthStatus status() {
    return status;
  }

  public void setLastestSuccess(Instant t) {
    this.lastestSuccess = t;
  }

  public void setStatus(HealthStatus s) {
    this.status = s;
  }

  @Override
  public String toString() {
    return "FollowerHealth{"
        + "follower="
        + follower
        + ", lastestSuccess="
        + lastestSuccess
        + ", status="
        + status
        + '}';
  }
}
