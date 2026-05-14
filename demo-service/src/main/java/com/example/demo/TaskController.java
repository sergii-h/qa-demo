package com.example.demo;

import com.example.demo.data.Task;
import com.example.demo.data.TaskRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/v1/tasks")
@AllArgsConstructor
public class TaskController {
    private final TaskRepository repository;
    private final TaskEventProducer taskEventProducer;
    private final TaskExternalClient taskExternalClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Task createTask(@RequestBody @Valid TaskRequest taskRequest) {
        Task existingTaskWithTitle = repository.findByTitle(taskRequest.getTitle());
        if (existingTaskWithTitle != null) {
            throw new DuplicateTitleException("Task with title '" + taskRequest.getTitle() + "' already exists");
        }

        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Task task = Task.builder()
                .id(String.valueOf(new ObjectId()))
                .title(taskRequest.getTitle())
                .description(taskRequest.getDescription())
                .status(taskRequest.getStatus())
                .priority(taskRequest.getPriority())
                .createdDate(now)
                .updatedDate(now)
                .build();

        Task savedTask = repository.insert(task);
        taskEventProducer.produceTaskCreated(savedTask);

        return savedTask;
    }

    @PutMapping("/{taskId}")
    Task updateTask(@PathVariable("taskId") String taskId, @RequestBody @Valid TaskRequest taskRequest) {
        Task existingTask = repository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        if (!existingTask.getTitle().equals(taskRequest.getTitle())) {
            Task existingTaskWithTitle = repository.findByTitle(taskRequest.getTitle());
            if (existingTaskWithTitle != null) {
                throw new DuplicateTitleException("Task with title '" + taskRequest.getTitle() + "' already exists");
            }
        }

        Task updatedTask = Task.builder()
                .id(existingTask.getId())
                .title(taskRequest.getTitle())
                .description(taskRequest.getDescription())
                .status(taskRequest.getStatus())
                .priority(taskRequest.getPriority())
                .createdDate(existingTask.getCreatedDate())
                .updatedDate(Instant.now().truncatedTo(ChronoUnit.MILLIS))
                .build();

        Task savedTask = repository.save(updatedTask);
        taskEventProducer.produceTaskUpdated(savedTask);

        return savedTask;
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteTask(@PathVariable("taskId") String taskId) {
        Task task = repository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
        
        repository.delete(task);
        taskEventProducer.produceTaskDeleted(task);
    }

    @GetMapping("/{taskId}")
    Task getTask(@PathVariable("taskId") String taskId) {
        return repository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
    }

    @GetMapping
    List<Task> getTasks() {
        return repository.findAll();
    }

    @GetMapping("/isValid/{taskId}")
    Boolean isValid(@PathVariable("taskId") String taskId) {
        Task task = repository.findById(taskId).orElse(null);

        if (task == null) {
            return false;
        }

        return taskExternalClient.validateTask(
                TaskRequest.builder()
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .status(task.getStatus())
                        .priority(task.getPriority())
                        .build()
        ).block();
    }
}

