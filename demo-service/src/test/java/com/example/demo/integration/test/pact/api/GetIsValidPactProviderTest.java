package com.example.demo.integration.test.pact.api;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.example.demo.data.Task;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

@Provider("demo-service-tasks-get-is-valid")
@EnableWireMock(@ConfigureWireMock(port = 8085))
@ResourceLock("wiremock-8085")
public class GetIsValidPactProviderTest extends PactProviderTestBase {

    @Autowired
    private WireMockServer wireMockServer;

    @State("validation result is true for the task")
    Map<String, Object> validationResultIsTrue() {
        Task task = taskRepository.save(buildTask(UUID.randomUUID().toString()));
        wireMockServer.stubFor(post("/external/validate/task")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("true")));
        return Map.of("taskId", task.getId());
    }
}
