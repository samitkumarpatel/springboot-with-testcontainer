package com.example.springboot_with_testcontainer.temporal;

import com.example.springboot_with_testcontainer.model.Transaction;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface TransferWorkflow {
    @WorkflowMethod
    public void transfer(Transaction transaction);
}
