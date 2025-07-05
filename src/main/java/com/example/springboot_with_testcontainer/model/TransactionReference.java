package com.example.springboot_with_testcontainer.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
public record TransactionReference(@MongoId String id, Long accountId, String transactionId, Long from, Long to, Double amount) { }
