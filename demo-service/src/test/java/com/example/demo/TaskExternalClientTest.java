package com.example.demo;

import com.example.demo.context.TaskTestContext;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskRequest;
import com.example.demo.data.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskExternalClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private TaskExternalClient taskExternalClient;

    TaskTestContext context;

    @BeforeEach
    void beforeEach() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        context = TaskTestContext.builder().build();
    }

    @Test
    void shouldReturnTrueWhenTaskIsValid() {
        // given
        when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(true));

        // when
        Mono<Boolean> result = taskExternalClient.validateTask(context.createTaskRequest());

        // then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldReturnFalseWhenTaskIsInvalid() {
        // given
         when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(false));

        // when
        Mono<Boolean> result = taskExternalClient.validateTask(context.createTaskRequest());

        // then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldHandleErrorWhenExternalServiceFails() {
        // given
        when(responseSpec.bodyToMono(Boolean.class))
                .thenReturn(Mono.error(new RuntimeException("External service error")));

        // when
        Mono<Boolean> result = taskExternalClient.validateTask(context.createTaskRequest());

        // then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
}
