package dev.felix2000jp.envelope.system.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Throwable.class)
    ResponseEntity<ProblemDetail> handleThrowable(Throwable ex) {
        var problemDetails = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetails.setTitle("Internal Server Error");
        problemDetails.setDetail("An error occurred while processing the request");

        log.error(ex.getMessage(), ex);
        return ResponseEntity.of(problemDetails).build();
    }

}
