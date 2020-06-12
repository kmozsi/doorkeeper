package com.bigtv.doorkeeper.controller;

import io.swagger.annotations.ApiParam;
import org.openapitools.api.DoorApi;

import org.openapitools.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class Controller implements DoorApi {

    @GetMapping("/api")
    public String index() {
        return "Skeleton";
    }


    @Override
    public ResponseEntity<EntryResponse> entry(
        @ApiParam(value = "" ,required=true)
        @RequestHeader(value="X-Token", required=true) String xToken
    ) {
        return ResponseEntity.ok(new EntryResponse().permitted(true));
    }

    @Override
    public ResponseEntity<Void> exit(
        @ApiParam(value = "" ,required=true)
        @RequestHeader(value="X-Token", required=true) String xToken
    ) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StatusResponse> getStatus(
        @ApiParam(value = "" ,required=true)
        @RequestHeader(value="X-Token", required=true) String xToken
     ) {
        return ResponseEntity.ok(new StatusResponse().position(10));
    }

    @Override
    public ResponseEntity<RegisterResponse> register(
        @ApiParam(value = "" ,required=true)
        @RequestHeader(value="X-Token", required=true) String xToken
    ) {
        return ResponseEntity.ok(new RegisterResponse().accepted(true).position(10));
    }

    @Override
    public ResponseEntity<Void> setCapacity(
        @ApiParam(value = "" ,required=true)
        @RequestHeader(value="X-Token", required=true) String xToken,
        @ApiParam(value = "Optional description in *Markdown*" ,required=true)
        @Valid @RequestBody CapacityBody capacityBody
    ) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
