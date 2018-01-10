import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.test.context.junit4.SpringRunner;

import de.idealo.kafka.config.KafkaEncryptionAutoConfiguration;
import de.idealo.kafka.config.KafkaEncryptionProperties;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "logging.level.org.springframework=INFO",
        "spring.main.banner-mode=off",
        "spring.kafka-encryption.allow-missing-topic-configuration=true"
})
public class ApplicationIT {
    @Autowired(required = false)
    private KafkaAutoConfiguration kafkaAutoConfiguration;
    @Autowired(required = false)
    private KafkaEncryptionAutoConfiguration kafkaEncAutoConfiguration;

    @Autowired(required = false)
    private KafkaEncryptionProperties properties;

    @Test
    public void shouldHaveLoaded_KafkaAutoConfig() {
        // this is from spring boot
        assertThat(kafkaAutoConfiguration).isNotNull();
    }

    @Test
    public void shouldHaveLoaded_KafkaEncryptionAutoConfig() {
        // this is our additional auto config
        assertThat(kafkaEncAutoConfiguration).isNotNull();
    }

    @Test
    public void shouldHaveRelaxedSecurity() {
        assertThat(properties.isAllowMissingTopicConfiguration()).isTrue();
    }

    @Configuration
    @EnableKafka
    @EnableAutoConfiguration
    static class TestConfig {
    }
}
