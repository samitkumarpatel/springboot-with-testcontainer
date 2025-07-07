package com.example.springboot_with_testcontainer.temporal;

import com.example.springboot_with_testcontainer.model.Customer;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class UserCreationWorkflowImpl implements UserCreationWorkflow {

    RetryOptions retryOptions = RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(1))
            .setMaximumInterval(Duration.ofSeconds(100))
            .setBackoffCoefficient(2)
            .setMaximumAttempts(2)
            .build();

    ActivityOptions options = ActivityOptions.newBuilder()
            .setRetryOptions(retryOptions)
            .setStartToCloseTimeout(Duration.ofMinutes(5))
            .setScheduleToCloseTimeout(Duration.ofMinutes(5))
            .build();

    UserCreationActivity userCreationActivity = Workflow.newActivityStub(UserCreationActivity.class, options);

    private Customer customer;
    private boolean completed = false;

    @Override
    public void newUserFlow(Customer customer) {
        this.customer = new Customer(customer.id(), customer.firstName(), customer.lastName());
        System.out.println("Starting new user flow for: " + customer);

        Workflow.await(() -> completed);
    }

    @Override
    public void approved(Customer customer) {
        System.out.println("User approved: " + customer);
        userCreationActivity.persist(customer);
        this.customer = customer;
        completed = true; // Mark workflow as completed
    }

    @Override
    public void rejected(Customer customer) {
        System.out.println("User rejected: " + customer);
        this.customer = null; // Clear customer on rejection
        completed = true; // Mark workflow as completed
    }

    @Override
    public Customer getCustomer() {
        return this.customer;
    }
}
