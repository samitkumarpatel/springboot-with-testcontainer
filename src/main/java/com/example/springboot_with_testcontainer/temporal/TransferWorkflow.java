package com.example.springboot_with_testcontainer.temporal;

import com.example.springboot_with_testcontainer.model.Transaction;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

// https://learn.temporal.io/getting_started/java/first_program_in_java/
@WorkflowInterface
public interface TransferWorkflow {
    @WorkflowMethod
    void transfer(Transaction transaction);
}
