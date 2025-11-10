package com.example.common;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class MessageRepository {
  private final List<Message> messages = new CopyOnWriteArrayList<>();

  public void addMessage(Message message) {
    messages.add(message);
  }

  public List<Message> getMessages() {
    return messages;
  }
}
