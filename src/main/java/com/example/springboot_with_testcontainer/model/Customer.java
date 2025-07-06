package com.example.springboot_with_testcontainer.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
public record Customer(@MongoId String id, String firstName, String lastName) { }
