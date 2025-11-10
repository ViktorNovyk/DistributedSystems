package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.common", "com.example.config"})
public class Iteration3App {
  public static void main(String[] args) {
    SpringApplication.run(Iteration3App.class, args);
  }
}
