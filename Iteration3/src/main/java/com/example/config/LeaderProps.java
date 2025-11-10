package com.example.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "leader")
public class LeaderProps {
  private List<String> followerUrls;
  private RequestProps request = new RequestProps();

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
}
