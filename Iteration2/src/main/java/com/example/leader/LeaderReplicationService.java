package com.example.leader;

import com.example.common.*;
import com.example.config.LeaderProps;
import com.example.follower.FollowerReplicationRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LeaderReplicationService implements ReplicationService<LeaderReplicationRequest> {
  private static final Logger logger = LoggerFactory.getLogger(LeaderReplicationService.class);
  private final StoreMessageService storeMessageService;
  private final List<Follower> followers;
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  private final AtomicLong sequencer = new AtomicLong(1);
  private final ReentrantLock lock = new ReentrantLock();
  private final Map<String, Long> processedMsgIdToSeq = new ConcurrentHashMap<>();

  public LeaderReplicationService(LeaderProps props, StoreMessageService storeMessageService) {
    this.storeMessageService = storeMessageService;
    this.followers =
        props.getFollowerUrls().stream()
            .map(
                baseUrl -> {
                  URI uri = URI.create(baseUrl);
                  return new Follower(uri.getHost(), RestClient.create(uri));
                })
            .toList();
    logger.info("LeaderReplicationService initialized");
  }

  @Override
  public List<Message> getMessages() {
    return storeMessageService.getMessages();
  }

  @Override
  public void replicate(LeaderReplicationRequest request) {
    final Message message = request.getMessage();

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
        return;
      }

      curSequence = sequencer.getAndIncrement();
      processedMsgIdToSeq.put(message.deduplicationId(), curSequence);
    } finally {
      lock.unlock();
    }
    storeMessageService.save(message);
    logger.info("Leader replication is successful. {}", message);

    final int waitingCount =
        callFollowersReplication(request, new FollowerReplicationRequest(message, curSequence));

    logger.info("Leader execution is finished. Waited for {} followers.", waitingCount);
  }

  private int callFollowersReplication(
      LeaderReplicationRequest leaderReq, FollowerReplicationRequest followerReq) {
    final int waitingCount = leaderReq.getWriteConcern() - 1;
    final CountDownLatch writeConcernLatch = new CountDownLatch(waitingCount);

    for (Follower follower : followers) {
      executorService.submit(follower.callFollower(followerReq, writeConcernLatch));
    }

    awaitWriteConcern(writeConcernLatch);
    return waitingCount;
  }

  private static void awaitWriteConcern(CountDownLatch writeConcernLatch) {
    try {
      writeConcernLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
