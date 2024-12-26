package com.red.team.taskvisionapp.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.red.team.taskvisionapp.model.dto.request.JwtClaims;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService{

    private static final Logger log = LoggerFactory.getLogger(JwtServiceImpl.class);

    @Override
    public String generateToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256("SECRET");
        try {
            return JWT.create()
                    .withSubject(user.getId())
                    .withClaim("role", user.getRole().name())
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(Instant.now().plusSeconds(3600))
                    .withIssuer("TASK VISION APP")
                    .sign(algorithm);
        }catch (JWTCreationException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating token");
        }
    }

    @Override
    public boolean verifyJwtToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256("SECRET");
            JWTVerifier jwtVerifier = JWT.require(algorithm).withIssuer("TASK VISION APP").build();
            jwtVerifier.verify(parseJwt(token));
            return true;
        }catch (JWTCreationException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid verify Jwt Token");
        }
    }

    @Override
    public JwtClaims getJwtClaims(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256("SECRET");
            JWTVerifier jwtVerifier = JWT.require(algorithm).withIssuer("TASK VISION APP").build();
            DecodedJWT decodedJWT = jwtVerifier.verify(parseJwt(token));
            return JwtClaims.builder()
                    .email(decodedJWT.getSubject())
                    .role(decodedJWT.getClaim("role").asString())
                    .build();
        }catch (JWTVerificationException e){
            log.error("JWT Verification failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Jwt Token");
        }
    }

    private String parseJwt(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return "";
    }
}
