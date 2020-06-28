package com.karanteam.doorkeeper.controller;

import com.karanteam.doorkeeper.enumeration.Role;
import com.karanteam.doorkeeper.service.JwtService;
import com.karanteam.doorkeeper.service.AdminService;
import com.karanteam.doorkeeper.service.OfficeMapService;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.AdminApi;
import org.openapitools.model.CapacityBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
public class AdminController implements AdminApi {

    private final AdminService adminService;
    private final OfficeMapService officeMapService;
    private final JwtService jwtService;

    public AdminController(AdminService adminService, OfficeMapService officeMapService, JwtService jwtService) {
        this.adminService = adminService;
        this.officeMapService = officeMapService;
        this.jwtService = jwtService;
    }

    /**
     * Rest endpoint to set the current capacity of the office.
     * The daily capacity of the office is: capacity * percentage / 100.
     * Only users with @{@link Role#HR} can set these values.
     *
     * @param xToken       JWT token with the user's identifier. (required)
     * @param capacityBody Contains the full capacity and the allowed percentage. (required)
     * @return @RegisterResponse
     */
    @Override
    public ResponseEntity<Void> setCapacity(String xToken, CapacityBody capacityBody) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE, Role.HR);
        log.info("Received set capacity call with userId: " + userId);
        adminService.setCapacity(capacityBody);
        officeMapService.recalculatePositions();
        return ResponseEntity.ok().build();
    }
}
