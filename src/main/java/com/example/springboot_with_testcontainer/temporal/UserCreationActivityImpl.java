package com.example.springboot_with_testcontainer.temporal;

import com.example.springboot_with_testcontainer.model.Customer;
import com.example.springboot_with_testcontainer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserCreationActivityImpl implements UserCreationActivity {
    final CustomerRepository customerRepository;

    @Override
    public void persist(Customer customer) {
        System.out.println("Persisting customer: " + customer);
        customerRepository.save(customer).subscribe(
            savedCustomer -> System.out.println("Customer saved: " + savedCustomer),
            error -> System.err.println("Error saving customer: " + error.getMessage())
        );
    }
}
