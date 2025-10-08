package com.example.follower;

import com.example.common.Message;
import com.example.common.ReplicationResult;
import java.util.Collection;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/follower")
public class FollowerController {
  private final FollowerReplicationService followerReplicationService;

  public FollowerController(FollowerReplicationService followerReplicationService) {
    this.followerReplicationService = followerReplicationService;
  }

  @PostMapping(path = "/messages", consumes = "application/json", produces = "application/json")
  public ReplicationResult addMessage(@RequestBody FollowerReplicationRequest request) {
    followerReplicationService.replicate(request);
    return new ReplicationResult("ASK");
  }

  @GetMapping(path = "/messages", produces = "application/json")
  public Collection<Message> getMessages() {
    return followerReplicationService.getMessages();
  }
}
