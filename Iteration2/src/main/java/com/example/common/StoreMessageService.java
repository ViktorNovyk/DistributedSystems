package com.example.common;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StoreMessageService {
  private static final Logger logger = LoggerFactory.getLogger(StoreMessageService.class);
  private final MessageRepository repository;

  public StoreMessageService(MessageRepository repository) {
    this.repository = repository;
  }

  public void replicate(Message message) {
    repository.addMessage(message);
  }

  public List<Message> getMessages() {
    return repository.getMessages();
  }
}
