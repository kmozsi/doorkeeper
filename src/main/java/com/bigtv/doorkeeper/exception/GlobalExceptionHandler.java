package com.bigtv.doorkeeper.exception;

import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.CONFLICT;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = JWTVerificationException.class)
    public ResponseEntity<Void> handleAuthorizationException(JWTVerificationException e) {
        log.info("JWT verification error: " + e);
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EntryNotFoundException.class)
    public ResponseEntity<String> handleEntryNotFound(
        EntryNotFoundException exception
    ) {
        log.info("Entry not found: " + exception);
        return ResponseEntity
            .status(CONFLICT)
            .body("Entry not found");
    }
}
