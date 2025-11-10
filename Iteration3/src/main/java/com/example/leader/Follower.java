package com.example.leader;

import org.springframework.web.client.RestClient;

public record Follower(String name, RestClient client) {}
