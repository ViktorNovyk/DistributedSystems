package com.example.follower;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class FollowerExceptionHandler {

  @ExceptionHandler(RandomException.class)
  public ProblemDetail handleException(RandomException e) {
    return ProblemDetail.forStatus(500);
  }
}
