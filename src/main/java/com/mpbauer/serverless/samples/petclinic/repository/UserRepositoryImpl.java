package com.mpbauer.serverless.samples.petclinic.repository;

import com.mpbauer.serverless.samples.petclinic.model.User;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@ApplicationScoped
public class UserRepositoryImpl implements UserRepository {

    @Inject
    EntityManager em;

    @Override
    public void save(User user) {
        if (this.em.find(User.class, user.getUsername()) == null) {
            this.em.persist(user);
        } else {
            this.em.merge(user);
        }
    }
}
