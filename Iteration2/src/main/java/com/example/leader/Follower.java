package com.example.leader;

import com.example.common.ReplicationResult;
import com.example.follower.FollowerReplicationRequest;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

record Follower(String name, RestClient client) {
  private static final Logger logger = LoggerFactory.getLogger(Follower.class);

  public Runnable callFollower(
      final FollowerReplicationRequest followerReq, CountDownLatch waiter) {
    return () -> {
      try {
        ReplicationResult result =
            client()
                .post()
                .uri("/follower/messages")
                .body(followerReq)
                .retrieve()
                .body(ReplicationResult.class);

        logger.info("{} Replication result: {}", name(), result);
      } catch (Exception e) {
        logger.error("Error in calling follower", e);
      } finally {
        waiter.countDown();
      }
    };
  }
}
