package de.idealo.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "spring.kafka-encryption")
@Getter
@Setter
public class KafkaEncryptionProperties {

    private Map<String, String> topics = new HashMap<>();
    private boolean enabled = true;
    private boolean allowMissingTopicConfiguration;
}
