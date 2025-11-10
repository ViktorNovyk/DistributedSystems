package com.example.leader;

import static org.slf4j.LoggerFactory.*;

import com.example.common.ReplicationResult;
import com.example.follower.FollowerReplicationRequest;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
public class FollowerClientService {
  private static final Logger logger = getLogger(FollowerClientService.class);
  private static final RetryTemplate retry =
      RetryTemplate.builder()
          .retryOn(org.springframework.web.client.RestClientException.class)
          .maxAttempts(Integer.MAX_VALUE)
          .exponentialBackoff(3000L, 1.5, 15000L)
          .build();

  public Runnable callFollower(
      final Follower follower,
      final FollowerReplicationRequest followerReq,
      CountDownLatch waiter) {
    return () -> {
      try {
        ReplicationResult result = retry.execute(retryableCall(follower, followerReq));
        logger.info("{} Replication result: {}", follower.name(), result);
      } finally {
        waiter.countDown();
      }
    };
  }

  private static RetryCallback<ReplicationResult, RuntimeException> retryableCall(
      Follower follower, FollowerReplicationRequest followerReq) {
    return ctx -> {
      try {
        return follower
            .client()
            .post()
            .uri("/follower/messages")
            .body(followerReq)
            .retrieve()
            .body(ReplicationResult.class);
      } catch (Exception e) {
        logger.error(
            "Error in calling follower {}. Retry {}. Error {}",
            follower.name(),
            ctx.getRetryCount(),
            e.getMessage());
        throw e;
      }
    };
  }
}
