package com.example.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "leader")
public class LeaderProps {
  private List<String> followerUrls;

  public List<String> getFollowerUrls() {
    return followerUrls;
  }

  public void setFollowerUrls(List<String> followerUrls) {
    this.followerUrls = followerUrls;
  }
}
