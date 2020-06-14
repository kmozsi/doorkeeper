package com.bigtv.doorkeeper.controller;

import com.bigtv.doorkeeper.service.BookingService;
import com.bigtv.doorkeeper.enumeration.Role;
import com.bigtv.doorkeeper.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.DoorApi;
import org.openapitools.model.CapacityBody;
import org.openapitools.model.EntryResponse;
import org.openapitools.model.RegisterResponse;
import org.openapitools.model.StatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class Controller implements DoorApi {

    private final BookingService bookingService;
    private final JwtService jwtService;

    public Controller(BookingService bookingService, JwtService jwtService) {
        this.bookingService = bookingService;
        this.jwtService = jwtService;
    }

    @Override
    public ResponseEntity<EntryResponse> entry(String xToken) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE);
        log.info("Received entry call with userId: " + userId);
        return ResponseEntity.ok(bookingService.entry(userId));
    }

    @Override
    public ResponseEntity<Void> exit(String xToken) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE);
        log.info("Received exit call with userId: " + userId);
        bookingService.exit(userId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<StatusResponse> getStatus(String xToken) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE);
        log.info("Received get status call with userId: " + userId);
        return ResponseEntity.ok(bookingService.status(userId));
    }

    @Override
    public ResponseEntity<RegisterResponse> register(String xToken) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE);
        log.info("Received register call with userId: " + userId);
        return ResponseEntity.ok(bookingService.register(userId));
    }

    @Override
    public ResponseEntity<Void> setCapacity(String xToken, CapacityBody capacityBody) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE, Role.HR);
        log.info("Received set capacity call with userId: " + userId);
        return ResponseEntity.ok().build();
    }
}
