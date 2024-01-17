package com.example.demo;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
class DemoRepository {

    private final Set<DemoData> items = new HashSet<>();

    DemoData create(DemoRequest demoRequest) {

        DemoData demoData = DemoData.builder()
                                       .id(String.valueOf(items.size() + 1))
                                       .name(demoRequest.getName())
                                       .description(demoRequest.getDescription())
                                       .amount(demoRequest.getAmount())
                                       .build();
        items.add(demoData);

        return demoData;
    }

    void update(String itemId, DemoRequest demoRequest) {
        DemoData demoData = getById(itemId);

        items.remove(demoData);
        items.add(
                demoData.toBuilder()
                        .name(demoRequest.getName())
                        .amount(demoRequest.getAmount())
                        .description(demoRequest.getDescription())
                        .build()
        );
    }

    void delete(String itemId) {
        DemoData demoData = getById(itemId);
        items.remove(demoData);
    }

    DemoData getById(String itemId) {
        return items.stream().filter(demoData -> demoData.getId().equals(itemId)).findAny()
                     .orElseThrow(() -> new NoSuchElementException("Item with id: " + itemId + " not found"));
    }

    Set<DemoData> findAll() {
        return items.stream().map(demoData -> demoData.toBuilder().build()).collect(Collectors.toSet());
    }
}