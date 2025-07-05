package com.example.springboot_with_testcontainer.repository;

import com.example.springboot_with_testcontainer.model.Account;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

public interface AccountRepository extends ListCrudRepository<Account, Long> {}
