package com.example.springboot_with_testcontainer.temporal;

import com.example.springboot_with_testcontainer.repository.AccountRepository;
import com.example.springboot_with_testcontainer.utility.AccountNotFoundException;
import com.example.springboot_with_testcontainer.utility.InsufficientBalanceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TransferActivityImpl implements TransferActivity {
    final AccountRepository accountRepository;

    @Override
    public void withdraw(Long accountId, String referenceId, Double amount) {
        System.out.println("Withdrawing " + amount + " from account " + accountId + " with reference " + referenceId);
        // Logic to withdraw amount from the account
        var account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " does not exist."));

        if (account.amount() < amount) {
            throw new InsufficientBalanceException("Insufficient balance for account " + accountId);
        } else {
            var accountWithNewBalance = account.toBuilder().amount(account.amount() - amount).build();
            accountRepository.save(accountWithNewBalance);
        }

    }

    @Override
    public void deposit(Long accountId, String referenceId, Double amount) {
        System.out.println("Depositing " + amount + " to account " + accountId + " with reference " + referenceId);
        // Logic to deposit amount to the account
        var account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " does not exist."));

        var accountWithNewBalance = account.toBuilder().amount(account.amount() + amount).build();
        accountRepository.save(accountWithNewBalance);
    }

    @Override
    public void refund(Long accountId, String referenceId, Double amount) {
        System.out.println("Refunding " + amount + " to account " + accountId + " with reference " + referenceId);
        // Logic to refund amount to the account
        var account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " does not exist."));
        var accountWithNewBalance = account.toBuilder().amount(account.amount() + amount).build();
        accountRepository.save(accountWithNewBalance);
    }

    @Override
    public void notification(Long accountId, String referenceId, Double amount, String message) {
        System.out.println("Sending notification for account " + accountId + " with reference " + referenceId + ": " + message);
        // Logic to send notification
    }
}
