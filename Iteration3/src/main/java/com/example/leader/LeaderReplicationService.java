package com.example.leader;

import com.example.common.*;
import com.example.follower.FollowerReplicationRequest;
import java.util.List;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LeaderReplicationService implements ReplicationService<LeaderReplicationRequest> {
  private static final Logger logger = LoggerFactory.getLogger(LeaderReplicationService.class);
  private final StoreMessageService storeMessageService;
  private final List<Follower> followers;
  private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

  private final FollowerClientService followerClientService;
  private final Sequencer sequencer;
  private final QuorumService quorumService;
  private final HeartbeatService heartbeatService;

  public LeaderReplicationService(
      StoreMessageService storeMessageService,
      FollowerClientService followerClientService,
      List<Follower> followers,
      Sequencer sequencer,
      QuorumService quorumService,
      HeartbeatService heartbeatService) {
    this.storeMessageService = storeMessageService;
    this.followers = followers;
    this.followerClientService = followerClientService;
    this.sequencer = sequencer;
    this.quorumService = quorumService;
    this.heartbeatService = heartbeatService;
    logger.info("LeaderReplicationService initialized");
  }

  @Override
  public List<Message> getMessages() {
    return storeMessageService.getMessages();
  }

  @Override
  public void replicate(LeaderReplicationRequest request) {
    if (!quorumService.hasQuorum()) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No quorum");
    }
    validateWriteConcern(request);
    final Message message = request.getMessage();
    sequencer
        .nextSequence(message)
        .ifPresent(curSequence -> saveAndCallFollowers(request, curSequence));
  }

  private void saveAndCallFollowers(LeaderReplicationRequest request, Long curSequence) {
    final var message = request.getMessage();
    storeMessageService.save(message);
    logger.info("Leader saving is successful. {}", message);

    final var req = new FollowerReplicationRequest(message, curSequence);
    final int waitingCount = callFollowersReplication(request, req);

    logger.info("Leader execution is finished. Waited for {} followers.", waitingCount);
  }

  private void validateWriteConcern(LeaderReplicationRequest request) {
    if (request.getWriteConcern() < 1
        || request.getWriteConcern() > heartbeatService.healthyCount() + 1) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Not enough health nodes to comply with writeConcern");
    }
  }

  private int callFollowersReplication(
      LeaderReplicationRequest leaderReq, FollowerReplicationRequest followerReq) {
    final int waitingCount = leaderReq.getWriteConcern() - 1;
    final CountDownLatch writeConcernLatch = new CountDownLatch(waitingCount);

    for (Follower follower : followers) {
      executorService.submit(
          followerClientService.callFollower(follower, followerReq, writeConcernLatch));
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
