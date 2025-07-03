package com.example.springboot_with_testcontainer.temporal;


import com.example.springboot_with_testcontainer.model.Transaction;

public class TransferWorkflowImpl implements TransferWorkflow {
    @Override
    public void transfer(Transaction transaction) {
        System.out.println("Transferring " + transaction.amount() + " from " + transaction.from() + " to " + transaction.to());
    }
}
