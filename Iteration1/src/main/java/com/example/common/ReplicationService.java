package com.example.common;

import java.util.List;

public interface ReplicationService {
  void replicate(Message message);

  List<Message> getMessages();
}
