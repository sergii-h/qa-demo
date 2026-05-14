package com.example.demo.integration.test.pact.api;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.example.demo.data.Task;

import java.util.Map;
import java.util.UUID;

@Provider("demo-service-tasks-update")
public class UpdateTaskPactProviderTest extends PactProviderTestBase {

    @State("a task exists to update and title is unique")
    Map<String, Object> taskExistsToUpdate() {
        Task task = taskRepository.save(buildTask(UUID.randomUUID().toString()));
        return Map.of("taskId", task.getId(), "updatedTitle", UUID.randomUUID().toString());
    }

    @State("another task has the requested title")
    Map<String, Object> anotherTaskHasRequestedTitle() {
        Task taskToUpdate = taskRepository.save(buildTask(UUID.randomUUID().toString()));
        String duplicateTitle = UUID.randomUUID().toString();
        taskRepository.save(buildTask(duplicateTitle));
        return Map.of("taskId", taskToUpdate.getId(), "updatedTitle", duplicateTitle);
    }
}
