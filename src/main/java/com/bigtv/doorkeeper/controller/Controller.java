package com.bigtv.doorkeeper.controller;

import com.bigtv.doorkeeper.service.OfficeEntryService;
import com.bigtv.doorkeeper.enumeration.Role;
import com.bigtv.doorkeeper.service.JwtService;
import org.openapitools.api.DoorApi;
import org.openapitools.model.CapacityBody;
import org.openapitools.model.EntryResponse;
import org.openapitools.model.RegisterResponse;
import org.openapitools.model.StatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
public class Controller implements DoorApi {

    private final OfficeEntryService officeEntryService;

    public Controller(OfficeEntryService officeEntryService) {
        this.officeEntryService = officeEntryService;
    }
    @Autowired
    private JwtService jwtService;

    @Override
    public ResponseEntity<EntryResponse> entry(String xToken) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE);
        Logger.getLogger(getClass().getName()).info("Received entry call with userId: " + userId);
        officeEntryService.entry(userId);
        return ResponseEntity.ok(new EntryResponse().permitted(true));
    }

    @Override
    public ResponseEntity<Void> exit(String xToken) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE);
        Logger.getLogger(getClass().getName()).info("Received exit call with userId: " + userId);
        officeEntryService.exit(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StatusResponse> getStatus(String xToken) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE);
        Logger.getLogger(getClass().getName()).info("Received get status call with userId: " + userId);
        return ResponseEntity.ok(new StatusResponse().position(10));
    }

    @Override
    public ResponseEntity<RegisterResponse> register(String xToken) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE);
        Logger.getLogger(getClass().getName()).info("Received register call with userId: " + userId);
        return ResponseEntity.ok(new RegisterResponse().accepted(true).position(10));
    }

    @Override
    public ResponseEntity<Void> setCapacity(String xToken, CapacityBody capacityBody) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE, Role.HR);
        Logger.getLogger(getClass().getName()).info("Received set capacity call with userId: " + userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
