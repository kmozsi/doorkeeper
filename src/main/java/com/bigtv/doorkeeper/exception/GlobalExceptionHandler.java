package com.bigtv.doorkeeper.exception;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.logging.Logger;
import static org.springframework.http.HttpStatus.CONFLICT;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = JWTVerificationException.class)
    public ResponseEntity<Void> handleAuthorizationException(JWTVerificationException e) {
        Logger.getLogger(getClass().getName()).info("JWT verification error: " + e);
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EntryNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleEntryNotFound(
        EntryNotFoundException exception
    ) {
        Logger.getLogger(getClass().getName()).info("Entry not found: " + exception);
        return ResponseEntity
            .status(CONFLICT)
            .body(ErrorMessage.of(CONFLICT, "Entry not found"));
    }
}
