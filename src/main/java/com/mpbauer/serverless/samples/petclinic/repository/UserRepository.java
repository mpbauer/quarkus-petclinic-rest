package com.mpbauer.serverless.samples.petclinic.repository;

import com.mpbauer.serverless.samples.petclinic.model.User;

public interface UserRepository {

    void save(User user);
}
