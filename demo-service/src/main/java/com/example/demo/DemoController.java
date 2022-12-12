package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/item")
class DemoController {

    private final DemoRepository demoRepository;

    @PostMapping
    DemoData createItem(@RequestBody @Valid DemoRequest demoRequest) {
        return demoRepository.create(demoRequest);
    }

    @PutMapping("/{itemId}")
    void updateItem(@PathVariable("itemId") String itemId, @RequestBody DemoRequest demoRequest) {
        demoRepository.update(itemId, demoRequest);
    }

    @DeleteMapping("/{itemId}")
    void deleteItem(@PathVariable("itemId") String itemId) {
        demoRepository.delete(itemId);
    }

    @GetMapping("/{itemId}")
    DemoData getItem(@PathVariable("itemId") String itemId) {
        return demoRepository.getById(itemId);
    }

    @GetMapping
    Set<DemoData> getItems() {
        return demoRepository.findAll();
    }
}
