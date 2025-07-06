package com.example.springboot_with_testcontainer.temporal;

import com.example.springboot_with_testcontainer.model.Customer;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface UserCreationActivity {
    void persist(Customer customer);
}
