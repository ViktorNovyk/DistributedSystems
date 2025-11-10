package com.example.leader;

import com.example.common.Message;
import com.example.common.ReplicationRequest;

public class LeaderReplicationRequest extends ReplicationRequest {
  private int writeConcern;

  public LeaderReplicationRequest() {}

  public LeaderReplicationRequest(Message message, int writeConcern) {
    super(message);
    this.writeConcern = writeConcern;
  }

  public int getWriteConcern() {
    return writeConcern;
  }

  public void setWriteConcern(int writeConcern) {
    this.writeConcern = writeConcern;
  }
}
