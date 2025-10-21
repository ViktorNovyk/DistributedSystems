package com.example.follower;

import com.example.common.Message;
import com.example.common.ReplicationService;
import com.example.common.StoreMessageService;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FollowerReplicationService implements ReplicationService<FollowerReplicationRequest> {
  private static final Logger logger = LoggerFactory.getLogger(FollowerReplicationService.class);
  private final StoreMessageService storeMessageService;

  // Buffer for storing messages that are not in order with the latest stored index
  private final Map<Long, Message> buffer = new ConcurrentSkipListMap<>();
  // Set for storing processed message ids to avoid duplicate replication
  private final Set<String> processedMsgIds = Collections.synchronizedSet(new HashSet<>());
  private final ReentrantLock lock = new ReentrantLock();

  private final Duration delay;

  public FollowerReplicationService(
      StoreMessageService storeMessageService, @Value("${follower.delay}") Duration delay) {
    this.storeMessageService = storeMessageService;
    this.delay = delay;
    logger.info("FollowerReplicationService initialized. Sleeping delays {} ms", delay.toMillis());
  }

  @Override
  public void replicate(FollowerReplicationRequest request) {
    final Message message = request.getMessage();
    logger.info(
        "Follower replication is started. msgId={}, seq={}",
        request.getMessage().deduplicationId(),
        request.getSequence());

    sleep();

    try {
      lock.lock();
      if (processedMsgIds.contains(request.getMessage().deduplicationId())) {
        logger.info("Message {} already replicated.", request.getMessage().deduplicationId());
        return;
      }
      processedMsgIds.add(request.getMessage().deduplicationId());
      innerReplicate(request, message);
    } finally {
      lock.unlock();
    }

    logger.info(
        "Follower replication is successful. msgId={}, seq={}",
        request.getMessage().deduplicationId(),
        request.getSequence());
  }

  private void innerReplicate(FollowerReplicationRequest request, Message message) {
    final long latestStoredIndex = storeMessageService.getMessages().size();
    boolean isNextInOrderSequence = latestStoredIndex + 1 == request.getSequence();
    if (isNextInOrderSequence) {
      replicateOrderedMessage(request, message);
    } else {
      buffer.put(request.getSequence(), message);
      logger.info(
          "Adding unordered message id=[{}] seq=[{}] to buffer.",
          message.deduplicationId(),
          request.getSequence());
    }
  }

  private void replicateOrderedMessage(FollowerReplicationRequest request, Message message) {
    storeMessageService.save(message);
    logger.info(
        "Follower replication msgId=[{}] seq=[{}] is successful.",
        message.deduplicationId(),
        request.getSequence());

    if (!buffer.isEmpty()) {
      // Trying to replicate buffered messages starting from the next index
      long nextBufInd = request.getSequence() + 1;

      // Checking buffers while ordered messages exist in the buffer
      while (buffer.containsKey(nextBufInd)) {
        final Message curMsg = buffer.remove(nextBufInd);
        storeMessageService.save(curMsg);
        logger.info(
            "Follower replication from buffer msgId=[{}] seq=[{}] is successful.",
            curMsg.deduplicationId(),
            nextBufInd);
        nextBufInd++;
      }
      logger.info("Finished replication from buffer.");
    } else {
      logger.info("Buffer is empty.");
    }
  }

  private void sleep() {
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
