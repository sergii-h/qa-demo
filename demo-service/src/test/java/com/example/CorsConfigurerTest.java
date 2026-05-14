package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorsConfigurerTest {

    @Mock
    private CorsRegistry corsRegistry;

    @Mock
    private CorsRegistration corsRegistration;

    @InjectMocks
    private CorsConfigurer corsConfigurer;

    @BeforeEach
    void setUp() {
        when(corsRegistry.addMapping(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.allowedMethods(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.allowedOriginPatterns(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.allowCredentials(true)).thenReturn(corsRegistration);
    }

    @Test
    void shouldConfigureCorsWithAllMappingsWhenAddCorsMappingsCalled() {
        ArgumentCaptor<String> mappingCaptor = ArgumentCaptor.forClass(String.class);

        corsConfigurer.addCorsMappings(corsRegistry);

        verify(corsRegistry).addMapping(mappingCaptor.capture());
        assertThat(mappingCaptor.getValue(), is("/**"));
        verify(corsRegistration).allowedMethods("*");
        verify(corsRegistration).allowedOriginPatterns("*");
        verify(corsRegistration).allowCredentials(true);
    }
}

