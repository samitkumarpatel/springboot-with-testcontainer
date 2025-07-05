package com.example.springboot_with_testcontainer.model;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table
@Builder(toBuilder = true)
public record Account(@Id Long id, String name, Double amount) { }
