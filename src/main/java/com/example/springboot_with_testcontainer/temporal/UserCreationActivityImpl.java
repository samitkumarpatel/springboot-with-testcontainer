package com.example.springboot_with_testcontainer.temporal;

import com.example.springboot_with_testcontainer.model.Customer;

public class UserCreationActivityImpl implements UserCreationActivity {

    @Override
    public void persist(Customer customer) {
        System.out.println("Persisting customer: " + customer);
    }
}
