package com.karanteam.doorkeeper.controller;

import com.karanteam.doorkeeper.service.BookingService;
import com.karanteam.doorkeeper.enumeration.Role;
import com.karanteam.doorkeeper.service.JwtService;
import com.karanteam.doorkeeper.service.OfficeCapacityService;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.DoorApi;
import org.openapitools.model.CapacityBody;
import org.openapitools.model.RegisterResponse;
import org.openapitools.model.StatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class Controller implements DoorApi {

    private final BookingService bookingService;
    private final OfficeCapacityService capacityService;
    private final JwtService jwtService;

    public Controller(BookingService bookingService, OfficeCapacityService capacityService, JwtService jwtService) {
        this.bookingService = bookingService;
        this.capacityService = capacityService;
        this.jwtService = jwtService;
    }

    /**
     * Rest endpoint for office entry based on the user's valid JWT token.
     * @param xToken JWT token with the user's identifier. (required)
     */
    @Override
    public ResponseEntity<Void> entry(String xToken) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE);
        log.info("Received entry call with userId: " + userId);
        bookingService.entry(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Rest endpoint for exiting the office based on the user's valid JWT token.
     * @param xToken JWT token with the user's identifier. (required)
     */
    @Override
    public ResponseEntity<Void> exit(String xToken) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE);
        log.info("Received exit call with userId: " + userId);
        bookingService.exit(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Rest endpoint to get the current status of the user based on valid JWT token.
     * If the user can enter the office, the position is 0, otherwise the position is equal
     * to the number of users that needs to leave before the user can enter the office.
     * @param xToken JWT token with the user's identifier. (required)
     * @return @StatusResponse with the user's position.
     */
    @Override
    public ResponseEntity<StatusResponse> getStatus(String xToken) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE);
        log.info("Received get status call with userId: " + userId);
        return ResponseEntity.ok(bookingService.status(userId));
    }

    /**
     * Rest endpoint to book a place in the office.
     * If the user can enter the office, canEnter flag is true.
     * If it is false, position is equal to the number of users that needs
     * to leave before the user can enter the office.
     * @param xToken JWT token with the user's identifier. (required)
     * @return @RegisterResponse
     */
    @Override
    public ResponseEntity<RegisterResponse> register(String xToken) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE);
        log.info("Received register call with userId: " + userId);
        return ResponseEntity.ok(bookingService.register(userId));
    }

    /**
     * Rest endpoint to set the current capacity of the office.
     * The daily capacity of the office is: capacity * percentage / 100.
     * Only users with @{@link Role#HR} can set these values.
     * @param xToken JWT token with the user's identifier. (required)
     * @param capacityBody Contains the full capacity and the allowed percentage. (required)
     * @return @RegisterResponse
     */
    @Override
    public ResponseEntity<Void> setCapacity(String xToken, CapacityBody capacityBody) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE, Role.HR);
        log.info("Received set capacity call with userId: " + userId);
        capacityService.setCapacity(capacityBody);
        return ResponseEntity.ok().build();
    }
}
