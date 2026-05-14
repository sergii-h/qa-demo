package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class DemoServiceApplicationTest {

    @Test
    void shouldRunApplicationWhenMainMethodCalled() {
        try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {
            String[] args = {"arg1", "arg2"};

            DemoServiceApplication.main(args);

            springApplicationMock.verify(() -> 
                SpringApplication.run(eq(DemoServiceApplication.class), eq(args))
            );
        }
    }

    @Test
    void shouldCreateInstanceWhenConstructorCalled() {
        DemoServiceApplication application = new DemoServiceApplication();

        assertThat(application, notNullValue());
    }
}
