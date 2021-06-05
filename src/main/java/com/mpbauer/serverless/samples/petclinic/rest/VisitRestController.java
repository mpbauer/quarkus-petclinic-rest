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

import com.mpbauer.serverless.samples.petclinic.model.Visit;
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

@Path("api/visits")
public class VisitRestController {

    @Inject
    ClinicService clinicService;

    @RolesAllowed(Roles.OWNER_ADMIN)
    @GET
    @Path("/")
    public Response getAllVisits() {
        Collection<Visit> visits = new ArrayList<>(this.clinicService.findAllVisits());
        if (visits.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(visits).build();
    }

    @RolesAllowed(Roles.OWNER_ADMIN)
    @GET
    @Path("/{visitId}")
    public Response getVisit(@PathParam("visitId") int visitId) {
        Visit visit = this.clinicService.findVisitById(visitId);
        if (visit == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(visit).build();
    }

    @RolesAllowed(Roles.OWNER_ADMIN)
    @POST
    @Path("/")
    public Response addVisit(@Valid @NotNull Visit visit, @Context UriInfo uriInfo) {
        this.clinicService.saveVisit(visit);
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        uriBuilder.path(Integer.toString(visit.getId()));
        return Response.created(uriBuilder.build()).entity(visit).build();
    }

    @RolesAllowed(Roles.OWNER_ADMIN)
    @PUT
    @Path("/{visitId}")
    public Response updateVisit(@PathParam("visitId") int visitId, @Valid @NotNull Visit visit) {
        Visit currentVisit = this.clinicService.findVisitById(visitId);
        if (currentVisit == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        currentVisit.setDate(visit.getDate());
        currentVisit.setDescription(visit.getDescription());
        currentVisit.setPet(visit.getPet());
        this.clinicService.saveVisit(currentVisit);
        return Response.noContent().entity(currentVisit).build();
    }

    @RolesAllowed(Roles.OWNER_ADMIN)
    @DELETE
    @Path("/{visitId}")
    @Transactional
    public Response deleteVisit(@PathParam("visitId") int visitId) {
        Visit visit = this.clinicService.findVisitById(visitId);
        if (visit == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        this.clinicService.deleteVisit(visit);
        return Response.noContent().build();
    }
}
