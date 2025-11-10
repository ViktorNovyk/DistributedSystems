package com.example.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("follower")
@Configuration
@ComponentScan(basePackages = {"com.example.follower"})
public class FollowerConfiguration {}
