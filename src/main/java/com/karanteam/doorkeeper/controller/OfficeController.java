package com.karanteam.doorkeeper.controller;

import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.enumeration.Role;
import com.karanteam.doorkeeper.service.OfficeMapService;
import com.karanteam.doorkeeper.service.JwtService;
import com.karanteam.doorkeeper.service.OfficePositionService;
import java.util.Optional;
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
    private final OfficePositionService officePositionService;
    private final JwtService jwtService;

    public OfficeController(OfficeMapService officeMapService,
        OfficePositionService officePositionService,
        JwtService jwtService) {
        this.officeMapService = officeMapService;
        this.officePositionService = officePositionService;
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

    @Override
    public ResponseEntity<byte[]> getPosition(String xToken, Integer id) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE);
        log.info("Received get position call with userId: " + userId);
        Optional<OfficePosition> position = officePositionService.findById(id);
        if (position.isPresent()) {
            try {
                byte[] content =  officeMapService.markPosition(position.get());
                return ResponseEntity.ok(content);
            } catch (IOException e) {
                return (ResponseEntity<byte[]>) ResponseEntity.badRequest();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<byte[]> getLayout(String xToken) {
        String userId = jwtService.parseToken(xToken, Role.HR);
        log.info("Received get layout call with userId: " + userId);
        try {
            byte[] content =  officeMapService.getLayout();
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            return (ResponseEntity<byte[]>) ResponseEntity.badRequest();
        }
    }
}
