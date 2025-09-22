package com.example.follower;

import com.example.common.Message;
import com.example.common.ReplicationService;
import com.example.common.StoreMessageService;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FollowerReplicationService implements ReplicationService {
  private static final Logger logger = LoggerFactory.getLogger(FollowerReplicationService.class);
  private final StoreMessageService storeMessageService;

  public FollowerReplicationService(StoreMessageService storeMessageService) {
    this.storeMessageService = storeMessageService;
    logger.info("FollowerReplicationService initialized");
  }

  @Override
  public void replicate(Message message) {
    sleep(20, 500);

    logger.info("Follower replication is started. {}", message);
    storeMessageService.replicate(message);

    sleep(50, 5000);
     logger.info("Follower replication is successful. {}", message);
  }

  private static void sleep(int from, int to) {
    long delay = ThreadLocalRandom.current().nextLong(from, to);
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Message> getMessages() {
    return storeMessageService.getMessages();
  }
}
