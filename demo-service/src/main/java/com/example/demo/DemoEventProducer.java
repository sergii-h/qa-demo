package com.example.demo;

import com.example.demo.data.DemoEvent;
import com.example.demo.data.DemoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@RequiredArgsConstructor
public class DemoEventProducer {

    private final KafkaTemplate<String, DemoEvent> kafkaTemplate;

    @Value("${kafka.topic}")
    private String topic;

    public void produce(DemoResponse demoResponse) {
        DemoEvent eventData = DemoEvent.builder()
                .id(demoResponse.getId())
                .name(demoResponse.getName())
                .description(demoResponse.getDescription())
                .amount(demoResponse.getAmount())
                .build();

        log.info("Sending demo response to {}: {}", topic, eventData);

        kafkaTemplate.send(topic, eventData);
    }
}
