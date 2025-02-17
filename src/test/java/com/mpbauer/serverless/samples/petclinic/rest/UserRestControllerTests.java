package com.mpbauer.serverless.samples.petclinic.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpbauer.serverless.samples.petclinic.AbstractIntegrationTest;
import com.mpbauer.serverless.samples.petclinic.model.User;
import com.mpbauer.serverless.samples.petclinic.service.UserService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class UserRestControllerTests extends AbstractIntegrationTest {

    @InjectMock
    UserService userService;

    @Test
    void testCreateUserSuccess() throws Exception {
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setEnabled(true);
        user.addRole("OWNER_ADMIN");
        ObjectMapper mapper = new ObjectMapper();
        String newVetAsJSON = mapper.writeValueAsString(user);
        given()
            .auth().oauth2(generateValidAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newVetAsJSON)
            .when()
            .post("/api/users/")
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    void testCreateUserError() throws Exception {
        Mockito.doCallRealMethod().when(userService).saveUser(Mockito.any());

        User user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setEnabled(true);
        ObjectMapper mapper = new ObjectMapper();
        String newVetAsJSON = mapper.writeValueAsString(user);
        given()
            .auth().oauth2(generateValidAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newVetAsJSON)
            .when()
            .post("/api/users/")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
