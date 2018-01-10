package de.idealo.kafka.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

public class KafkaEncryptionAutoConfigurationMissingConfigIT {

    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @Before
    public void setUp() throws Exception {
        context.register(TestConfig.class);
    }

    @Test
    public void shouldFailToStartWhenConfigIsMissing() {
        context.refresh();
        try {
            context.start();
            fail("should fail");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).endsWith("KAFKA-ENCRYPTION no password(s) for encryption configured");
        }
    }

    @Test
    public void shouldNotFailToStartWhenMissingConfigIsOk() {

        String key = "spring.kafka-encryption.allow-missing-topic-configuration";
        System.setProperty(key, true + "");

        context.refresh();

        try {
            context.start();
        } finally {
            System.clearProperty(key);
        }
    }

    @Configuration
    @EnableKafka
    @EnableAutoConfiguration
    public static class TestConfig {
    }
}
