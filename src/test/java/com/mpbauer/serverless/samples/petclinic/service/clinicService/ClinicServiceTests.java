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
package com.mpbauer.serverless.samples.petclinic.service.clinicService;

import com.mpbauer.serverless.samples.petclinic.config.QuarkusDataSourceProvider;
import com.mpbauer.serverless.samples.petclinic.model.*;
import com.mpbauer.serverless.samples.petclinic.repository.PetTypeRepository;
import com.mpbauer.serverless.samples.petclinic.repository.VetRepository;
import com.mpbauer.serverless.samples.petclinic.service.ClinicService;
import com.radcortez.flyway.test.annotation.DataSource;
import com.radcortez.flyway.test.annotation.FlywayTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p> Base class for {@link ClinicService} integration tests.
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@FlywayTest(@DataSource(QuarkusDataSourceProvider.class))
@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
//@FlywayTest(value = @DataSource(url = "jdbc:h2:mem:default"))
class ClinicServiceTests {

    @Inject
    protected ClinicService clinicService;

    @Inject
    PetTypeRepository petTypeRepository;

    @Inject
    VetRepository vetRepository;

    @Test
    void shouldFindOwnersByLastName() {
        Collection<Owner> owners = this.clinicService.findOwnerByLastName("Davis");
        assertThat(owners.size()).isEqualTo(2);

        owners = this.clinicService.findOwnerByLastName("Daviss");
        assertThat(owners.isEmpty()).isTrue();
    }

    @Test
    void shouldFindSingleOwnerWithPet() {
        Owner owner = this.clinicService.findOwnerById(1);
        assertThat(owner.getLastName()).startsWith("Franklin");
        assertThat(owner.getPets().size()).isEqualTo(1);
        assertThat(owner.getPets().get(0).getType()).isNotNull();
        assertThat(owner.getPets().get(0).getType().getName()).isEqualTo("cat");
    }

    @Test
    @Transactional
    void shouldInsertOwner() {
        Collection<Owner> owners = this.clinicService.findOwnerByLastName("Schultz");
        int found = owners.size();

        Owner owner = new Owner();
        owner.setFirstName("Sam");
        owner.setLastName("Schultz");
        owner.setAddress("4, Evans Street");
        owner.setCity("Wollongong");
        owner.setTelephone("4444444444");
        this.clinicService.saveOwner(owner);
        assertThat(owner.getId().longValue()).isNotEqualTo(0);

        owners = this.clinicService.findOwnerByLastName("Schultz");
        assertThat(owners.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdateOwner() {
        Owner owner = this.clinicService.findOwnerById(1);
        String oldLastName = owner.getLastName();
        String newLastName = oldLastName + "X";

        owner.setLastName(newLastName);
        this.clinicService.saveOwner(owner);

        // retrieving new name from database
        owner = this.clinicService.findOwnerById(1);
        assertThat(owner.getLastName()).isEqualTo(newLastName);
    }

    @Test
    void shouldFindPetWithCorrectId() {
        Pet pet7 = this.clinicService.findPetById(7);
        assertThat(pet7.getName()).startsWith("Samantha");
        assertThat(pet7.getOwner().getFirstName()).isEqualTo("Jean");

    }

    @Test
    @Transactional
    void shouldInsertPetIntoDatabaseAndGenerateId() {
        Owner owner6 = this.clinicService.findOwnerById(6);
        int found = owner6.getPets().size();

        Pet pet = new Pet();
        pet.setName("bowser");
        pet.setType(petTypeRepository.findById(2));
        pet.setBirthDate(new Date());
        owner6.addPet(pet);
        assertThat(owner6.getPets().size()).isEqualTo(found + 1);

        this.clinicService.savePet(pet);
        this.clinicService.saveOwner(owner6);

        owner6 = this.clinicService.findOwnerById(6);
        assertThat(owner6.getPets().size()).isEqualTo(found + 1);
        // checks that id has been generated
        assertThat(pet.getId()).isNotNull();
    }

    @Test
    @Transactional
    void shouldUpdatePetName() throws Exception {
        Pet pet7 = this.clinicService.findPetById(7);
        String oldName = pet7.getName();

        String newName = oldName + "X";
        pet7.setName(newName);
        this.clinicService.savePet(pet7);

        pet7 = this.clinicService.findPetById(7);
        assertThat(pet7.getName()).isEqualTo(newName);
    }

    @Test
    void shouldFindVets() {
        Collection<Vet> vets = this.clinicService.findVets();

        Vet vet = vetRepository.findById(3);
        assertThat(vet.getLastName()).isEqualTo("Douglas");
        assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
        assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("dentistry");
        assertThat(vet.getSpecialties().get(1).getName()).isEqualTo("surgery");
    }

    @Test
    @Transactional
    void shouldAddNewVisitForPet() {
        Pet pet7 = this.clinicService.findPetById(7);
        int found = pet7.getVisits().size();
        Visit visit = new Visit();
        pet7.addVisit(visit);
        visit.setDescription("test");
        this.clinicService.saveVisit(visit);
        this.clinicService.savePet(pet7);

        pet7 = this.clinicService.findPetById(7);
        assertThat(pet7.getVisits().size()).isEqualTo(found + 1);
        assertThat(visit.getId()).isNotNull();
    }

    @Test
    void shouldFindVisitsByPetId() throws Exception {
        Collection<Visit> visits = this.clinicService.findVisitsByPetId(7);
        assertThat(visits.size()).isEqualTo(2);
        Visit[] visitArr = visits.toArray(new Visit[visits.size()]);
        assertThat(visitArr[0].getPet()).isNotNull();
        assertThat(visitArr[0].getDate()).isNotNull();
        assertThat(visitArr[0].getPet().getId()).isEqualTo(7);
    }

    @Test
    void shouldFindAllPets(){
        Collection<Pet> pets = this.clinicService.findAllPets();
        Pet pet1 = pets.stream().filter(pet -> pet.getId() == 1).findFirst().get();
        assertThat(pet1.getName()).isEqualTo("Leo");
        Pet pet3 = pets.stream().filter(pet -> pet.getId() == 3).findFirst().get();
        assertThat(pet3.getName()).isEqualTo("Rosy");
    }

    @Test
    @Transactional
    void shouldDeletePet(){
        Pet pet = this.clinicService.findPetById(1);
        this.clinicService.deletePet(pet);
        try {
            pet = this.clinicService.findPetById(1);
		} catch (Exception e) {
			pet = null;
		}
        assertThat(pet).isNull();
    }

    @Test
    void shouldFindVisitDyId(){
    	Visit visit = this.clinicService.findVisitById(1);
    	assertThat(visit.getId()).isEqualTo(1);
    	assertThat(visit.getPet().getName()).isEqualTo("Samantha");
    }

    @Test
    void shouldFindAllVisits(){
        Collection<Visit> visits = this.clinicService.findAllVisits();
        Visit visit1 = visits.stream().filter(e -> e.getId() == 1).findFirst().get();
        assertThat(visit1.getPet().getName()).isEqualTo("Samantha");
        Visit visit3 = visits.stream().filter(e -> e.getId() == 3).findFirst().get();
        assertThat(visit3.getPet().getName()).isEqualTo("Max");
    }

    @Test
    @Transactional
    void shouldInsertVisit() {
        Collection<Visit> visits = this.clinicService.findAllVisits();
        int found = visits.size();

        Pet pet = this.clinicService.findPetById(1);

        Visit visit = new Visit();
        visit.setPet(pet);
        visit.setDate(new Date());
        visit.setDescription("new visit");


        this.clinicService.saveVisit(visit);
        assertThat(visit.getId().longValue()).isNotEqualTo(0);

        visits = this.clinicService.findAllVisits();
        assertThat(visits.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdateVisit(){
    	Visit visit = this.clinicService.findVisitById(1);
    	String oldDesc = visit.getDescription();
        String newDesc = oldDesc + "X";
        visit.setDescription(newDesc);
        this.clinicService.saveVisit(visit);
        visit = this.clinicService.findVisitById(1);
        assertThat(visit.getDescription()).isEqualTo(newDesc);
    }

    @Test
    @Transactional
    void shouldDeleteVisit(){
    	Visit visit = this.clinicService.findVisitById(1);
        this.clinicService.deleteVisit(visit);
        try {
        	visit = this.clinicService.findVisitById(1);
		} catch (Exception e) {
			visit = null;
		}
        assertThat(visit).isNull();
    }

    @Test
    void shouldFindVetDyId(){
    	Vet vet = this.clinicService.findVetById(1);
    	assertThat(vet.getFirstName()).isEqualTo("James");
    	assertThat(vet.getLastName()).isEqualTo("Carter");
    }

    @Test
    @Transactional
    void shouldInsertVet() {
        Collection<Vet> vets = this.clinicService.findAllVets();
        int found = vets.size();

        Vet vet = new Vet();
        vet.setFirstName("John");
        vet.setLastName("Dow");

        this.clinicService.saveVet(vet);
        assertThat(vet.getId().longValue()).isNotEqualTo(0);

        vets = this.clinicService.findAllVets();
        assertThat(vets.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdateVet(){
    	Vet vet = this.clinicService.findVetById(1);
    	String oldLastName = vet.getLastName();
        String newLastName = oldLastName + "X";
        vet.setLastName(newLastName);
        this.clinicService.saveVet(vet);
        vet = this.clinicService.findVetById(1);
        assertThat(vet.getLastName()).isEqualTo(newLastName);
    }

    @Test
    @Transactional
    void shouldDeleteVet(){
    	Vet vet = this.clinicService.findVetById(1);
        this.clinicService.deleteVet(vet);
        try {
        	vet = this.clinicService.findVetById(1);
		} catch (Exception e) {
			vet = null;
		}
        assertThat(vet).isNull();
    }

    @Test
    void shouldFindAllOwners(){
        Collection<Owner> owners = this.clinicService.findAllOwners();
        Owner owner1 = owners.stream().filter(e -> e.getId() == 1).findFirst().get();
        assertThat(owner1.getFirstName()).isEqualTo("George");
        Owner owner3 = owners.stream().filter(e -> e.getId() == 3).findFirst().get();
        assertThat(owner3.getFirstName()).isEqualTo("Eduardo");
    }

    @Test
    @Transactional
    void shouldDeleteOwner(){
    	Owner owner = this.clinicService.findOwnerById(1);
        this.clinicService.deleteOwner(owner);
        try {
        	owner = this.clinicService.findOwnerById(1);
		} catch (Exception e) {
			owner = null;
		}
        assertThat(owner).isNull();
    }

    @Test
    void shouldFindPetTypeById(){
    	PetType petType = this.clinicService.findPetTypeById(1);
    	assertThat(petType.getName()).isEqualTo("cat");
    }

    @Test
    void shouldFindAllPetTypes(){
        Collection<PetType> petTypes = this.clinicService.findAllPetTypes();
        PetType petType1 = petTypes.stream().filter(e -> e.getId() == 1).findFirst().get();
        assertThat(petType1.getName()).isEqualTo("cat");
        PetType petType3 = petTypes.stream().filter(e -> e.getId() == 3).findFirst().get();
        assertThat(petType3.getName()).isEqualTo("lizard");
    }

    @Test
    @Transactional
    void shouldInsertPetType() {
        Collection<PetType> petTypes = this.clinicService.findAllPetTypes();
        int found = petTypes.size();

        PetType petType = new PetType();
        petType.setName("tiger");

        this.clinicService.savePetType(petType);
        assertThat(petType.getId().longValue()).isNotEqualTo(0);

        petTypes = this.clinicService.findAllPetTypes();
        assertThat(petTypes.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdatePetType(){
    	PetType petType = this.clinicService.findPetTypeById(1);
    	String oldLastName = petType.getName();
        String newLastName = oldLastName + "X";
        petType.setName(newLastName);
        this.clinicService.savePetType(petType);
        petType = this.clinicService.findPetTypeById(1);
        assertThat(petType.getName()).isEqualTo(newLastName);
    }

    @Test
    @Transactional
    void shouldDeletePetType(){
    	PetType petType = this.clinicService.findPetTypeById(1);
        this.clinicService.deletePetType(petType);
        try {
        	petType = this.clinicService.findPetTypeById(1);
		} catch (Exception e) {
			petType = null;
		}
        assertThat(petType).isNull();
    }

    @Test
    void shouldFindSpecialtyById(){
    	Specialty specialty = this.clinicService.findSpecialtyById(1);
    	assertThat(specialty.getName()).isEqualTo("radiology");
    }

    @Test
    void shouldFindAllSpecialtys(){
        Collection<Specialty> specialties = this.clinicService.findAllSpecialties();
        Specialty specialty1 = specialties.stream().filter(e -> e.getId() == 1).findFirst().get();
        assertThat(specialty1.getName()).isEqualTo("radiology");
        Specialty specialty3 = specialties.stream().filter(e -> e.getId() == 3).findFirst().get();
        assertThat(specialty3.getName()).isEqualTo("dentistry");
    }

    @Test
    @Transactional
    void shouldInsertSpecialty() {
        Collection<Specialty> specialties = this.clinicService.findAllSpecialties();
        int found = specialties.size();

        Specialty specialty = new Specialty();
        specialty.setName("dermatologist");

        this.clinicService.saveSpecialty(specialty);
        assertThat(specialty.getId().longValue()).isNotEqualTo(0);

        specialties = this.clinicService.findAllSpecialties();
        assertThat(specialties.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdateSpecialty(){
    	Specialty specialty = this.clinicService.findSpecialtyById(1);
    	String oldLastName = specialty.getName();
        String newLastName = oldLastName + "X";
        specialty.setName(newLastName);
        this.clinicService.saveSpecialty(specialty);
        specialty = this.clinicService.findSpecialtyById(1);
        assertThat(specialty.getName()).isEqualTo(newLastName);
    }

    @Test
    @Transactional
    void shouldDeleteSpecialty(){
        Specialty specialty = new Specialty();
        specialty.setName("test");
        this.clinicService.saveSpecialty(specialty);
        Integer specialtyId = specialty.getId();
        assertThat(specialtyId).isNotNull();
    	specialty = this.clinicService.findSpecialtyById(specialtyId);
        assertThat(specialty).isNotNull();
        this.clinicService.deleteSpecialty(specialty);
        try {
        	specialty = this.clinicService.findSpecialtyById(specialtyId);
		} catch (Exception e) {
			specialty = null;
		}
        assertThat(specialty).isNull();
    }
}
