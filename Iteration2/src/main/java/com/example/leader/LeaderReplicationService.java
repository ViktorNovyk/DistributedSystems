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
import java.util.function.Supplier;
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
    storeMessageService.replicate(message);
    logger.info("Leader replication is successful. {}", message);

    final int waitingCount =
        callFollowersReplication(request, new FollowerReplicationRequest(message, curSequence));

    logger.info("Leader execution is finished. Waited for {} followers.", waitingCount);
  }

  private int callFollowersReplication(
      LeaderReplicationRequest leaderReq, FollowerReplicationRequest followerReq) {
    List<CompletableFuture<ReplicationResult>> futures =
        followers.stream().map(follower -> callFollowerAsync(followerReq, follower)).toList();

    final int waitingCount = leaderReq.getWriteConcern() - 1;
    waitWriteConcern(waitingCount, futures);
    return waitingCount;
  }

  private void waitWriteConcern(
      int waitingCount, List<CompletableFuture<ReplicationResult>> futures) {
    if (waitingCount > 0 && waitingCount < futures.size()) {
      waiting(waitingCount, futures);

    } else if (waitingCount >= futures.size()) {
      logger.info("Waiting for all {} followers to complete replication.", followers.size());
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
  }

  private static void waiting(
      final int waitingCount, final List<CompletableFuture<ReplicationResult>> futures) {
    logger.info("Waiting for {} followers to complete replication.", waitingCount);

    int finishedCount = 0;
    List<CompletableFuture<ReplicationResult>> notFinished = futures;
    while (finishedCount < waitingCount) {
      final var revisited = notFinished.stream().filter(f -> !f.isDone()).toList();
      if (revisited.size() < notFinished.size()) {
        finishedCount = finishedCount + (notFinished.size() - revisited.size());
        notFinished = revisited;
      }
    }
  }

  private CompletableFuture<ReplicationResult> callFollowerAsync(
      final FollowerReplicationRequest followerReq, final Follower follower) {
    return CompletableFuture.supplyAsync(callFollower(followerReq, follower), executorService);
  }

  private Supplier<ReplicationResult> callFollower(
      final FollowerReplicationRequest followerReq, final Follower follower) {
    return () -> {
      ReplicationResult result =
          follower
              .client()
              .post()
              .uri("/follower/messages")
              .body(followerReq)
              .retrieve()
              .body(ReplicationResult.class);

      logger.info("{} Replication result: {}", follower.name(), result);
      return result;
    };
  }

  private record Follower(String name, RestClient client) {}
}
