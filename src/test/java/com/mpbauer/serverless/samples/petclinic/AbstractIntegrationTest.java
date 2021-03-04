package com.mpbauer.serverless.samples.petclinic;

import com.mpbauer.serverless.samples.petclinic.security.Roles;
import io.smallrye.jwt.build.Jwt;

public abstract class AbstractIntegrationTest {

    protected String generateValidOwnerAdminToken() {
        return Jwt.groups(Roles.OWNER_ADMIN).sign();
    }

    protected String generateValidVetAdminToken() {
        return Jwt
            .groups(Roles.VET_ADMIN)
            .sign();
    }

    protected String generateValidAdminToken() {
        return Jwt
            .groups(Roles.ADMIN)
            .sign();
    }
}
