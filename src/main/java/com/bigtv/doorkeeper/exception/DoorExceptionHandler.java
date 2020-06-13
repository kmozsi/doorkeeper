package com.bigtv.doorkeeper.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import static org.springframework.http.HttpStatus.CONFLICT;

@ControllerAdvice
public class DoorExceptionHandler {

    @ExceptionHandler(EntryNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleEntryNotFound(
        EntryNotFoundException exception,
        WebRequest request
    ) {
        return ResponseEntity
            .status(CONFLICT)
            .body(ErrorMessage.of(CONFLICT, "Entry not found"));
    }
}
