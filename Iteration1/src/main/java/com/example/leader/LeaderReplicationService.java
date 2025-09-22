package com.example.leader;

import com.example.common.Message;
import com.example.common.ReplicationResult;
import com.example.common.ReplicationService;
import com.example.common.StoreMessageService;
import com.example.config.LeaderProps;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LeaderReplicationService implements ReplicationService {
  private static final Logger logger = LoggerFactory.getLogger(LeaderReplicationService.class);
  private final StoreMessageService storeMessageService;
  private final List<Follower> followers;
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

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
  public void replicate(Message message) {
    storeMessageService.replicate(message);
    logger.info("Leader replication is successful. {}", message);

    List<CompletableFuture<ReplicationResult>> futures =
        followers.stream().map(follower -> callFollowerAsync(message, follower)).toList();

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    logger.info("All followers replication are successful.");
  }

  private CompletableFuture<ReplicationResult> callFollowerAsync(
      final Message message, final Follower follower) {
    return CompletableFuture.supplyAsync(callFollower(message, follower), executorService);
  }

  private Supplier<ReplicationResult> callFollower(final Message message, final Follower follower) {
    return () -> {
      ReplicationResult result =
          follower
              .client()
              .post()
              .uri("/follower/messages")
              .body(message)
              .retrieve()
              .body(ReplicationResult.class);

      logger.info("{} Replication result: {}", follower.name(), result);
      return result;
    };
  }

  private record Follower(String name, RestClient client) {}
}
