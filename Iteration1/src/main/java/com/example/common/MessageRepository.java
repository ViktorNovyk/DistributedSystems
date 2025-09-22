package com.example.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MessageRepository {
  private final List<Message> messages = Collections.synchronizedList(new ArrayList<>());

  public void addMessage(Message message) {
    messages.add(message);
  }

  public List<Message> getMessages() {
    return messages;
  }
}
