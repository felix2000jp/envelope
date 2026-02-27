package dev.felix2000jp.envelope.appusers.infrastructure.api;

import dev.felix2000jp.envelope.appusers.domain.exceptions.AppuserAlreadyExistsException;
import dev.felix2000jp.envelope.appusers.domain.exceptions.AppuserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class AppuserExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AppuserExceptionHandler.class);

    @ExceptionHandler(AppuserNotFoundException.class)
    ResponseEntity<ProblemDetail> handleAppuserNotFoundException(AppuserNotFoundException ex) {
        var problemDetails = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());

        log.warn(ex.getMessage(), ex);
        return ResponseEntity.of(problemDetails).build();
    }

    @ExceptionHandler(AppuserAlreadyExistsException.class)
    ResponseEntity<ProblemDetail> handleAppuserConflictException(AppuserAlreadyExistsException ex) {
        var problemDetails = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());

        log.warn(ex.getMessage(), ex);
        return ResponseEntity.of(problemDetails).build();
    }

}
