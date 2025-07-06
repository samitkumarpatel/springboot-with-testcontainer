package com.example.springboot_with_testcontainer.repository;

import com.example.springboot_with_testcontainer.model.Customer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> { }
