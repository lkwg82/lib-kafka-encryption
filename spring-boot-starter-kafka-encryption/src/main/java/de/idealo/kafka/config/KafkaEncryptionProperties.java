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
    /**
     * map topic-name to password
     */
    private Map<String, String> topicPasswords = new HashMap<>();

    /**
     * flag to enable encryption
     */
    private boolean enabled = true;

    /**
     * flag to enable whitelisting of topicPasswords
     */
    private boolean allowMissingTopicConfiguration;
}
