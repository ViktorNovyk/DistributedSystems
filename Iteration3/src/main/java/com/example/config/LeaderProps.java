package com.example.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "leader")
public class LeaderProps {
  private List<String> followerUrls;
  private RequestProps request = new RequestProps();
  private HeartbeatProps heartbeat = new HeartbeatProps();

  public HeartbeatProps getHeartbeat() {
    return heartbeat;
  }

  public void setHeartbeat(HeartbeatProps heartbeat) {
    this.heartbeat = heartbeat;
  }

  public static class RequestProps {
    private Duration connectionTimeout;
    private Duration readTimeout;

    public Duration getConnectionTimeout() {
      return connectionTimeout;
    }

    public void setConnectionTimeout(Duration connectionTimeout) {
      this.connectionTimeout = connectionTimeout;
    }

    public Duration getReadTimeout() {
      return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
      this.readTimeout = readTimeout;
    }
  }

  public List<String> getFollowerUrls() {
    return followerUrls;
  }

  public void setFollowerUrls(List<String> followerUrls) {
    this.followerUrls = followerUrls;
  }

  public RequestProps getRequest() {
    return request;
  }

  public void setRequest(RequestProps request) {
    this.request = request;
  }

  public static class HeartbeatProps {
    private Duration suspectedAfter = Duration.ofSeconds(5);
    private Duration unhealthyAfter = Duration.ofSeconds(15);

    public Duration getSuspectedAfter() {
      return suspectedAfter;
    }

    public void setSuspectedAfter(Duration suspectedAfter) {
      this.suspectedAfter = suspectedAfter;
    }

    public Duration getUnhealthyAfter() {
      return unhealthyAfter;
    }

    public void setUnhealthyAfter(Duration unhealthyAfter) {
      this.unhealthyAfter = unhealthyAfter;
    }
  }
}
