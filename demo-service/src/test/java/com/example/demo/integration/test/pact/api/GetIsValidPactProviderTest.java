package com.example.demo.integration.test.pact.api;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.example.demo.data.Task;
import com.github.tomakehurst.wiremock.client.WireMock;

import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

@Provider("demo-service-tasks-get-is-valid")
public class GetIsValidPactProviderTest extends PactProviderTestBase {

    @State("validation result is true for the task")
    Map<String, Object> validationResultIsTrue() {
        Task task = taskRepository.save(buildTask(UUID.randomUUID().toString()));
        WireMock.stubFor(post("/external/validate/task")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("true")));
        return Map.of("taskId", task.getId());
    }
}
