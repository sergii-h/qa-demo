package com.example.demo;

import com.example.demo.context.TaskTestContext;
import com.example.demo.data.Task;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskRequest;
import com.example.demo.data.TaskStatus;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {
    @Mock
    private TaskRepository repository;

    @Mock
    private TaskEventProducer taskEventProducer;

    @Mock
    private TaskExternalClient taskExternalClient;

    @InjectMocks
    private TaskController taskController;

    TaskTestContext context;

    @BeforeEach()
    void beforeEach() {
        context = TaskTestContext.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .build();
    }

    @Nested
    class CreateTaskTest {
        @Test
        void shouldCreateTaskWhenValidRequestProvided() {
            // given
            Task savedTask = context.createTask();
            when(repository.insert(any(Task.class))).thenReturn(savedTask);

            // when
            Task result = taskController.createTask(context.createTaskRequest());

            // then
            assertThat(result, is(savedTask));
        }

        @Test
        void shouldFindByTitleWithTitleProvided() {
            // given
            when(repository.findByTitle(context.getTitle())).thenReturn(null);

            // when
            taskController.createTask(context.createTaskRequest());

            // then
            verify(repository).findByTitle(context.getTitle());
        }

        @Test
        void shouldThrowDuplicateTitleExceptionWhenTitleAlreadyExists() {
            // given
            when(repository.findByTitle(context.getTitle())).thenReturn(context.createTask());

            // when
            DuplicateTitleException exception = assertThrows(
                    DuplicateTitleException.class,
                    () -> taskController.createTask(context.createTaskRequest())
            );

            // then
            assertEquals("Task with title '" + context.getTitle() + "' already exists", exception.getMessage());
            verify(repository, never()).insert(any(Task.class));
            verify(taskEventProducer, never()).produceTaskCreated(any());
        }

        @Test
        void shouldCallInsertWithTask() {
            // given
            ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

            // when
            taskController.createTask(context.createTaskRequest());

            // then
            verify(repository).insert(taskCaptor.capture());

            Task capturedTask = taskCaptor.getValue();
            
            // Update context with dynamic fields to validate complete object
            context.setId(capturedTask.getId());
            context.setUpdatedDate(capturedTask.getUpdatedDate());
            context.setCreatedDate(capturedTask.getCreatedDate());

            assertThat(capturedTask, is(context.createTask()));
            assertThat(capturedTask.getCreatedDate(), is(capturedTask.getUpdatedDate()));
        }

        @Test
        void shouldCallProducerWithSavedTask() {
            // given
            Task savedTask = context.createTask();
            when(repository.insert(any(Task.class))).thenReturn(savedTask);

            // when
            taskController.createTask(context.createTaskRequest());

            // then
            verify(taskEventProducer, times(1)).produceTaskCreated(savedTask);
            verify(taskEventProducer, never()).produceTaskUpdated(any());
            verify(taskEventProducer, never()).produceTaskDeleted(any());
        }

        @Test
        void shouldNotProduceEventWhenRepositoryThrows() {
            // given
            doThrow(new RuntimeException("DB error"))
                    .when(repository)
                    .insert(any(Task.class));

            // when
            assertThrows(
                    RuntimeException.class,
                    () -> taskController.createTask(context.createTaskRequest())
            );

            // then
            verify(taskEventProducer, never()).produceTaskCreated(any());
        }
    }

    @Nested
    class UpdateTaskTest {
        @Test
        void shouldUpdateTaskWhenValidRequestProvided() {
            // given
            Instant createdDate = Instant.parse("2024-01-01T10:00:00Z");

            TaskTestContext existingContext = TaskTestContext.builder()
                    .title("Old Title")
                    .description("Old Description")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.LOW)
                    .createdDate(createdDate)
                    .updatedDate(createdDate)
                    .build();

            TaskTestContext updateContext = TaskTestContext.builder()
                    .id(existingContext.getId())
                    .title("Updated Title")
                    .description("Updated Description")
                    .status(TaskStatus.IN_PROGRESS)
                    .priority(TaskPriority.HIGH)
                    .build();

            Task updatedTask = updateContext.createTask();

            when(repository.findById(existingContext.getId())).thenReturn(Optional.of(existingContext.createTask()));
            when(repository.save(any(Task.class))).thenReturn(updatedTask);

            // when
            Task result = taskController.updateTask(existingContext.getId(), updateContext.createTaskRequest());

            // then
            assertThat(result, is(updatedTask));
        }

        @Test
        void shouldThrowTaskNotFoundExceptionWhenTaskDoesNotExist() {
            // given
            when(repository.findById(context.getId())).thenReturn(Optional.empty());

            // then
            TaskNotFoundException exception = assertThrows(
                    TaskNotFoundException.class,
                    () -> taskController.updateTask(context.getId(), context.createTaskRequest())
            );

            assertEquals("Task not found with id: " + context.getId(), exception.getMessage());
            verify(repository, never()).findByTitle(any());
            verify(repository, never()).save(any(Task.class));
            verify(taskEventProducer, never()).produceTaskUpdated(any(Task.class));
        }

        @Test
        void shouldThrowDuplicateTitleExceptionWhenUpdatingToExistingTitle() {
            // given
            TaskTestContext updateContext = TaskTestContext.builder()
                    .title("Updated Title")
                    .build();

            when(repository.findById(context.getId())).thenReturn(Optional.of(context.createTask()));
            when(repository.findByTitle(updateContext.getTitle())).thenReturn(context.createTask());

            // then
            DuplicateTitleException exception = assertThrows(
                    DuplicateTitleException.class,
                    () -> taskController.updateTask(context.getId(), updateContext.createTaskRequest())
            );

            assertEquals("Task with title '" + updateContext.getTitle() + "' already exists", exception.getMessage());
            verify(repository, never()).save(any(Task.class));
            verify(taskEventProducer, never()).produceTaskUpdated(any(Task.class));
        }

        @Test
        void shouldCallSaveWithTask() {
            // given
            Instant createdDate = Instant.parse("2024-01-01T10:00:00Z");

            TaskTestContext existingContext = TaskTestContext.builder()
                    .title("Old Title")
                    .description("Old Description")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.LOW)
                    .createdDate(createdDate)
                    .updatedDate(createdDate)
                    .build();

            TaskTestContext updateContext = TaskTestContext.builder()
                    .id(existingContext.getId())
                    .title("Updated Title")
                    .description("Updated Description")
                    .status(TaskStatus.IN_PROGRESS)
                    .priority(TaskPriority.HIGH)
                    .createdDate(existingContext.getCreatedDate())
                    .build();

            when(repository.findById(existingContext.getId())).thenReturn(Optional.of(existingContext.createTask()));
            when(repository.save(any(Task.class))).thenReturn(updateContext.createTask());

            ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

            // when
            taskController.updateTask(existingContext.getId(), updateContext.createTaskRequest());

            // then
            verify(repository).save(taskCaptor.capture());

            Task capturedTask = taskCaptor.getValue();
            
            // Update context with dynamic field to validate complete object
            updateContext.setUpdatedDate(capturedTask.getUpdatedDate());

            assertThat(capturedTask, is(updateContext.createTask()));
            assertTrue(capturedTask.getUpdatedDate().isAfter(capturedTask.getCreatedDate()));
        }

        @Test
        void shouldCallProducerWithSavedTask() {
            // given
            Task updatedTask = context.createTask();

            when(repository.findById(context.getId())).thenReturn(Optional.of(context.createTask()));
            when(repository.save(any(Task.class))).thenReturn(updatedTask);

            // when
            taskController.updateTask(context.getId(), context.createTaskRequest());

            // then
            verify(taskEventProducer, times(1)).produceTaskUpdated(updatedTask);
            verify(taskEventProducer, never()).produceTaskCreated(any());
            verify(taskEventProducer, never()).produceTaskDeleted(any());
        }

        @Test
        void shouldAllowUpdateWhenTitleRemainsTheSame() {
            // given
            Task existingTask = context.createTask();
            Task updatedTask = context.createTask();

            when(repository.findById(context.getId())).thenReturn(Optional.of(existingTask));
            when(repository.save(any(Task.class))).thenReturn(updatedTask);

            // when
            Task result = taskController.updateTask(context.getId(), context.createTaskRequest());

            // then
            verify(repository, never()).findByTitle(any());
            verify(repository).save(any(Task.class));
            assertThat(result, is(updatedTask));
        }
    }

    @Nested
    class DeleteTaskTest {
        @Test
        void shouldDeleteTaskWhenTaskExists() {
            // given
            TaskTestContext deleteContext = TaskTestContext.builder()
                    .title("Task to Delete")
                    .description("Description")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.LOW)
                    .build();

            Task existingTask = deleteContext.createTask();

            when(repository.findById(deleteContext.getId())).thenReturn(Optional.of(existingTask));

            // when
            taskController.deleteTask(deleteContext.getId());

            // then
            verify(repository).findById(deleteContext.getId());
            verify(repository).delete(existingTask);
            verify(taskEventProducer, times(1)).produceTaskDeleted(existingTask);
            verify(taskEventProducer, never()).produceTaskCreated(any());
            verify(taskEventProducer, never()).produceTaskUpdated(any());
        }

        @Test
        void shouldThrowTaskNotFoundExceptionWhenDeletingNonExistentTask() {
            // given
            when(repository.findById(context.getId())).thenReturn(Optional.empty());

            // when & then
            TaskNotFoundException exception = assertThrows(
                    TaskNotFoundException.class,
                    () -> taskController.deleteTask(context.getId())
            );

            assertEquals("Task not found with id: " + context.getId(), exception.getMessage());
            verify(repository).findById(context.getId());
            verify(repository, never()).delete(any(Task.class));
            verify(taskEventProducer, never()).produceTaskDeleted(any(Task.class));
        }
    }

    @Nested
    class GetTaskTest {
        @Test
        void shouldGetTaskWhenTaskExists() {
            // given
            TaskTestContext getContext = TaskTestContext.builder()
                    .title("Test Task")
                    .description("Description")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.LOW)
                    .build();

            Task existingTask = getContext.createTask();

            when(repository.findById(getContext.getId())).thenReturn(Optional.of(existingTask));

            // when
            Task result = taskController.getTask(getContext.getId());

            // then
            verify(repository).findById(getContext.getId());
            assertEquals(existingTask, result);
            verify(taskEventProducer, never()).produceTaskCreated(any());
            verify(taskEventProducer, never()).produceTaskUpdated(any());
            verify(taskEventProducer, never()).produceTaskDeleted(any());
        }

        @Test
        void shouldThrowTaskNotFoundExceptionWhenGettingNonExistentTask() {
            // given
            when(repository.findById(context.getId())).thenReturn(Optional.empty());

            // when & then
            TaskNotFoundException exception = assertThrows(
                    TaskNotFoundException.class,
                    () -> taskController.getTask(context.getId())
            );

            assertEquals("Task not found with id: " + context.getId(), exception.getMessage());
            verify(repository).findById(context.getId());
        }

        @Test
        void shouldGetAllTasks() {
            // given
            TaskTestContext context1 = TaskTestContext.builder()
                    .title("Task 1")
                    .description("Description 1")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.LOW)
                    .build();

            TaskTestContext context2 = TaskTestContext.builder()
                    .title("Task 2")
                    .description("Description 2")
                    .status(TaskStatus.IN_PROGRESS)
                    .priority(TaskPriority.HIGH)
                    .build();

            Task task1 = context1.createTask();
            Task task2 = context2.createTask();

            java.util.List<Task> expectedTasks = java.util.List.of(task1, task2);
            when(repository.findAll()).thenReturn(expectedTasks);

            // when
            java.util.List<Task> results = taskController.getTasks();

            // then
            verify(repository).findAll();
            assertEquals(expectedTasks, results);
            verify(taskEventProducer, never()).produceTaskCreated(any());
            verify(taskEventProducer, never()).produceTaskUpdated(any());
            verify(taskEventProducer, never()).produceTaskDeleted(any());
        }

        @Test
        void shouldReturnEmptyListWhenNoTasksExist() {
            // given
            when(repository.findAll()).thenReturn(java.util.Collections.emptyList());

            // when
            java.util.List<Task> results = taskController.getTasks();

            // then
            verify(repository).findAll();
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    class ValidateTaskTest {
        @Test
        void shouldReturnTrueWhenTaskIsValid() {
            // given
            TaskTestContext validContext = TaskTestContext.builder()
                    .title("Valid Task")
                    .description("Description")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.HIGH)
                    .build();

            Task task = validContext.createTask();

            when(repository.findById(validContext.getId())).thenReturn(Optional.of(task));
            when(taskExternalClient.validateTask(any(TaskRequest.class)))
                    .thenReturn(reactor.core.publisher.Mono.just(true));

            ArgumentCaptor<TaskRequest> requestCaptor = ArgumentCaptor.forClass(TaskRequest.class);

            // when
            Boolean result = taskController.isValid(validContext.getId());

            // then
            assertTrue(result);
            verify(repository).findById(validContext.getId());
            verify(taskExternalClient).validateTask(requestCaptor.capture());

            TaskRequest capturedRequest = requestCaptor.getValue();
            TaskRequest expectedRequest = validContext.createTaskRequest();

            assertEquals(expectedRequest, capturedRequest);
        }

        @Test
        void shouldReturnFalseWhenTaskDoesNotExist() {
            // given
            when(repository.findById(context.getId())).thenReturn(Optional.empty());

            // when
            Boolean result = taskController.isValid(context.getId());

            // then
            assertFalse(result);
            verify(repository).findById(context.getId());
            verify(taskExternalClient, never()).validateTask(any(TaskRequest.class));
        }

        @Test
        void shouldReturnFalseWhenExternalValidationFails() {
            // given
            TaskTestContext invalidContext = TaskTestContext.builder()
                    .title("Invalid Task")
                    .description("Description")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.HIGH)
                    .build();

            Task task = invalidContext.createTask();

            when(repository.findById(invalidContext.getId())).thenReturn(Optional.of(task));
            when(taskExternalClient.validateTask(any(TaskRequest.class)))
                    .thenReturn(reactor.core.publisher.Mono.just(false));

            // when
            Boolean result = taskController.isValid(invalidContext.getId());

            // then
            assertFalse(result);
            verify(repository).findById(invalidContext.getId());
            verify(taskExternalClient).validateTask(any(TaskRequest.class));
        }
    }
}

