package de.idealo.kafka.config;

import static org.mockito.Mockito.mock;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import de.idealo.kafka.serializer.DecryptDeserializer;
import de.idealo.kafka.serializer.EncryptSerializer;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.kafka-encryption.allow-missing-topic-configuration=true")
@TestPropertySource(locations = "classpath:test.properties")
public class KafkaEncryptionAutoConfigurationIT {
    @Autowired
    private List<EncryptSerializer> encryptSerializers;
    @Autowired
    private List<DecryptDeserializer> decryptDeserializers;

    @Test
    public void shouldHaveOnlyOneSerializer() {
        Assertions.assertThat(encryptSerializers).hasSize(1);
        Assertions.assertThat(decryptDeserializers).hasSize(1);
    }

    @Configuration
    @EnableKafka
    @EnableAutoConfiguration
    public static class TestConfig {
        @Bean
        EncryptSerializer encryptSerializer2() {
            return mock(EncryptSerializer.class);
        }

        @Bean
        DecryptDeserializer decryptDeserializer2() {
            return mock(DecryptDeserializer.class);
        }
    }
}
