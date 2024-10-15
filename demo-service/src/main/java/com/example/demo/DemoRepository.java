package com.example.demo;

import com.example.demo.data.DemoRequest;
import com.example.demo.data.DemoData;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
class DemoRepository {

    private final Set<DemoData> items = new HashSet<>();

    DemoData create(DemoRequest demoRequest) {

        DemoData demoResponse = DemoData.builder()
                                       .id(String.valueOf(items.size() + 1))
                                       .name(demoRequest.getName())
                                       .description(demoRequest.getDescription())
                                       .amount(demoRequest.getAmount())
                                       .build();
        items.add(demoResponse);

        return demoResponse;
    }

    void update(String itemId, DemoRequest demoRequest) {
        DemoData demoResponse = getById(itemId);

        items.remove(demoResponse);
        items.add(
                demoResponse.toBuilder()
                        .name(demoRequest.getName())
                        .amount(demoRequest.getAmount())
                        .description(demoRequest.getDescription())
                        .build()
        );
    }

    void delete(String itemId) {
        DemoData demoResponse = getById(itemId);
        items.remove(demoResponse);
    }

    DemoData getById(String itemId) {
        return items.stream().filter(demoResponse -> demoResponse.getId().equals(itemId)).findAny()
                     .orElseThrow(() -> new NoSuchElementException("Item with id: " + itemId + " not found"));
    }

    Set<DemoData> findAll() {
        return items.stream().map(demoResponse -> demoResponse.toBuilder().build()).collect(Collectors.toSet());
    }
}