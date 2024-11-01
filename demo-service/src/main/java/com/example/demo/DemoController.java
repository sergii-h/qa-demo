package com.example.demo;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.data.DemoData;
import com.example.demo.data.DemoRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/items")
class DemoController {
    private final ItemRepository repository;
    private final DemoEventProducer demoEventProducer;

    public DemoController(ItemRepository repository, DemoEventProducer demoEventProducer) {
        this.repository = repository;
        this.demoEventProducer = demoEventProducer;
    }

    @PostMapping
    DemoData createItem(@RequestBody @Valid DemoRequest demoRequest) {
        DemoData item = DemoData.builder()
                .id(String.valueOf(new ObjectId()))
                .name(demoRequest.getName())
                .description(demoRequest.getDescription())
                .amount(demoRequest.getAmount())
                .build();

        DemoData response = repository.insert(item);
        demoEventProducer.produce(item);

        return response;
    }

    @PutMapping("/{itemId}")
    void updateItem(@PathVariable("itemId") String itemId, @RequestBody DemoRequest demoRequest) {
        DemoData item = repository.findById(itemId).orElseThrow();

        DemoData changedItem = DemoData.builder()
                .id(item.getId())
                .name(demoRequest.getName())
                .description(demoRequest.getDescription())
                .amount(demoRequest.getAmount())
                .build();

        repository.save(changedItem);
    }

    @DeleteMapping("/{itemId}")
    void deleteItem(@PathVariable("itemId") String itemId) {
        DemoData item = repository.findById(itemId).orElseThrow();
        repository.delete(item);
    }

    @GetMapping("/{itemId}")
    DemoData getItem(@PathVariable("itemId") String itemId) {
        return repository.findById(itemId).orElseThrow();
    }

    @GetMapping
    List<DemoData> getItems() {
        return repository.findAll();
    }
}
