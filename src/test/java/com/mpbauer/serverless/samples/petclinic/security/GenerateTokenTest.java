package com.mpbauer.serverless.samples.petclinic.security;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;

/**
 * A simple utility class to generate and print a JWT token string to stdout.
 */
@QuarkusTest
@Disabled("Enable for JWT Token generation")
class GenerateTokenTest {

    // Token expires after 100 years
    private static final Duration TOKEN_EXPIRATION = Duration.ofDays(ChronoUnit.DAYS.between(LocalDateTime.now(), LocalDateTime.now().plusYears(100)));

    /**
     * Generate JWT token
     */
    @Test
    void generateOwnerAdminToken() {
        String token = Jwt.groups(Roles.OWNER_ADMIN)
            .expiresIn(TOKEN_EXPIRATION)
            .sign();

        Assertions.assertNotNull(token);
        System.out.println(token);
    }

    @Test
    void generateVetAdminToken() {
        String token = Jwt.groups(Roles.VET_ADMIN)
            .expiresIn(TOKEN_EXPIRATION)
            .sign();

        Assertions.assertNotNull(token);
        System.out.println(token);
    }

    @Test
    void generateAdminToken() {
        String token = Jwt.groups(Roles.ADMIN)
            .expiresIn(TOKEN_EXPIRATION)
            .sign();

        Assertions.assertNotNull(token);
        System.out.println(token);
    }

    @Test
    void generateTokenWithAllRoles() {
        String token = Jwt.groups(new HashSet<>(Arrays.asList(Roles.ADMIN, Roles.OWNER_ADMIN, Roles.VET_ADMIN)))
            .expiresIn(TOKEN_EXPIRATION)
            .sign();

        Assertions.assertNotNull(token);
        System.out.println(token);
    }
}
