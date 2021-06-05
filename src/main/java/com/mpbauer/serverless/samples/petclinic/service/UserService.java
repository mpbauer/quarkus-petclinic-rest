package com.mpbauer.serverless.samples.petclinic.service;

import com.mpbauer.serverless.samples.petclinic.model.User;

public interface UserService {

    void saveUser(User user) throws Exception;
}
