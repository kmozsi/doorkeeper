package com.bigtv.doorkeeper.exception;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.CONFLICT;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = JWTVerificationException.class)
    public ResponseEntity<Void> handleAuthorizationException(JWTVerificationException e) {
        logger.info("JWT verification error: " + e);
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EntryNotFoundException.class)
    public ResponseEntity<String> handleEntryNotFound(
        EntryNotFoundException exception
    ) {
        logger.info("Entry not found: " + exception);
        return ResponseEntity
            .status(CONFLICT)
            .body("Entry not found");
    }
}
