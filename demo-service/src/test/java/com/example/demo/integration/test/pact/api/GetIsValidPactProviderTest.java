package com.example.demo.integration.test.pact.api;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.example.demo.data.Task;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

@Provider("demo-service-tasks-get-is-valid")
@EnableWireMock(@ConfigureWireMock(
        port = 0,
        baseUrlProperties = "external.service.url"
))
public class GetIsValidPactProviderTest extends PactProviderTestBase {

    @State("validation result is true for the task")
    Map<String, Object> validationResultIsTrue() {
        Task task = taskRepository.save(buildTask(UUID.randomUUID().toString()));
        stubFor(post("/external/validate/task")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("true")));
        return Map.of("taskId", task.getId());
    }
}
