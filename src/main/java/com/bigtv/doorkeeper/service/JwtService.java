package com.bigtv.doorkeeper.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bigtv.doorkeeper.config.JwtConfiguration;
import com.bigtv.doorkeeper.enumeration.Role;
import org.springframework.stereotype.Service;

import static java.util.Arrays.stream;

@Service
public class JwtService {

    private final JwtConfiguration configuration;

    public JwtService(JwtConfiguration configuration) {
        this.configuration = configuration;
    }

    public String parseToken(String token, Role... roles) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(configuration.getSecret());
        JWTVerifier verifier = JWT.require(algorithm)
                .withArrayClaim("roles", stream(roles).map(Role::toString).toArray(String[]::new))
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("userId").asString();
    }
}

