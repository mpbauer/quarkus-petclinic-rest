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

import com.mpbauer.serverless.samples.petclinic.model.PetType;
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

@Path("api/pettypes")
public class PetTypeRestController {

    @Inject
    ClinicService clinicService;

    @Inject
    Validator validator; // TODO try to get rid of programmatic validations and replace it with AOP

    @RolesAllowed({Roles.OWNER_ADMIN, Roles.VET_ADMIN})
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPetTypes() {
        Collection<PetType> petTypes = new ArrayList<>();
        petTypes.addAll(this.clinicService.findAllPetTypes());
        if (petTypes.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(petTypes).build();
    }

    @RolesAllowed({Roles.OWNER_ADMIN, Roles.VET_ADMIN})
    @GET
    @Path("/{petTypeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPetType(@PathParam("petTypeId") int petTypeId) {
        PetType petType = this.clinicService.findPetTypeById(petTypeId);
        if (petType == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(petType).build();
    }

    @RolesAllowed(Roles.VET_ADMIN)
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPetType(@Valid PetType petType) {
        Set<ConstraintViolation<PetType>> errors = validator.validate(petType); // TODO
        if (!errors.isEmpty() || (petType == null)) {
            return Response.status(Response.Status.BAD_REQUEST).header("errors", errors.stream().collect(Collectors.toMap(ConstraintViolation::getPropertyPath, ConstraintViolation::getMessage))).entity(petType).build();
        }
        this.clinicService.savePetType(petType);

        // URI location = ucBuilder.path("/api/pettypes/{id}").buildAndExpand(petType.getId()).toUri(); // TODO
        // return Response.created(location).build();
        return Response.status(Response.Status.CREATED).entity(petType).build();
    }

    @RolesAllowed(Roles.VET_ADMIN)
    @PUT
    @Path("/{petTypeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePetType(@PathParam("petTypeId") int petTypeId, @Valid PetType petType) {
        Set<ConstraintViolation<PetType>> errors = validator.validate(petType); // TODO
        if (!errors.isEmpty() || (petType == null)) {
            return Response.status(Response.Status.BAD_REQUEST).header("errors", errors.stream().collect(Collectors.toMap(ConstraintViolation::getPropertyPath, ConstraintViolation::getMessage))).entity(petType).build();
        }
        PetType currentPetType = this.clinicService.findPetTypeById(petTypeId);
        if (currentPetType == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        currentPetType.setName(petType.getName());
        this.clinicService.savePetType(currentPetType);
        return Response.noContent().entity(currentPetType).build();
    }

    @RolesAllowed(Roles.VET_ADMIN)
    @DELETE
    @Path("/{petTypeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response deletePetType(@PathParam("petTypeId") int petTypeId) {
        PetType petType = this.clinicService.findPetTypeById(petTypeId);
        if (petType == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        this.clinicService.deletePetType(petType);
        return Response.noContent().build();
    }

}
