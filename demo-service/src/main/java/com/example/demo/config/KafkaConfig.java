package com.example.demo.config;

import com.example.demo.DemoEventProducer;
import com.example.demo.data.DemoEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final Map<String, Object> configProps;

    public KafkaConfig() {
        this.configProps = new HashMap<>();
    }

    @Bean
    @DependsOn("demoEventKafkaTemplate")
    public DemoEventProducer demoEventProducer(KafkaTemplate<String, DemoEvent> demoEventKafkaTemplate) {
        return new DemoEventProducer(demoEventKafkaTemplate);
    }

    @Bean
    public KafkaTemplate<String, DemoEvent> demoEventKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(getProducerFactoryConfig()));
    }

    private Map<String, Object> getProducerFactoryConfig() {
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return configProps;
    }
}
