package com.example.springboot_with_testcontainer.temporal;

import com.example.springboot_with_testcontainer.model.Customer;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface UserCreationWorkflow {
    @WorkflowMethod
    void newUserFlow(Customer customer);

    @SignalMethod
    void approved(Customer customer);

    @SignalMethod
    void rejected(Customer customer);

    @QueryMethod
    Customer getCustomer();
}
