package org.lotto.ui.tests.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.bson.types.ObjectId;

import java.util.Map;

@Data
@Builder
public class KafkaProducer<T> {
    private String topic;

    @Builder.Default private Map<String, Object> config = Map.of();

    public KafkaProducer<T> produce(T message) {
        String messageAsString;

        try {
            messageAsString = message instanceof String ?
                    message.toString() :
                    new ObjectMapper().writeValueAsString(message);

        } catch (JsonProcessingException e) {
            throw new InternalError(e);
        }

        try (org.apache.kafka.clients.producer.KafkaProducer<Object, Object> kafkaProducer =
                     new org.apache.kafka.clients.producer.KafkaProducer<>(config)) {
            kafkaProducer.send(new ProducerRecord<>(topic, new ObjectId().toString(),  messageAsString));
        }

        return this;
    }
}

