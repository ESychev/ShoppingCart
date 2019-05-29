package org.o7planning.sbshoppingcart.dao;

import org.o7planning.sbshoppingcart.entity.Account;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Transactional
@Repository
public class AccountDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public Account findAccount(String userName) {
        return entityManager.find(Account.class, userName);
    }

}
