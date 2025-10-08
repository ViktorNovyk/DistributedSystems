package com.example.follower;

import com.example.common.Message;
import com.example.common.ReplicationRequest;

public class FollowerReplicationRequest extends ReplicationRequest {
  private Long sequence;

  public FollowerReplicationRequest() {}

  public FollowerReplicationRequest(Message message, Long sequence) {
    super(message);
    this.sequence = sequence;
  }

  public Long getSequence() {
    return sequence;
  }

  public void setSequence(Long sequence) {
    this.sequence = sequence;
  }
}
