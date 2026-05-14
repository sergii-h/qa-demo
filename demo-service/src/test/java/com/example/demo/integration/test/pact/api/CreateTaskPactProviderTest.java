package com.example.demo.integration.test.pact.api;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;

import java.util.Map;
import java.util.UUID;

@Provider("demo-service-tasks-create")
public class CreateTaskPactProviderTest extends PactProviderTestBase {

    @State("task title is unique")
    Map<String, Object> taskTitleIsUnique() {
        return Map.of("taskTitle", UUID.randomUUID().toString());
    }

    @State("task title already exists")
    Map<String, Object> taskTitleAlreadyExists() {
        String uniqueTitle = UUID.randomUUID().toString();
        taskRepository.save(buildTask(uniqueTitle));
        return Map.of("taskTitle", uniqueTitle);
    }
}
