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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;

/**
 * Test class for {@link OwnerRestController}
 *
 * @author Vitaliy Fedoriv
 */
@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class OwnerRestControllerTests extends AbstractIntegrationTest {

    @InjectMock
    ClinicService clinicService;

    private List<Owner> owners;

    @BeforeEach
    public void initOwners() {
        owners = new ArrayList<>();

        Owner ownerWithPet = new Owner();
        ownerWithPet.setId(1);
        ownerWithPet.setFirstName("George");
        ownerWithPet.setLastName("Franklin");
        ownerWithPet.setAddress("110 W. Liberty St.");
        ownerWithPet.setCity("Madison");
        ownerWithPet.setTelephone("6085551023");
        ownerWithPet.addPet(getTestPetWithIdAndName(ownerWithPet, 1, "Rosy"));
        owners.add(ownerWithPet);

        Owner owner = new Owner();
        owner.setId(2);
        owner.setFirstName("Betty");
        owner.setLastName("Davis");
        owner.setAddress("638 Cardinal Ave.");
        owner.setCity("Sun Prairie");
        owner.setTelephone("6085551749");
        owners.add(owner);

        owner = new Owner();
        owner.setId(3);
        owner.setFirstName("Eduardo");
        owner.setLastName("Rodriquez");
        owner.setAddress("2693 Commerce St.");
        owner.setCity("McFarland");
        owner.setTelephone("6085558763");
        owners.add(owner);

        owner = new Owner();
        owner.setId(4);
        owner.setFirstName("Harold");
        owner.setLastName("Davis");
        owner.setAddress("563 Friendly St.");
        owner.setCity("Windsor");
        owner.setTelephone("6085553198");
        owners.add(owner);
    }

    private Pet getTestPetWithIdAndName(final Owner owner, final int id, final String name) {
        PetType petType = new PetType();
        petType.setId(2);
        petType.setName("dog");
        Pet pet = new Pet();
        pet.setId(id);
        pet.setName(name);
        pet.setBirthDate(new Date());
        pet.setOwner(owner);
        pet.setType(petType);
        pet.addVisit(getTestVisitForPet(pet, 1));
        return pet;
    }

    private Visit getTestVisitForPet(final Pet pet, final int id) {
        Visit visit = new Visit();
        visit.setId(id);
        visit.setPet(pet);
        visit.setDate(new Date());
        visit.setDescription("test" + id);
        return visit;
    }

    @Test
    void testGetOwnerSuccess() {
        given(this.clinicService.findOwnerById(1)).willReturn(owners.get(0));

        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .when()
            .get("/api/owners/1")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(1))
            .body("firstName", equalTo("George"));
    }

    @Test
    void testGetOwnerNotFound() {
        given(this.clinicService.findOwnerById(-1)).willReturn(null);
        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/owners/-1")
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testGetOwnersListSuccess() {
        owners.remove(0);
        owners.remove(1);
        given(this.clinicService.findOwnerByLastName("Davis")).willReturn(owners);
        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .when()
            .get("/api/owners/*/lastname/Davis")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.OK.getStatusCode())
            .body("[0].id", equalTo(2))
            .body("[0].firstName", equalTo("Betty"))
            .body("[1].id", equalTo(4))
            .body("[1].firstName", equalTo("Harold"));
    }

    @Test
    void testGetOwnersListNotFound() {
        owners.clear();
        given(this.clinicService.findOwnerByLastName("0")).willReturn(owners);
        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/owners/?lastName=0")
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testGetAllOwnersSuccess() {
        owners.remove(0);
        owners.remove(1);
        given(this.clinicService.findAllOwners()).willReturn(owners);
        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .when()
            .get("/api/owners/")
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("[0].id", equalTo(2))
            .body("[0].firstName", equalTo("Betty"))
            .body("[1].id", equalTo(4))
            .body("[1].firstName", equalTo("Harold"));
    }

    @Test
    void testGetAllOwnersNotFound() {
        owners.clear();
        given(this.clinicService.findAllOwners()).willReturn(owners);
        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/owners/")
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testCreateOwnerErrorIdSpecified() throws Exception {
        Owner newOwner = owners.get(0);
        newOwner.setId(999);
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(newOwner);

        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newOwnerAsJSON)
            .when()
            .post("/api/owners/")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
            .header("errors", "[{\"objectName\":\"body\",\"fieldName\":\"id\",\"fieldValue\":\"999\",\"errorMessage\":\"must not be specified\"}]");
    }

    @Test
    void testCreateOwnerError() throws Exception {
        Owner newOwner = owners.get(0);
        newOwner.setId(null);
        newOwner.setFirstName(null);
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(newOwner);

        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newOwnerAsJSON)
            .when()
            .post("/api/owners/")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testUpdateOwnerSuccess() throws Exception {
        given(this.clinicService.findOwnerById(1)).willReturn(owners.get(0));
        int ownerId = owners.get(0).getId();
        Owner updatedOwner = new Owner();
        // body.id = ownerId which is used in url path
        updatedOwner.setId(ownerId);
        updatedOwner.setFirstName("George I");
        updatedOwner.setLastName("Franklin");
        updatedOwner.setAddress("110 W. Liberty St.");
        updatedOwner.setCity("Madison");
        updatedOwner.setTelephone("6085551023");
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(updatedOwner);

        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newOwnerAsJSON)
            .when()
            .put("/api/owners/" + ownerId)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .when()
            .get("/api/owners/" + ownerId)
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(1))
            .body("firstName", equalTo("George I"));
    }

    @Test
    void testUpdateOwnerSuccessNoBodyId() throws Exception {
        given(this.clinicService.findOwnerById(1)).willReturn(owners.get(0));
        int ownerId = owners.get(0).getId();
        Owner updatedOwner = new Owner();
        updatedOwner.setFirstName("George I");
        updatedOwner.setLastName("Franklin");
        updatedOwner.setAddress("110 W. Liberty St.");
        updatedOwner.setCity("Madison");
        updatedOwner.setTelephone("6085551023");
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(updatedOwner);

        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newOwnerAsJSON)
            .when()
            .put("/api/owners/" + ownerId)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .when()
            .get("/api/owners/" + ownerId)
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(ownerId))
            .body("firstName", equalTo("George I"));
    }

    @Test
    void testUpdateOwnerErrorBodyIdMismatchWithPathId() throws Exception {
        int ownerId = owners.get(0).getId();
        Owner updatedOwner = new Owner();
        // body.id != ownerId
        updatedOwner.setId(-1);
        updatedOwner.setFirstName("George I");
        updatedOwner.setLastName("Franklin");
        updatedOwner.setAddress("110 W. Liberty St.");
        updatedOwner.setCity("Madison");
        updatedOwner.setTelephone("6085551023");
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(updatedOwner);

        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newOwnerAsJSON)
            .when()
            .put("/api/owners/" + ownerId)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
            .header("errors", "[{\"objectName\":\"body\",\"fieldName\":\"id\",\"fieldValue\":\"-1\",\"errorMessage\":\"does not match pathId: 1\"}]");
    }

    @Test
    void testUpdateOwnerError() throws Exception {
        Owner newOwner = owners.get(0);
        newOwner.setFirstName("");
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(newOwner);

        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newOwnerAsJSON)
            .when()
            .put("/api/owners/1")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testDeleteOwnerSuccess() throws Exception {
        Owner newOwner = owners.get(0);
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(newOwner);
        given(this.clinicService.findOwnerById(1)).willReturn(owners.get(0));

        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newOwnerAsJSON)
            .when()
            .delete("/api/owners/1")
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void testDeleteOwnerError() throws Exception {
        Owner newOwner = owners.get(0);
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(newOwner);
        given(this.clinicService.findOwnerById(-1)).willReturn(null);

        given()
            .auth().oauth2(generateValidOwnerAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newOwnerAsJSON)
            .when()
            .delete("/api/owners/-1")
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
