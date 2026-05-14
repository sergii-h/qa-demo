package com.example.demo;

import com.example.demo.data.TaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class TaskExternalClient {
    @Autowired
    private WebClient webClient;

    public Mono<Boolean> validateTask(TaskRequest taskRequest) {
        return webClient.post()
                .uri("/external/validate/task")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(taskRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnTerminate(
                        () -> log.info("Task validation processed for title '{}'", taskRequest.getTitle())
                );
    }
}

