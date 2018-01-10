package de.idealo.kafka.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.kafka-encryption.allow-missing-topic-configuration=true")
@TestPropertySource(locations = "classpath:test.properties")
public class KafkaEncryptionAutoConfigurationActivationIT {
    @Autowired(required = false)
    private KafkaEncryptionAutoConfiguration configuration;

    @Test
    public void shouldBeActivatedByDefault() {
        assertThat(configuration).isNotNull();
    }

    @Configuration
    @EnableKafka
    @EnableAutoConfiguration
    public static class TestConfig {
    }
}
