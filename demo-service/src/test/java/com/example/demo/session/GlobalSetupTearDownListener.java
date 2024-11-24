package com.example.demo.session;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.launcher.LauncherSession;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

public class GlobalSetupTearDownListener implements LauncherSessionListener {
    @Override
    public void launcherSessionOpened(LauncherSession session) {
        session.getLauncher().registerTestExecutionListeners(new TestExecutionListener() {
            @Override
            public void testPlanExecutionStarted(TestPlan testPlan) {
                Awaitility.setDefaultTimeout(Duration.ofMinutes(1));

                GenericContainer<?> kafka = new GenericContainer<>("bitnami/kafka:3.7.0")
                        .withEnv(Map.of(
                                "KAFKA_CFG_NODE_ID", "0",
                                "KAFKA_CFG_PROCESS_ROLES", "controller,broker",
                                "KAFKA_CFG_LISTENERS", "PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094",
                                "KAFKA_CFG_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092,EXTERNAL://localhost:9094",
                                "KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP", "CONTROLLER:PLAINTEXT,EXTERNAL:PLAINTEXT,PLAINTEXT:PLAINTEXT",
                                "KAFKA_CFG_CONTROLLER_QUORUM_VOTERS", "0@localhost:9093",
                                "KAFKA_CFG_CONTROLLER_LISTENER_NAMES", "CONTROLLER"
                        ))
                        .withExposedPorts(9094)
                        .withCreateContainerCmdModifier(
                                e -> Objects.requireNonNull(e.getHostConfig())
                                        .withPortBindings(new PortBinding(Ports.Binding.bindPort(9094), new ExposedPort(9094)))
                        );

                kafka.start();

                MongoDBContainer mongo = new MongoDBContainer("mongo:8.0.3")
                        .withCommand("--replSet rs0 --bind_ip_all")
                        .withCreateContainerCmdModifier(
                                e -> Objects.requireNonNull(e.getHostConfig())
                                        .withPortBindings(
                                                new PortBinding(Ports.Binding.bindPort(27018),
                                                        new ExposedPort(27017))
                                        )
                        );

                mongo.start();
            }
        });
    }

    @Override
    public void launcherSessionClosed(LauncherSession session) {
    }
}