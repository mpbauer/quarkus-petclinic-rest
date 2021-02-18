/*
 * Copyright 2002-2017 the original author or authors.
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
package com.mpbauer.serverless.samples.petclinic.service;

import com.mpbauer.serverless.samples.petclinic.config.LoggingFilter;
import com.mpbauer.serverless.samples.petclinic.model.*;
import com.mpbauer.serverless.samples.petclinic.repository.*;
import io.quarkus.cache.CacheResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collection;

/**
 * Mostly used as a facade for all Petclinic controllers
 * Also a placeholder for @Transactional and @Cacheable annotations
 *
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@ApplicationScoped
public class ClinicServiceImpl implements ClinicService {

    private static final Logger LOG = LoggerFactory.getLogger(ClinicServiceImpl.class);


    PetRepository petRepository;
    VetRepository vetRepository;
    OwnerRepository ownerRepository;
    VisitRepository visitRepository;
    SpecialtyRepository specialtyRepository;
    PetTypeRepository petTypeRepository;

    @Inject
    public ClinicServiceImpl(
        PetRepository petRepository,
        VetRepository vetRepository,
        OwnerRepository ownerRepository,
        VisitRepository visitRepository,
        SpecialtyRepository specialtyRepository,
        PetTypeRepository petTypeRepository) {
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
        this.ownerRepository = ownerRepository;
        this.visitRepository = visitRepository;
        this.specialtyRepository = specialtyRepository;
        this.petTypeRepository = petTypeRepository;
    }

    @Override
    @Transactional
    public Collection<Pet> findAllPets() {
        return petRepository.findAll();
    }

    @Override
    @Transactional
    public void deletePet(Pet pet) {
        petRepository.delete(pet);
    }

    @Override
    @Transactional
    public Visit findVisitById(int visitId) {
        Visit visit = null;
        try {
            visit = visitRepository.findById(visitId);
        } catch (Exception e) {
            LOG.warn("An unexpected exception occured", e);
            // TODO try to catch a more generic exception to not accidentally swallow important exceptions
            // just ignore not found exceptions for Jdbc/Jpa realization
            return null;
        }
        return visit;
    }

    @Override
    @Transactional
    public Collection<Visit> findAllVisits() {
        return visitRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteVisit(Visit visit) {
        visitRepository.delete(visit);
    }

    @Override
    @Transactional
    public Vet findVetById(int id) {
        Vet vet = null;
        try {
            vet = vetRepository.findById(id);
        } catch (Exception e) {
            LOG.warn("An unexpected exception occured", e);
            // TODO try to catch a more generic exception to not accidentally swallow important exceptions
            // just ignore not found exceptions for Jdbc/Jpa realization
            return null;
        }
        return vet;
    }

    @Override
    @Transactional
    public Collection<Vet> findAllVets() {
        return vetRepository.findAll();
    }

    @Override
    @Transactional
    public void saveVet(Vet vet) {
        vetRepository.save(vet);
    }

    @Override
    @Transactional
    public void deleteVet(Vet vet) {
        vetRepository.delete(vet);
    }

    @Override
    @Transactional
    public Collection<Owner> findAllOwners() {
        return ownerRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteOwner(Owner owner) {
        ownerRepository.delete(owner);
    }

    @Override
    @Transactional
    public PetType findPetTypeById(int petTypeId) {
        PetType petType = null;
        try {
            petType = petTypeRepository.findById(petTypeId);
        } catch (Exception e) {
            LOG.warn("An unexpected exception occured", e);
            // TODO try to catch a more generic exception to not accidentally swallow important exceptions
            // just ignore not found exceptions for Jdbc/Jpa realization
            return null;
        }
        return petType;
    }

    @Override
    @Transactional
    public Collection<PetType> findAllPetTypes() {
        return petTypeRepository.findAll();
    }

    @Override
    @Transactional
    public void savePetType(PetType petType) {
        petTypeRepository.save(petType);
    }

    @Override
    @Transactional
    public void deletePetType(PetType petType) {
        petTypeRepository.delete(petType);
    }

    @Override
    @Transactional
    public Specialty findSpecialtyById(int specialtyId) {
        Specialty specialty = null;
        try {
            specialty = specialtyRepository.findById(specialtyId);
        } catch (Exception e) {
            LOG.warn("An unexpected exception occured", e);

            // TODO try to catch a more generic exception to not accidentally swallow important exceptions
            // just ignore not found exceptions for Jdbc/Jpa realization
            return null;
        }
        return specialty;
    }

    @Override
    @Transactional
    public Collection<Specialty> findAllSpecialties() {
        return specialtyRepository.findAll();
    }

    @Override
    @Transactional
    public void saveSpecialty(Specialty specialty) {
        specialtyRepository.save(specialty);
    }

    @Override
    @Transactional
    public void deleteSpecialty(Specialty specialty) {
        specialtyRepository.delete(specialty);
    }

    @Override
    @Transactional
    public Collection<PetType> findPetTypes() {
        return petRepository.findPetTypes();
    }

    @Override
    @Transactional
    public Owner findOwnerById(int id) {
        Owner owner = null;
        try {
            owner = ownerRepository.findById(id);
        } catch (Exception e) {
            LOG.warn("An unexpected exception occured", e);

            // TODO try to catch a more generic exception to not accidentally swallow important exceptions
            // just ignore not found exceptions for Jdbc/Jpa realization
            return null;
        }
        return owner;
    }

    @Override
    @Transactional
    public Pet findPetById(int id) {
        Pet pet = null;
        try {
            pet = petRepository.findById(id);
        } catch (Exception e) {
            LOG.warn("An unexpected exception occured", e);

            // TODO try to catch a more generic exception to not accidentally swallow important exceptions
            // just ignore not found exceptions for Jdbc/Jpa realization
            return null;
        }
        return pet;
    }

    @Override
    @Transactional
    public void savePet(Pet pet) {
        petRepository.save(pet);
    }

    @Override
    @Transactional
    public void saveVisit(Visit visit) {
        visitRepository.save(visit);

    }

    @Override
    @Transactional
    @CacheResult(cacheName = "vets")
    public Collection<Vet> findVets() {
        return vetRepository.findAll();
    }

    @Override
    @Transactional
    public void saveOwner(Owner owner) {
        ownerRepository.save(owner);

    }

    @Override
    @Transactional
    public Collection<Owner> findOwnerByLastName(String lastName) {
        return ownerRepository.findByLastName(lastName);
    }

    @Override
    @Transactional
    public Collection<Visit> findVisitsByPetId(int petId) {
        return visitRepository.findByPetId(petId);
    }
}
