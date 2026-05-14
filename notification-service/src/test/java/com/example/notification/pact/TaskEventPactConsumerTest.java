package com.example.notification.pact;

import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.V4Interaction;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.example.notification.TaskEventListener;
import com.example.notification.TaskNotificationHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "demo-service-tasks-events", providerType = ProviderType.ASYNCH)
public class TaskEventPactConsumerTest {

    private final TaskEventListener listener = new TaskEventListener(new TaskNotificationHandler(), new ObjectMapper());

    @Pact(consumer = "notification-service")
    public V4Pact taskEvent(PactBuilder builder) {
        return builder
                .expectsToReceiveMessageInteraction("a task event", message ->
                        message.withContents(contents -> contents.withContent(
                                new PactDslJsonBody()
                                        .stringType("taskId", "507f1f77bcf86cd799439011")
                                        .stringMatcher("status", "TODO|IN_PROGRESS|DONE", "TODO"))))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "taskEvent")
    void shouldHandleTaskEvent(List<V4Interaction.AsynchronousMessage> messages) throws Exception {
        listener.onTaskEvent(messages.get(0).contentsAsString());
    }
}
