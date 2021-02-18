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

import com.mpbauer.serverless.samples.petclinic.model.Owner;
import com.mpbauer.serverless.samples.petclinic.security.Roles;
import com.mpbauer.serverless.samples.petclinic.service.ClinicService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * @author Vitaliy Fedoriv
 */

@Path("/api/owners")
public class OwnerRestController {

    @Inject
    ClinicService clinicService;

    @Inject
    Validator validator; // TODO try to get rid of programmatic validations and replace it with AOP

    @RolesAllowed(Roles.OWNER_ADMIN)
    @GET
    @Path("/*/lastname/{lastName}")
    @Produces(MediaType.APPLICATION_JSON) // can be omitted with Quarkus >= 1.10.0
    public Response getOwnersList(@PathParam("lastName") String ownerLastName) {
        if (ownerLastName == null) {
            ownerLastName = "";
        }
        Collection<Owner> owners = this.clinicService.findOwnerByLastName(ownerLastName);
        if (owners.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(owners).status(Response.Status.OK).build();
    }

    @RolesAllowed(Roles.OWNER_ADMIN)
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOwners() {
        Collection<Owner> owners = this.clinicService.findAllOwners();
        if (owners.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(owners).status(Response.Status.OK).build();
    }

    @RolesAllowed(Roles.OWNER_ADMIN)
    @GET
    @Path("/{ownerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOwner(@PathParam("ownerId") int ownerId) {
        Owner owner = null;
        owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(owner).status(Response.Status.OK).build();
    }

    @RolesAllowed(Roles.OWNER_ADMIN)
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addOwner(@Valid Owner owner) {
        if (owner.getId() != null) {
            BindingErrorsResponse<Owner> bindingErrorsResponse = new BindingErrorsResponse<>(owner.getId());
            return Response.status(Response.Status.BAD_REQUEST).header("errors", bindingErrorsResponse.toJSON()).build();
        }
        this.clinicService.saveOwner(owner);
        //headers.setLocation(ucBuilder.path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri()); // TODO implement this
        return Response.ok(owner).status(Response.Status.CREATED).build();
    }

    @RolesAllowed(Roles.OWNER_ADMIN)
    @PUT
    @Path("/{ownerId}")
    public Response updateOwner(@PathParam("ownerId") int ownerId, @Valid Owner owner) {
        boolean bodyIdMatchesPathId = owner.getId() == null || ownerId == owner.getId();
        if (!bodyIdMatchesPathId) {
            BindingErrorsResponse<Owner> bindingErrorsResponse = new BindingErrorsResponse<>(ownerId, owner.getId());
            return Response.status(Response.Status.BAD_REQUEST).header("errors", bindingErrorsResponse.toJSON()).build();
        }
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        currentOwner.setAddress(owner.getAddress());
        currentOwner.setCity(owner.getCity());
        currentOwner.setFirstName(owner.getFirstName());
        currentOwner.setLastName(owner.getLastName());
        currentOwner.setTelephone(owner.getTelephone());
        this.clinicService.saveOwner(currentOwner);
        return Response.noContent().build();
    }

    @RolesAllowed(Roles.OWNER_ADMIN)
    @DELETE
    @Path("/{ownerId}")
    @Transactional
    public Response deleteOwner(@PathParam("ownerId") int ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        this.clinicService.deleteOwner(owner);
        return Response.noContent().build();
    }

}
