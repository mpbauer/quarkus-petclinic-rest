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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.Collection;

@Path("api/pettypes")
public class PetTypeRestController {

    @Inject
    ClinicService clinicService;

    @RolesAllowed({Roles.OWNER_ADMIN, Roles.VET_ADMIN})
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPetTypes() {
        Collection<PetType> petTypes = new ArrayList<>(this.clinicService.findAllPetTypes());
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
    public Response addPetType(@Valid @NotNull PetType petType, @Context UriInfo uriInfo) {
        this.clinicService.savePetType(petType);
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        uriBuilder.path(Integer.toString(petType.getId()));
        return Response.status(Response.Status.CREATED).entity(petType).build();
    }

    @RolesAllowed(Roles.VET_ADMIN)
    @PUT
    @Path("/{petTypeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePetType(@PathParam("petTypeId") int petTypeId, @Valid @NotNull PetType petType) {
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
