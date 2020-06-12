package com.bigtv.doorkeeper.controller;

import org.openapitools.api.DoorApi;
import org.openapitools.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class Controller implements DoorApi {

    @GetMapping("/api")
    public String index() {
        return "Skeleton";
    }


    @Override
    public ResponseEntity<EntryResponse> entry(String xToken) {
        return ResponseEntity.ok(new EntryResponse().permitted(true));
    }

    @Override
    public ResponseEntity<Void> exit(String xToken) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StatusResponse> getStatus(String xToken) {
        return ResponseEntity.ok(new StatusResponse().position(10));
    }

    @Override
    public ResponseEntity<RegisterResponse> register(String xToken) {
        return ResponseEntity.ok(new RegisterResponse().accepted(true).position(10));
    }

    @Override
    public ResponseEntity<Void> setCapacity(String xToken, CapacityBody capacityBody) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
