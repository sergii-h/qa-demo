package com.example.demo;

import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/items")
class DemoController {
    private final ItemRepository repository;
    private final DemoEventProducer demoEventProducer;

    @Autowired
    public DemoController(ItemRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    DemoResponse createItem(@RequestBody @Valid DemoRequest demoRequest) {
        DemoData item = DemoData.builder()
                .id(String.valueOf(new ObjectId()))
                .name(demoRequest.getName())
                .description(demoRequest.getDescription())
                .amount(demoRequest.getAmount())
                .build();

        demoEventProducer.produce(item);

        return repository.insert(item);
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
