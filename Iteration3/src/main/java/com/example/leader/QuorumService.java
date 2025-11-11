package com.example.leader;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuorumService {
    private final HeartbeatService heartbeatService;
    private final List<Follower> followers;

    public QuorumService(HeartbeatService heartbeatService, List<Follower> followers) {
        this.heartbeatService = heartbeatService; this.followers = followers;
    }

    public int quorumSize() {
        int nodesCount = followers.size()+ 1; //+1 means a leader
        return nodesCount / 2 + 1; // adding +1 for majorty
    }

    public boolean hasQuorum() {
        long healthyFollowers = heartbeatService.healthyCount();
        return (healthyFollowers + 1) >= quorumSize(); // +1 for leader
    }
}