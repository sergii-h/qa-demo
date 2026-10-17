package com.example.demo;

import com.example.demo.context.TaskTestContext;
import com.example.demo.data.Task;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskRequest;
import com.example.demo.data.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {
    private static final Instant NOW = Instant.parse("2024-06-15T12:30:45.123Z");
    private static final Instant EARLIER = Instant.parse("2024-01-01T10:00:00Z");

    @Mock
    private TaskRepository repository;

    @Mock
    private TaskEventProducer taskEventProducer;

    @Mock
    private TaskExternalClient taskExternalClient;

    private TaskController taskController;

    TaskTestContext context;

    @BeforeEach
    void beforeEach() {
        taskController = new TaskController(repository, taskEventProducer, taskExternalClient, Clock.fixed(NOW, ZoneOffset.UTC));

        context = TaskTestContext.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .createdDate(NOW)
                .updatedDate(NOW)
                .build();
    }

    @Nested
    class CreateTaskTest {
        @Test
        void shouldPersistTaskAndPublishCreatedEventWhenTitleIsUnique() {
            // given
            when(repository.insert(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

            // when
            Task result = taskController.createTask(context.createTaskRequest());

            // then
            verify(repository).insert(taskCaptor.capture());
            context.setId(taskCaptor.getValue().getId());

            assertThat(taskCaptor.getValue(), is(context.createTask()));
            assertThat(result, is(context.createTask()));
            verify(taskEventProducer).produceTaskCreated(context.createTask());
        }

        @Test
        void shouldRejectCreationWhenTitleAlreadyExists() {
            // given
            when(repository.findByTitle(context.getTitle())).thenReturn(context.createTask());

            // when
            DuplicateTitleException exception = assertThrows(
                    DuplicateTitleException.class,
                    () -> taskController.createTask(context.createTaskRequest())
            );

            // then
            assertThat(exception.getMessage(), is("Task with title '" + context.getTitle() + "' already exists"));
            verify(repository, never()).insert(any(Task.class));
            verify(taskEventProducer, never()).produceTaskCreated(any());
        }

        @Test
        void shouldNotPublishEventWhenPersistenceFails() {
            // given
            doThrow(new RuntimeException("DB error")).when(repository).insert(any(Task.class));

            // when
            assertThrows(RuntimeException.class, () -> taskController.createTask(context.createTaskRequest()));

            // then
            verify(taskEventProducer, never()).produceTaskCreated(any());
        }
    }

    @Nested
    class UpdateTaskTest {
        TaskTestContext existingContext;

        @BeforeEach
        void beforeEach() {
            existingContext = TaskTestContext.builder()
                    .id(context.getId())
                    .title("Old Title")
                    .description("Old Description")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.LOW)
                    .createdDate(EARLIER)
                    .updatedDate(EARLIER)
                    .build();
        }

        @Test
        void shouldPersistChangesPreservingCreatedDateAndPublishUpdatedEventWhenTaskExists() {
            // given
            context.setCreatedDate(EARLIER);
            when(repository.findById(context.getId())).thenReturn(Optional.of(existingContext.createTask()));
            when(repository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

            // when
            Task result = taskController.updateTask(context.getId(), context.createTaskRequest());

            // then
            verify(repository).save(taskCaptor.capture());
            assertThat(taskCaptor.getValue(), is(context.createTask()));
            assertThat(result, is(context.createTask()));
            verify(taskEventProducer).produceTaskUpdated(context.createTask());
        }

        @Test
        void shouldRejectUpdateWhenTaskDoesNotExist() {
            // given
            when(repository.findById(context.getId())).thenReturn(Optional.empty());

            // when
            TaskNotFoundException exception = assertThrows(
                    TaskNotFoundException.class,
                    () -> taskController.updateTask(context.getId(), context.createTaskRequest())
            );

            // then
            assertThat(exception.getMessage(), is("Task not found with id: " + context.getId()));
            verify(repository, never()).save(any(Task.class));
            verify(taskEventProducer, never()).produceTaskUpdated(any(Task.class));
        }

        @Test
        void shouldRejectUpdateWhenNewTitleBelongsToAnotherTask() {
            // given
            TaskTestContext otherTaskContext = TaskTestContext.builder().title(context.getTitle()).build();
            when(repository.findById(context.getId())).thenReturn(Optional.of(existingContext.createTask()));
            when(repository.findByTitle(context.getTitle())).thenReturn(otherTaskContext.createTask());

            // when
            DuplicateTitleException exception = assertThrows(
                    DuplicateTitleException.class,
                    () -> taskController.updateTask(context.getId(), context.createTaskRequest())
            );

            // then
            assertThat(exception.getMessage(), is("Task with title '" + context.getTitle() + "' already exists"));
            verify(repository, never()).save(any(Task.class));
            verify(taskEventProducer, never()).produceTaskUpdated(any(Task.class));
        }

        @Test
        void shouldAllowUpdateWhenTitleRemainsTheSame() {
            // given
            context.setCreatedDate(EARLIER);
            existingContext.setTitle(context.getTitle());
            when(repository.findById(context.getId())).thenReturn(Optional.of(existingContext.createTask()));
            lenient().when(repository.findByTitle(context.getTitle())).thenReturn(existingContext.createTask());
            when(repository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Task result = taskController.updateTask(context.getId(), context.createTaskRequest());

            // then
            assertThat(result, is(context.createTask()));
        }
    }

    @Nested
    class DeleteTaskTest {
        @Test
        void shouldRemoveTaskAndPublishDeletedEventWhenTaskExists() {
            // given
            Task existingTask = context.createTask();
            when(repository.findById(context.getId())).thenReturn(Optional.of(existingTask));

            // when
            taskController.deleteTask(context.getId());

            // then
            verify(repository).delete(existingTask);
            verify(taskEventProducer).produceTaskDeleted(existingTask);
        }

        @Test
        void shouldRejectDeletionWhenTaskDoesNotExist() {
            // given
            when(repository.findById(context.getId())).thenReturn(Optional.empty());

            // when
            TaskNotFoundException exception = assertThrows(
                    TaskNotFoundException.class,
                    () -> taskController.deleteTask(context.getId())
            );

            // then
            assertThat(exception.getMessage(), is("Task not found with id: " + context.getId()));
            verify(repository, never()).delete(any(Task.class));
            verify(taskEventProducer, never()).produceTaskDeleted(any(Task.class));
        }
    }

    @Nested
    class GetTaskTest {
        @Test
        void shouldRejectLookupWhenTaskDoesNotExist() {
            // given
            when(repository.findById(context.getId())).thenReturn(Optional.empty());

            // when
            TaskNotFoundException exception = assertThrows(
                    TaskNotFoundException.class,
                    () -> taskController.getTask(context.getId())
            );

            // then
            assertThat(exception.getMessage(), is("Task not found with id: " + context.getId()));
        }
    }

    @Nested
    class ValidateTaskTest {
        @Test
        void shouldForwardTaskDataToExternalServiceAndReturnItsVerdictWhenTaskExists() {
            // given
            when(repository.findById(context.getId())).thenReturn(Optional.of(context.createTask()));
            when(taskExternalClient.validateTask(any(TaskRequest.class))).thenReturn(Mono.just(true));
            ArgumentCaptor<TaskRequest> requestCaptor = ArgumentCaptor.forClass(TaskRequest.class);

            // when
            Boolean result = taskController.isValid(context.getId());

            // then
            assertTrue(result);
            verify(taskExternalClient).validateTask(requestCaptor.capture());
            assertThat(requestCaptor.getValue(), is(context.createTaskRequest()));
        }

        @Test
        void shouldReturnFalseWithoutCallingExternalServiceWhenTaskDoesNotExist() {
            // given
            when(repository.findById(context.getId())).thenReturn(Optional.empty());

            // when
            Boolean result = taskController.isValid(context.getId());

            // then
            assertFalse(result);
            verify(taskExternalClient, never()).validateTask(any(TaskRequest.class));
        }

        @Test
        void shouldReturnFalseWhenExternalServiceRejectsTask() {
            // given
            when(repository.findById(context.getId())).thenReturn(Optional.of(context.createTask()));
            when(taskExternalClient.validateTask(any(TaskRequest.class))).thenReturn(Mono.just(false));

            // when
            Boolean result = taskController.isValid(context.getId());

            // then
            assertFalse(result);
        }
    }
}
