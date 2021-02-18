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

import com.mpbauer.serverless.samples.petclinic.model.Specialty;
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

/**
 * @author Vitaliy Fedoriv
 */

@Path("api/specialties")
public class SpecialtyRestController {

    @Inject
    ClinicService clinicService;

    @RolesAllowed(Roles.VET_ADMIN)
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSpecialtys() {
        Collection<Specialty> specialties = new ArrayList<>(this.clinicService.findAllSpecialties());
        if (specialties.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(specialties).build();
    }

    @RolesAllowed(Roles.VET_ADMIN)
    @GET
    @Path("/{specialtyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSpecialty(@PathParam("specialtyId") int specialtyId) {
        Specialty specialty = this.clinicService.findSpecialtyById(specialtyId);
        if (specialty == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(specialty).build();
    }

    @RolesAllowed(Roles.VET_ADMIN)
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSpecialty(@Valid @NotNull Specialty specialty, @Context UriInfo uriInfo) {
        this.clinicService.saveSpecialty(specialty);
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        uriBuilder.path(Integer.toString(specialty.getId()));
        return Response.status(Response.Status.CREATED).entity(specialty).build();
    }

    @RolesAllowed(Roles.VET_ADMIN)
    @PUT
    @Path("/{specialtyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSpecialty(@PathParam("specialtyId") int specialtyId, @Valid @NotNull Specialty specialty) {
        Specialty currentSpecialty = this.clinicService.findSpecialtyById(specialtyId);
        if (currentSpecialty == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        currentSpecialty.setName(specialty.getName());
        this.clinicService.saveSpecialty(currentSpecialty);
        return Response.noContent().entity(currentSpecialty).build();
    }

    @RolesAllowed(Roles.VET_ADMIN)
    @DELETE
    @Path("/{specialtyId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response deleteSpecialty(@PathParam("specialtyId") int specialtyId) {
        Specialty specialty = this.clinicService.findSpecialtyById(specialtyId);
        if (specialty == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        this.clinicService.deleteSpecialty(specialty);
        return Response.noContent().build();
    }

}
