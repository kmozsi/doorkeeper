package com.bigtv.doorkeeper.exception;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@RequiredArgsConstructor(staticName = "of")
public class ErrorMessage {
    private final HttpStatus status;
    private final String message;
}
