package com.bigtv.doorkeeper.exception;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.logging.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = JWTVerificationException.class)
    public ResponseEntity<Void> handleAuthorizationException(JWTVerificationException e) {
        Logger.getLogger(getClass().getName()).info("JWT verification error: " + e);
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}
