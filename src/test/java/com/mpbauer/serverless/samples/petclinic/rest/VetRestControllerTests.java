/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mpbauer.serverless.samples.petclinic.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpbauer.serverless.samples.petclinic.AbstractIntegrationTest;
import com.mpbauer.serverless.samples.petclinic.model.Vet;
import com.mpbauer.serverless.samples.petclinic.service.ClinicService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;

/**
 * Test class for {@link VetRestController}
 *
 * @author Vitaliy Fedoriv
 */
@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class VetRestControllerTests extends AbstractIntegrationTest {

    @InjectMock
    ClinicService clinicService;

    private List<Vet> vets;

    @BeforeEach
    public void initVets() {
        vets = new ArrayList<>();


        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("James");
        vet.setLastName("Carter");
        vets.add(vet);

        vet = new Vet();
        vet.setId(2);
        vet.setFirstName("Helen");
        vet.setLastName("Leary");
        vets.add(vet);

        vet = new Vet();
        vet.setId(3);
        vet.setFirstName("Linda");
        vet.setLastName("Douglas");
        vets.add(vet);
    }

    @Test
    void testGetVetSuccess() {
        given(this.clinicService.findVetById(1)).willReturn(vets.get(0));
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/vets/1")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(1))
            .body("firstName", equalTo("James"));
    }

    @Test
    void testGetVetNotFound() {
        given(this.clinicService.findVetById(-1)).willReturn(null);
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/vets/-1")
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testGetAllVetsSuccess() {
        given(this.clinicService.findAllVets()).willReturn(vets);
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/vets/")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.OK.getStatusCode())
            .body("[0].id", equalTo(1))
            .body("[0].firstName", equalTo("James"))
            .body("[1].id", equalTo(2))
            .body("[1].firstName", equalTo("Helen"));
    }

    @Test
    void testGetAllVetsNotFound() {
        vets.clear();
        given(this.clinicService.findAllVets()).willReturn(vets);
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/vets/")
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testCreateVetSuccess() throws Exception {
        Vet newVet = vets.get(0);
        newVet.setId(999);
        ObjectMapper mapper = new ObjectMapper();
        String newVetAsJSON = mapper.writeValueAsString(newVet);
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newVetAsJSON)
            .when()
            .post("/api/vets/")
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    void testCreateVetError() throws Exception {
        Vet newVet = vets.get(0);
        newVet.setId(null);
        newVet.setFirstName(null);
        ObjectMapper mapper = new ObjectMapper();
        String newVetAsJSON = mapper.writeValueAsString(newVet);
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newVetAsJSON)
            .when()
            .post("/api/vets/")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testUpdateVetSuccess() throws Exception {
        given(this.clinicService.findVetById(1)).willReturn(vets.get(0));
        Vet newVet = vets.get(0);
        newVet.setFirstName("James");
        ObjectMapper mapper = new ObjectMapper();
        String newVetAsJSON = mapper.writeValueAsString(newVet);

        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newVetAsJSON)
            .when()
            .put("/api/vets/1")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .when()
            .get("/api/vets/1")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(1))
            .body("firstName", equalTo("James"));
    }

    @Test
    void testUpdateVetError() throws Exception {
        Vet newVet = vets.get(0);
        newVet.setFirstName("");
        ObjectMapper mapper = new ObjectMapper();
        String newVetAsJSON = mapper.writeValueAsString(newVet);
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newVetAsJSON)
            .when()
            .put("/api/vets/1")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testDeleteVetSuccess() throws Exception {
        Vet newVet = vets.get(0);
        ObjectMapper mapper = new ObjectMapper();
        String newVetAsJSON = mapper.writeValueAsString(newVet);
        given(this.clinicService.findVetById(1)).willReturn(vets.get(0));
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newVetAsJSON)
            .when()
            .delete("/api/vets/1")
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void testDeleteVetError() throws Exception {
        Vet newVet = vets.get(0);
        ObjectMapper mapper = new ObjectMapper();
        String newVetAsJSON = mapper.writeValueAsString(newVet);
        given(this.clinicService.findVetById(-1)).willReturn(null);
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newVetAsJSON)
            .when()
            .delete("/api/vets/-1")
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}

