package com.example.demo.integration.test.pact.api;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.example.demo.data.Task;

import java.util.Map;
import java.util.UUID;

@Provider("demo-service-tasks-get-by-id")
public class GetTaskByIdPactProviderTest extends PactProviderTestBase {

    @State("a task exists")
    Map<String, Object> taskExists() {
        Task task = taskRepository.save(buildTask(UUID.randomUUID().toString()));
        return Map.of("taskId", task.getId());
    }
}
