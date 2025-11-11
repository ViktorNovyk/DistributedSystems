package com.example.leader;

import com.example.common.Message;
import com.example.common.ReplicationResult;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/leader")
public class LeaderController {
  private static final Logger logger = LoggerFactory.getLogger(LeaderController.class);
  private final LeaderReplicationService leaderReplicationService;
  private final HeartbeatService heartbeat;
  private final QuorumService quorumService;

  public LeaderController(
      LeaderReplicationService leaderReplicationService,
      HeartbeatService heartbeat,
      QuorumService quorumService) {
    this.leaderReplicationService = leaderReplicationService;
    this.heartbeat = heartbeat;
    this.quorumService = quorumService;
  }

  @PostMapping(path = "/messages", consumes = "application/json")
  public ReplicationResult addMessage(@RequestBody @Valid LeaderRequest request) {
    logger.info("");
    leaderReplicationService.replicate(
        new LeaderReplicationRequest(request.message(), request.writeConcern()));
    return new ReplicationResult("ASK");
  }

  @GetMapping(path = "/messages", produces = "application/json")
  public Collection<Message> getMessages() {
    return leaderReplicationService.getMessages();
  }

  @GetMapping("/health")
  public QuorumHealth health() {
    return new QuorumHealth(
        heartbeat.snapshot(), quorumService.quorumSize(), quorumService.hasQuorum());
  }

  public record QuorumHealth(
      Map<String, HealthStatus> followers, int quorumSize, boolean hasQuorum) {}
}
