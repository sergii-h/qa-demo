package com.example.demo.integration.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class KafkaConsumer<T> extends Thread {
    private String topic;
    private Class<T> messageClass;
    private Map<String, Object> config;

    @Builder.Default private String filter = "";
    @Builder.Default private int consumingTimeSec = 60;
    @Builder.Default private boolean waiting = true;
    @Builder.Default private Map<String, Object> defaultConfig = new HashMap<>(){{
        put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + RandomStringUtils.randomNumeric(10));
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    }};

    private final List<String> messages = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<T> records = new ArrayList<>();

    @Override
    public void run() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        defaultConfig.putAll(config);

        Consumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(defaultConfig);

        consumer.subscribe(Collections.singletonList(topic));

        while(waiting) {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));

            consumerRecords.forEach(consumerRecord -> {
                String message = consumerRecord.value();

                if (message.toLowerCase().contains(filter.toLowerCase())) {
                    try {
                        records.add(objectMapper.readValue(message, messageClass));
                    } catch (IOException e) {
                        throw new InternalError("message cannot be decoded: " + message, e);
                    }
                }
            });

            consumer.commitSync();
        }

        consumer.close();
    }

    @SneakyThrows
    public boolean isTopicPresent(String topicName) {
        try (AdminClient admin = AdminClient.create(config)) {
            return admin.listTopics().names().get().contains(topicName);
        }
    }

    public void shutdown() {
        this.waiting = false;
    }
}

