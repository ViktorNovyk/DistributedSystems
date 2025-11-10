package com.example.common;

import java.util.List;

public interface ReplicationService<R extends ReplicationRequest> {
  void replicate(R request);

  List<Message> getMessages();
}
