package com.example.springboot_with_testcontainer.temporal;

import com.example.springboot_with_testcontainer.model.Transaction;
import com.example.springboot_with_testcontainer.utility.AccountNotFoundException;
import com.example.springboot_with_testcontainer.utility.InsufficientBalanceException;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.UUID;

public class TransferWorkflowImpl implements TransferWorkflow {
    final RetryOptions retryOptions = RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(20))
            .setBackoffCoefficient(2)
            .setMaximumAttempts(3)
            .build();
    final ActivityOptions options = ActivityOptions.newBuilder()
            .setRetryOptions(retryOptions)
            .setStartToCloseTimeout(Duration.ofMinutes(1))
            .setScheduleToCloseTimeout(Duration.ofMinutes(1))
            .build();

    final TransferActivity accountActivity = Workflow.newActivityStub(TransferActivity.class, options);
    final UUID uuid = UUID.randomUUID();
    @Override
    public void transfer(Transaction transaction) {
        System.out.println("Transferring " + transaction.amount() + " from " + transaction.from() + " to " + transaction.to());

        try {
            accountActivity.withdraw(transaction.from(), uuid.toString(), transaction.amount());
            accountActivity.deposit(transaction.to(), uuid.toString(), transaction.amount());
            accountActivity.notification(transaction.from(), uuid.toString(), transaction.amount(), "Transfer successful to " + transaction.to());
            accountActivity.notification(transaction.to(), uuid.toString(), transaction.amount(), "Transfer received from " + transaction.from());
        } catch (AccountNotFoundException | InsufficientBalanceException a) {
            System.out.println(a.getMessage());
            throw a;
        } catch (RuntimeException e) {
            System.out.println("Transfer failed, refunding " + transaction.amount() + " to " + transaction.from());
            accountActivity.refund(transaction.from(), uuid.toString(), transaction.amount());
            throw e; // rethrow the exception to signal failure
        }
    }
}
