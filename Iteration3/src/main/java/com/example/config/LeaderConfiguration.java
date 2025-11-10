package com.example.config;

import com.example.leader.Follower;
import java.net.URI;
import java.util.List;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Profile("leader")
@Configuration
@ComponentScan(basePackages = {"com.example.leader"})
@EnableConfigurationProperties({LeaderProps.class})
public class LeaderConfiguration {
  @Bean
  List<Follower> followers(LeaderProps props) {
    return props.getFollowerUrls().stream()
        .map(
            baseUrl -> {
              var cfg =
                  org.apache.hc.client5.http.config.RequestConfig.custom()
                      .setConnectionRequestTimeout(
                          Timeout.of(props.getRequest().getConnectionTimeout()))
                      .setResponseTimeout((Timeout.of(props.getRequest().getReadTimeout())))
                      .build();

              var hc =
                  org.apache.hc.client5.http.impl.classic.HttpClients.custom()
                      .setDefaultRequestConfig(cfg)
                      .evictIdleConnections(org.apache.hc.core5.util.TimeValue.ofSeconds(10))
                      .evictExpiredConnections()
                      .build();

              var factory =
                  new org.springframework.http.client.HttpComponentsClientHttpRequestFactory(hc);
              URI uri = URI.create(baseUrl);
              return new Follower(
                  uri.getHost(), RestClient.builder().requestFactory(factory).baseUrl(uri).build());
            })
        .toList();
  }
}
