package com.example.common;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StoreMessageService implements ReplicationService {
  private final MessageRepository repository;

  public StoreMessageService(MessageRepository repository) {
    this.repository = repository;
  }

  @Override
  public void replicate(Message message) {
    repository.addMessage(message);
  }

  @Override
  public List<Message> getMessages() {
    return repository.getMessages();
  }
}
