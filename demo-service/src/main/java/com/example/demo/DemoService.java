package com.example.demo;

import com.example.demo.data.DemoRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
class DemoService {
    @Autowired
    private WebClient webClient;

    public Mono<String> validateItem(DemoRequest demoRequest) {
        return webClient.post()
                .uri("/external/validate/item")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(demoRequest)
                .retrieve().bodyToMono(String.class)
                .map(String::toLowerCase)
                .doOnTerminate(
                        () -> log.info("Demo request with name '{}' validation processed", demoRequest.getName())
                );
    }
}
