package com.example.demo.config;

import com.example.demo.data.TaskEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(MockitoExtension.class)
class KafkaConfigTest {

    private KafkaConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
    }

    @Test
    void shouldCreateKafkaTemplateWhenTaskEventKafkaTemplateCalled() {
        KafkaTemplate<String, TaskEvent> result = kafkaConfig.taskEventKafkaTemplate();

        assertThat(result, is(notNullValue()));
        assertThat(result, is(instanceOf(KafkaTemplate.class)));
    }

    @Test
    void shouldConfigureProducerFactoryWithBootstrapServersWhenKafkaTemplateCreated() {
        kafkaConfig.taskEventKafkaTemplate();

        Map<String, Object> configProps = (Map<String, Object>) ReflectionTestUtils.getField(kafkaConfig, "configProps");
        
        assertThat(configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG), is("localhost:9092"));
    }

    @Test
    void shouldConfigureJsonSerializerWithoutTypeHeadersWhenKafkaTemplateCreated() {
        kafkaConfig.taskEventKafkaTemplate();

        Map<String, Object> configProps = (Map<String, Object>) ReflectionTestUtils.getField(kafkaConfig, "configProps");
        
        assertThat(configProps.get(JsonSerializer.ADD_TYPE_INFO_HEADERS), is(false));
    }

    @Test
    void shouldConfigureStringKeySerializerWhenKafkaTemplateCreated() {
        kafkaConfig.taskEventKafkaTemplate();

        Map<String, Object> configProps = (Map<String, Object>) ReflectionTestUtils.getField(kafkaConfig, "configProps");
        
        assertThat(configProps.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG), is(StringSerializer.class));
    }

    @Test
    void shouldConfigureJsonValueSerializerWhenKafkaTemplateCreated() {
        kafkaConfig.taskEventKafkaTemplate();

        Map<String, Object> configProps = (Map<String, Object>) ReflectionTestUtils.getField(kafkaConfig, "configProps");
        
        assertThat(configProps.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG), is(JsonSerializer.class));
    }
}

