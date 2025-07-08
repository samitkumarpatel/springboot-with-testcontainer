package com.example.springboot_with_testcontainer.model;

import lombok.Builder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Builder(toBuilder = true)
public record Customer(@MongoId String id, String firstName, String lastName, String approvalComments) { }
