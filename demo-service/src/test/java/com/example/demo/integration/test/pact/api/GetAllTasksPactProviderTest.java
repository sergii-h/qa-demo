package com.example.demo.integration.test.pact.api;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;

import java.util.UUID;

@Provider("demo-service-tasks-get-all")
public class GetAllTasksPactProviderTest extends PactProviderTestBase {

    @State("tasks exist")
    void tasksExist() {
        taskRepository.save(buildTask(UUID.randomUUID().toString()));
    }
}
