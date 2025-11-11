package com.example.leader;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class LeaderExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ProblemDetail handleException(ResponseStatusException e) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(e.getStatusCode());
    problemDetail.setDetail(e.getMessage());
    return problemDetail;
  }
}
