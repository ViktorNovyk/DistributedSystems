package com.example.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("leader")
@Configuration
@ComponentScan(basePackages = {"com.example.leader"})
@EnableConfigurationProperties({LeaderProps.class})
public class LeaderConfiguration {}
