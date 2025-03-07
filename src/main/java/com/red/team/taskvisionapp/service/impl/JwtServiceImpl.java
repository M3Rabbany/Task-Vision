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
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService{
    private static final Logger log = LoggerFactory.getLogger(JwtServiceImpl.class);
    private static final String SECRET = "SECRET";
    private static final String ISSUER = "TASK VISION APP";

    @Override
    public String generateToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET);
        try {
            return JWT.create()
                    .withSubject(user.getEmail()) // Use email as subject
                    .withClaim("role", user.getRole().name())
                    .withClaim("id", user.getId())
                    .withIssuedAt(Date.from(Instant.now()))
                    .withExpiresAt(Date.from(Instant.now().plusSeconds(3600))) // 1 hour validity
                    .withIssuer(ISSUER)
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating token");
        }
    }

    @Override
    public boolean verifyJwtToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier jwtVerifier = JWT.require(algorithm).withIssuer(ISSUER).build();
            jwtVerifier.verify(parseJwt(token));
            return true;
        } catch (JWTVerificationException e) {
            log.error("JWT Verification failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public JwtClaims getJwtClaims(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier jwtVerifier = JWT.require(algorithm).withIssuer(ISSUER).build();
            DecodedJWT decodedJWT = jwtVerifier.verify(parseJwt(token));

            return JwtClaims.builder()
                    .id(decodedJWT.getClaim("id").asString())
                    .email(decodedJWT.getSubject())
                    .role(decodedJWT.getClaim("role").asString())
                    .build();
        } catch (JWTVerificationException e) {
            log.error("JWT Verification failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Jwt Token");
        }
    }

    private String parseJwt(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    @Override
    public String validateAndExtractEmail(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier jwtVerifier = JWT.require(algorithm).withIssuer(ISSUER).build();
            DecodedJWT decodedJWT = jwtVerifier.verify(parseJwt(token));

            return decodedJWT.getClaim("email").asString();
        } catch (JWTVerificationException e) {
            log.error("JWT Verification failed: {}", e.getMessage());
            throw new ValidationException("Invalid or expired token");
        }
    }
}
