package com.example.demo.integration.test.pact.api;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import com.example.demo.TaskRepository;
import com.example.demo.data.Task;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskStatus;
import com.example.demo.integration.ApiIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@PactBroker(url = "${PACT_BROKER_BASE_URL:http://localhost:9292}")
@EnableWireMock(@ConfigureWireMock(port = 8085))
public abstract class PactProviderTestBase extends ApiIntegrationTestBase {

    @Autowired
    protected TaskRepository taskRepository;

    @BeforeEach
    void setupPactTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    protected Task buildTask(String title) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        return Task.builder()
                .title(title)
                .description("Document release tasks")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .createdDate(now)
                .updatedDate(now)
                .build();
    }
}
