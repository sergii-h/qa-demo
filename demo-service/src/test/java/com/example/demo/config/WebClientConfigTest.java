package com.example.demo.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebClientConfigTest {

    @Mock
    private WebClient.Builder builder;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private WebClientConfig webClientConfig;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(webClientConfig, "baseUrl", "http://localhost:8080");
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);
    }

    @Test
    void shouldCreateWebClientWithBaseUrlWhenWebClientCalled() {
        WebClient result = webClientConfig.webClient(builder);

        assertThat(result, is(notNullValue()));
        verify(builder).baseUrl("http://localhost:8080");
        verify(builder).build();
    }
}

