package com.example.leader;

import com.example.common.Message;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

@Component
public class Sequencer {
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Sequencer.class);
  private final AtomicLong sequencer = new AtomicLong(1);
  private final ReentrantLock lock = new ReentrantLock();
  private final Map<String, Long> processedMsgIdToSeq = new ConcurrentHashMap<>();

  public Optional<Long> nextSequence(Message message) {
    // Making sequencer and deduplication logic a single threaded
    final long curSequence;
    try {
      lock.lock();
      final Long possibleProcessedSeq = processedMsgIdToSeq.get(message.deduplicationId());
      if (Objects.nonNull(possibleProcessedSeq)) {
        logger.info("Message {} already processed", message.deduplicationId());
        // Assuming that messages eventually were replicated with no communication errors
        // occurred. So no need to call followers
        logger.info("Skipping followers replication.");
        return Optional.empty();
      }

      curSequence = sequencer.getAndIncrement();
      processedMsgIdToSeq.put(message.deduplicationId(), curSequence);
    } finally {
      lock.unlock();
    }
    return Optional.of(curSequence);
  }
}
