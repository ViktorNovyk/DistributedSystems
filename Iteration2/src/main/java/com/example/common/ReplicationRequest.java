package com.example.common;

public abstract class ReplicationRequest {
  private Message message;

  public ReplicationRequest() {}

  public ReplicationRequest(Message message) {
    this.message = message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }

  public Message getMessage() {
    return message;
  }
}
