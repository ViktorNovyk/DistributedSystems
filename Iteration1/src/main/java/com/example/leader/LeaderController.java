package com.example.leader;

import com.example.common.Message;
import com.example.common.ReplicationResult;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/leader")
public class LeaderController {
  private static final Logger logger = LoggerFactory.getLogger(LeaderController.class);
  private final LeaderReplicationService leaderReplicationService;

  public LeaderController(LeaderReplicationService leaderReplicationService) {
    this.leaderReplicationService = leaderReplicationService;
  }

  @PostMapping(path = "/messages", consumes = "application/json")
  public ReplicationResult addMessage(@RequestBody Message message) {
    logger.info("");
    leaderReplicationService.replicate(message);
    return new ReplicationResult("ASK");
  }

  @GetMapping(path = "/messages", produces = "application/json")
  public Collection<Message> getMessages() {
    return leaderReplicationService.getMessages();
  }
}
