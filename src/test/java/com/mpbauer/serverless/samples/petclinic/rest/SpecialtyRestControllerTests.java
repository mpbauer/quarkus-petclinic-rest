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
import com.mpbauer.serverless.samples.petclinic.model.Specialty;
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
 * Test class for {@link SpecialtyRestController}
 *
 * @author Vitaliy Fedoriv
 */
@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class SpecialtyRestControllerTests extends AbstractIntegrationTest {

    @InjectMock
    ClinicService clinicService;

    private List<Specialty> specialties;

    @BeforeEach
    public void initSpecialtys() {
        specialties = new ArrayList<>();

        Specialty specialty = new Specialty();
        specialty.setId(1);
        specialty.setName("radiology");
        specialties.add(specialty);

        specialty = new Specialty();
        specialty.setId(2);
        specialty.setName("surgery");
        specialties.add(specialty);

        specialty = new Specialty();
        specialty.setId(3);
        specialty.setName("dentistry");
        specialties.add(specialty);
    }

    @Test
    void testGetSpecialtySuccess() {
        given(this.clinicService.findSpecialtyById(1)).willReturn(specialties.get(0));
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/specialties/1")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(1))
            .body("name", equalTo("radiology"));
    }

    @Test
    void testGetSpecialtyNotFound() {
        given(this.clinicService.findSpecialtyById(-1)).willReturn(null);
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/specialties/-1")
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testGetAllSpecialtysSuccess() {
        specialties.remove(0);
        given(this.clinicService.findAllSpecialties()).willReturn(specialties);
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/specialties/")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.OK.getStatusCode())
            .body("[0].id", equalTo(2))
            .body("[0].name", equalTo("surgery"))
            .body("[1].id", equalTo(3))
            .body("[1].name", equalTo("dentistry"));
    }

    @Test
    void testGetAllSpecialtysNotFound() {
        specialties.clear();
        given(this.clinicService.findAllSpecialties()).willReturn(specialties);
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .when()
            .get("/api/specialties/")
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testCreateSpecialtySuccess() throws Exception {
        Specialty newSpecialty = specialties.get(0);
        newSpecialty.setId(999);
        ObjectMapper mapper = new ObjectMapper();
        String newSpecialtyAsJSON = mapper.writeValueAsString(newSpecialty);
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newSpecialtyAsJSON)
            .when()
            .post("/api/specialties/")
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    void testCreateSpecialtyError() throws Exception {
        Specialty newSpecialty = specialties.get(0);
        newSpecialty.setId(null);
        newSpecialty.setName(null);
        ObjectMapper mapper = new ObjectMapper();
        String newSpecialtyAsJSON = mapper.writeValueAsString(newSpecialty);
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newSpecialtyAsJSON)
            .when()
            .post("/api/specialties/")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testUpdateSpecialtySuccess() throws Exception {
        given(this.clinicService.findSpecialtyById(2)).willReturn(specialties.get(1));
        Specialty newSpecialty = specialties.get(1);
        newSpecialty.setName("surgery I");
        ObjectMapper mapper = new ObjectMapper();
        String newSpecialtyAsJSON = mapper.writeValueAsString(newSpecialty);

        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newSpecialtyAsJSON)
            .when()
            .put("/api/specialties/2")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .when()
            .get("/api/specialties/2")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(2))
            .body("name", equalTo("surgery I"));
    }

    @Test
    void testUpdateSpecialtyError() throws Exception {
        Specialty newSpecialty = specialties.get(0);
        newSpecialty.setName("");
        ObjectMapper mapper = new ObjectMapper();
        String newSpecialtyAsJSON = mapper.writeValueAsString(newSpecialty);
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newSpecialtyAsJSON)
            .when()
            .put("/api/specialties/1")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testDeleteSpecialtySuccess() throws Exception {
        Specialty newSpecialty = specialties.get(0);
        ObjectMapper mapper = new ObjectMapper();
        String newSpecialtyAsJSON = mapper.writeValueAsString(newSpecialty);
        given(this.clinicService.findSpecialtyById(1)).willReturn(specialties.get(0));
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newSpecialtyAsJSON)
            .when()
            .delete("/api/specialties/1")
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void testDeleteSpecialtyError() throws Exception {
        Specialty newSpecialty = specialties.get(0);
        ObjectMapper mapper = new ObjectMapper();
        String newSpecialtyAsJSON = mapper.writeValueAsString(newSpecialty);
        given(this.clinicService.findSpecialtyById(-1)).willReturn(null);
        given()
            .auth().oauth2(generateValidVetAdminToken())
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(newSpecialtyAsJSON)
            .when()
            .delete("/api/specialties/-1")
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
