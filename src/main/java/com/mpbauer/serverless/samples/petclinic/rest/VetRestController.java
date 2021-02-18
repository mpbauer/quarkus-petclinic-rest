/*
 * Copyright 2016-2018 the original author or authors.
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

import com.mpbauer.serverless.samples.petclinic.model.Specialty;
import com.mpbauer.serverless.samples.petclinic.model.Vet;
import com.mpbauer.serverless.samples.petclinic.security.Roles;
import com.mpbauer.serverless.samples.petclinic.service.ClinicService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Vitaliy Fedoriv
 */

@Path("api/vets")
public class VetRestController {

    @Inject
    ClinicService clinicService;

    @Inject
    Validator validator; // TODO try to get rid of programmatic validations and replace it with AOP

    @RolesAllowed(Roles.VET_ADMIN)
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllVets() {
        Collection<Vet> vets = new ArrayList<>();
        vets.addAll(this.clinicService.findAllVets());
        if (vets.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(vets).build();
    }

    @RolesAllowed(Roles.VET_ADMIN)
    @GET
    @Path(value = "/{vetId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVet(@PathParam("vetId") int vetId) {
        Vet vet = this.clinicService.findVetById(vetId);
        if (vet == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(vet).build();
    }

    @RolesAllowed(Roles.VET_ADMIN)
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addVet(@Valid Vet vet) {
        Set<ConstraintViolation<Vet>> errors = validator.validate(vet); // TODO
        if (!errors.isEmpty() || (vet == null)) {
            return Response.status(Response.Status.BAD_REQUEST).header("errors", errors.stream().collect(Collectors.toMap(ConstraintViolation::getPropertyPath, ConstraintViolation::getMessage))).entity(vet).build();
        }

        // URI location = ucBuilder.path("/api/vets/{id}").buildAndExpand(vet.getId()).toUri(); // TODO
        // return Response.created(location).build();
        this.clinicService.saveVet(vet);
        return Response.status(Response.Status.CREATED).entity(vet).build();
    }

    @RolesAllowed(Roles.VET_ADMIN)
    @PUT
    @Path("/{vetId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateVet(@PathParam("vetId") int vetId, @Valid Vet vet) {
        Set<ConstraintViolation<Vet>> errors = validator.validate(vet); // TODO
        if (!errors.isEmpty() || (vet == null)) {
            return Response.status(Response.Status.BAD_REQUEST).header("errors", errors.stream().collect(Collectors.toMap(ConstraintViolation::getPropertyPath, ConstraintViolation::getMessage))).entity(vet).build();
        }

        Vet currentVet = this.clinicService.findVetById(vetId);
        if (currentVet == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        currentVet.setFirstName(vet.getFirstName());
        currentVet.setLastName(vet.getLastName());
        currentVet.clearSpecialties();
        for (Specialty spec : vet.getSpecialties()) {
            currentVet.addSpecialty(spec);
        }
        this.clinicService.saveVet(currentVet);
        return Response.noContent().entity(currentVet).build();
    }

    @RolesAllowed(Roles.VET_ADMIN)
    @DELETE
    @Path("/{vetId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response deleteVet(@PathParam("vetId") int vetId) {
        Vet vet = this.clinicService.findVetById(vetId);
        if (vet == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        this.clinicService.deleteVet(vet);
        return Response.noContent().build();
    }
}
