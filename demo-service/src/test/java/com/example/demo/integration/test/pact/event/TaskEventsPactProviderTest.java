package com.example.demo.integration.test.pact.event;

import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.PactVerifyProvider;
import com.example.demo.data.TaskEvent;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskStatus;
import com.example.demo.integration.ApiIntegrationTestBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

@Provider("demo-service-tasks-events")
@PactBroker(url = "${PACT_BROKER_BASE_URL:http://localhost:9292}")
public class TaskEventsPactProviderTest extends ApiIntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setMessageTarget(PactVerificationContext context) {
        context.setTarget(new MessageTestTarget());
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @PactVerifyProvider("a task event")
    String taskEvent() throws JsonProcessingException {
        return objectMapper.writeValueAsString(TaskEvent.builder()
                .taskId("507f1f77bcf86cd799439011")
                .title("Fix critical bug")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .timestamp(Instant.now())
                .eventType("CREATED")
                .build());
    }
}
