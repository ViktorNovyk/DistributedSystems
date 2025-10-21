package com.example;

import com.example.common.Message;
import com.example.common.MessageRepository;
import com.example.common.ReplicationResult;
import com.example.common.StoreMessageService;
import com.example.follower.FollowerReplicationRequest;
import com.example.follower.FollowerReplicationService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.LongStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class FollowerReplicationServiceTest {
  private final FollowerReplicationService service =
      new FollowerReplicationService(new StoreMessageService(new MessageRepository()), Duration.ofSeconds(5L));
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  @Test
  void runFollowerConcurrently() {
    final List<FollowerReplicationRequest> requests =
        LongStream.range(1, 101)
            .mapToObj(
                i ->
                    new FollowerReplicationRequest(
                        new Message("msg-%d".formatted(i), "dedup-%d".formatted(i)), i))
            .toList();
    final List<FollowerReplicationRequest> unorderedRequests = new ArrayList<>(requests);
    Collections.shuffle(unorderedRequests);

    final List<CompletableFuture<ReplicationResult>> futures = new ArrayList<>();
    for (FollowerReplicationRequest request : unorderedRequests) {
      CompletableFuture<ReplicationResult> future =
          CompletableFuture.supplyAsync(
              () -> {
                service.replicate(request);
                return new ReplicationResult("ASK");
              },
              executorService);
      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    List<String> actual = service.getMessages().stream().map(Message::value).toList();
    List<String> expected = requests.stream().map(r -> r.getMessage().value()).toList();
    Assertions.assertThat(actual).containsExactlyElementsOf(expected);
  }
}
