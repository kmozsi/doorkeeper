package com.karanteam.doorkeeper.controller;

import com.karanteam.doorkeeper.enumeration.Role;
import com.karanteam.doorkeeper.service.OfficeMapService;
import com.karanteam.doorkeeper.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.OfficeApi;
import org.openapitools.model.PositionsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;

@Controller
@Slf4j
public class OfficeController implements OfficeApi {

    private final OfficeMapService officeMapService;
    private final JwtService jwtService;

    public OfficeController(OfficeMapService officeMapService, JwtService jwtService) {
        this.officeMapService = officeMapService;
        this.jwtService = jwtService;
    }

    @Override
    public ResponseEntity<PositionsResponse> setPositions(String xToken, @Valid MultipartFile officeMap) {
        String userId = jwtService.parseToken(xToken, Role.HR);
        log.info("Received set positions call with userId: " + userId);
        try {
            BigDecimal posCount = BigDecimal.valueOf(officeMapService.storePositions(officeMap.getBytes()));
            return ResponseEntity.ok(new PositionsResponse().message(posCount));
        } catch (IOException e) {
            return (ResponseEntity<PositionsResponse>) ResponseEntity.badRequest();
        }
    }
}
