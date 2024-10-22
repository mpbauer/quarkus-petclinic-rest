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
import com.mpbauer.serverless.samples.petclinic.model.Owner;
import com.mpbauer.serverless.samples.petclinic.model.Pet;
import com.mpbauer.serverless.samples.petclinic.model.PetType;
import com.mpbauer.serverless.samples.petclinic.model.Visit;
import com.mpbauer.serverless.samples.petclinic.service.ClinicService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

/**
 * Test class for {@link VisitRestController}
 *
 * @author Vitaliy Fedoriv
 */
@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class VisitRestControllerTests extends AbstractIntegrationTest {

    @InjectMock
    ClinicService clinicService;

    private List<Visit> visits;

    @BeforeEach
    public void initVisits() {
        visits = new ArrayList<>();

        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("Eduardo");
        owner.setLastName("Rodriquez");
        owner.setAddress("2693 Commerce St.");
        owner.setCity("McFarland");
        owner.setTelephone("6085558763");

        PetType petType = new PetType();
        petType.setId(2);
        petType.setName("dog");

        Pet pet = new Pet();
        pet.setId(8);
        pet.setName("Rosy");
        pet.setBirthDate(new Date());
        pet.setOwner(owner);
        pet.setType(petType);


        Visit visit = new Visit();
        visit.setId(2);
        visit.setPet(pet);
        visit.setDate(new Date());
        visit.setDescription("rabies shot");
        visits.add(visit);

        visit = new Visit();
        visit.setId(3);
        visit.setPet(pet);
        visit.setDate(new Date());
        visit.setDescription("neutered");
        visits.add(visit);
    }

    @Test
    void testGetVisitSuccess() {
        given(this.clinicService.findVisitById(2)).willReturn(visits.get(0));
        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/visits/2")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(2))
            .body("description", equalTo("rabies shot"));
    }

    @Test
    void testGetVisitNotFound() {
        given(this.clinicService.findVisitById(-1)).willReturn(null);
        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/visits/-1")
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testGetAllVisitsSuccess() {
        given(this.clinicService.findAllVisits()).willReturn(visits);
        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/visits/")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.OK.getStatusCode())
            .body("[0].id", equalTo(2))
            .body("[0].description", equalTo("rabies shot"))
            .body("[1].id", equalTo(3))
            .body("[1].description", equalTo("neutered"));
    }

    @Test
    void testGetAllVisitsNotFound() {
        visits.clear();
        given(this.clinicService.findAllVisits()).willReturn(visits);
        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/visits/")
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testCreateVisitSuccess() throws Exception {
        Visit newVisit = visits.get(0);
        newVisit.setId(999);
        ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(newVisit);
        System.out.println("newVisitAsJSON " + newVisitAsJSON);
        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newVisitAsJSON)
            .when()
            .post("/api/visits/")
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    void testCreateVisitError() {
        assertThrows(IOException.class, () -> {
            Visit newVisit = visits.get(0);
            newVisit.setId(null);
            newVisit.setPet(null);
            ObjectMapper mapper = new ObjectMapper();
            String newVisitAsJSON = mapper.writeValueAsString(newVisit);
            given()
                .auth().oauth2(generateValidOwnerAdminToken())
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(newVisitAsJSON)
                .when()
                .post("/api/visits/")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        });
    }

    @Test
    void testUpdateVisitSuccess() throws Exception {
        given(this.clinicService.findVisitById(2)).willReturn(visits.get(0));
        Visit newVisit = visits.get(0);
        newVisit.setDescription("rabies shot test");
        ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(newVisit);

        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newVisitAsJSON)
            .when()
            .put("/api/visits/2")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .when()
            .get("/api/visits/2")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(2))
            .body("description", equalTo("rabies shot test"));
    }

    @Test
    void testUpdateVisitError() {
        assertThrows(IOException.class, () -> {
            Visit newVisit = visits.get(0);
            newVisit.setPet(null);
            ObjectMapper mapper = new ObjectMapper();
            String newVisitAsJSON = mapper.writeValueAsString(newVisit);
            given()
                .auth().oauth2(generateValidOwnerAdminToken())
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(newVisitAsJSON)
                .when()
                .put("/api/visits/2")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        });
    }

    @Test
    void testDeleteVisitSuccess() throws Exception {
        Visit newVisit = visits.get(0);
        ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(newVisit);
        given(this.clinicService.findVisitById(2)).willReturn(visits.get(0));
        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newVisitAsJSON)
            .when()
            .delete("/api/visits/2")
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void testDeleteVisitError() throws Exception {
        Visit newVisit = visits.get(0);
        ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(newVisit);
        given(this.clinicService.findVisitById(-1)).willReturn(null);
        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newVisitAsJSON)
            .when()
            .delete("/api/visits/-1")
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

}
