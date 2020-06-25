package com.karanteam.doorkeeper.controller;

import com.karanteam.doorkeeper.enumeration.Role;
import com.karanteam.doorkeeper.service.JwtService;
import com.karanteam.doorkeeper.service.VipService;
import java.net.URI;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.VipApi;
import org.openapitools.model.VipUser;
import org.openapitools.model.VipUsers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@Slf4j
public class VipController implements VipApi {

    private final VipService vipService;
    private final JwtService jwtService;

    public VipController(VipService vipService, JwtService jwtService) {
        this.vipService = vipService;
        this.jwtService = jwtService;
    }

    @Override
    public ResponseEntity<Void> addVip(String xToken, @Valid VipUser vipUser) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE, Role.HR);
        log.info("Receiveed add vip call with userId: " + userId);
        vipService.addVipUser(vipUser.getUserId());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("").buildAndExpand().toUri();
        return ResponseEntity.created(uri).build();
    }

    @Override
    public ResponseEntity<Void> deleteVip(String xToken, String vipUserId) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE, Role.HR);
        log.info("Received delete vip call with userId: " + userId);
        if (vipService.deleteVip(vipUserId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<VipUsers> getVips(String xToken) {
        String userId = jwtService.parseToken(xToken, Role.EMPLOYEE, Role.HR);
        log.info("Received get vips call with userId: " + userId);
        VipUsers users = new VipUsers().vipUsers(vipService.getVipUserIds());
        return ResponseEntity.ok(users);
    }
}
