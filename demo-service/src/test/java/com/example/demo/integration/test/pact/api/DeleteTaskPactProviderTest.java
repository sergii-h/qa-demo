package com.example.demo.integration.test.pact.api;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.example.demo.data.Task;

import java.util.Map;
import java.util.UUID;

@Provider("demo-service-tasks-delete")
public class DeleteTaskPactProviderTest extends PactProviderTestBase {

    @State("a task exists to delete")
    Map<String, Object> taskExistsToDelete() {
        Task task = taskRepository.save(buildTask(UUID.randomUUID().toString()));
        return Map.of("taskId", task.getId());
    }
}
