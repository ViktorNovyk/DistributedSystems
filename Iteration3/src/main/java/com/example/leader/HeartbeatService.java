package com.example.leader;

import static org.slf4j.LoggerFactory.*;

import com.example.config.LeaderProps;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HeartbeatService {
  private static final Logger logger = getLogger(HeartbeatService.class);
  private final Map<String, FollowerHealth> states = new ConcurrentHashMap<>();
  private final Duration suspectedAfter;
  private final Duration unhealthyAfter;

  public HeartbeatService(List<Follower> followers, LeaderProps props) {
    var hb = props.getHeartbeat();
    this.suspectedAfter = hb.getSuspectedAfter();
    this.unhealthyAfter = hb.getUnhealthyAfter();

    followers.forEach(
        f -> states.put(f.name(), new FollowerHealth(f, Instant.EPOCH, HealthStatus.UNHEALTHY)));
  }

  // single threaded. Waits 2sec atfer previous execution
  @Scheduled(fixedRate = 2000)
  public void checkFollowersHealth() {
    var now = Instant.now();
    List<FollowerHealth> stateChanged = new java.util.ArrayList<>();
    for (var health : states.values()) {
      try {
        health.follower().client().get().uri("/follower/health").retrieve().toBodilessEntity();
        health.setLastestSuccess(now);
      } catch (Exception e) {
        // do nothing
      }
      recalculateStateAndGetUpdated(now, health).ifPresent(stateChanged::add);
    }

    if (!stateChanged.isEmpty()) {

      logger.info("Updated states: {}", snapshot());
    }
  }

  private Optional<FollowerHealth> recalculateStateAndGetUpdated(
      Instant now, FollowerHealth health) {
    var since = Duration.between(health.lastSuccess(), now);
    var prevStatus = health.status();
    var newStatus =
        since.compareTo(suspectedAfter) <= 0
            ? HealthStatus.HEALTHY
            : since.compareTo(unhealthyAfter) <= 0
                ? HealthStatus.SUSPECTED
                : HealthStatus.UNHEALTHY;
    health.setStatus(newStatus);
    if (newStatus != prevStatus) {
      logger.info(
          "Follower {} changed status from {} to {}",
          health.follower().name(),
          prevStatus,
          newStatus);
      return Optional.of(health);
    }

    return Optional.empty();
  }

  public Map<String, HealthStatus> snapshot() {
    return states.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().status()));
  }

  public long healthyCount() {
    return states.values().stream().filter(h -> h.status() == HealthStatus.HEALTHY).count();
  }
}
