package com.example.demo;

import jakarta.annotation.Nonnull;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.data.DemoData;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends MongoRepository<DemoData, String> {
    @Nonnull
    Optional<DemoData> findById(@Nonnull String itemId);

    @Nonnull
    List<DemoData> findAll();
}
