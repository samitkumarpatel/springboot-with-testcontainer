package com.example.springboot_with_testcontainer.temporal;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

// Temporal
@ActivityInterface
public interface TransferActivity {
    @ActivityMethod
    public void withdraw(Long accountId, String referenceId, Double amount);

    @ActivityMethod
    public void deposit(Long accountId, String referenceId, Double amount);

    @ActivityMethod
    public void refund(Long accountId, String referenceId, Double amount);

    @ActivityMethod
    public void notification(Long accountId, String referenceId, Double amount, String message);
}
