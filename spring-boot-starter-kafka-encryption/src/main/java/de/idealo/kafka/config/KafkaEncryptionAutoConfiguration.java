package de.idealo.kafka.config;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import de.idealo.kafka.serializer.DecryptDeserializer;
import de.idealo.kafka.serializer.EncryptSerializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties(KafkaEncryptionProperties.class)
@Conditional(KafkaEncryptionAutoConfiguration.Condition.class)
@RequiredArgsConstructor
public class KafkaEncryptionAutoConfiguration implements ApplicationListener<ContextStartedEvent> {
    private final KafkaEncryptionProperties properties;

    @Bean
    @ConditionalOnMissingBean(DecryptDeserializer.class)
    DecryptDeserializer decryptDeserializer(ConsumerFactory<String, String> consumerFactory, KafkaEncryptionProperties properties) {
        DecryptDeserializer decryptDeserializer = new DecryptDeserializer(properties.getTopics());
        setValueDeserializer(consumerFactory, decryptDeserializer);

        return decryptDeserializer;
    }

    @Bean
    @ConditionalOnMissingBean(EncryptSerializer.class)
    EncryptSerializer encryptSerializer(ProducerFactory<String, String> producerFactory, KafkaEncryptionProperties properties) {
        Map<String, String> topics = properties.getTopics();
        boolean allowMissingTopicConfiguration = properties.isAllowMissingTopicConfiguration();
        EncryptSerializer serializer = new EncryptSerializer(topics, allowMissingTopicConfiguration);

        setValueSerializer(producerFactory, serializer);

        return serializer;
    }

    private void setValueSerializer(ProducerFactory<String, String> producerFactory, EncryptSerializer serializer) {
        if (producerFactory instanceof DefaultKafkaProducerFactory) {
            ((DefaultKafkaProducerFactory<String, String>) producerFactory).setValueSerializer(serializer);
        } else {
            throw new IllegalArgumentException("could not handle this situation, I dont know how to set the value serializer");
        }
    }

    private void setValueDeserializer(ConsumerFactory<String, String> consumerFactory, DecryptDeserializer deserializer) {
        if (consumerFactory instanceof DefaultKafkaConsumerFactory) {
            ((DefaultKafkaConsumerFactory<String, String>) consumerFactory).setValueDeserializer(deserializer);
        } else {
            throw new IllegalArgumentException("could not handle this situation, I dont know how to set the value deserializer");
        }
    }

    @Override
    public void onApplicationEvent(ContextStartedEvent event) {
        if (properties.isAllowMissingTopicConfiguration()) {
            // ok
        } else {
            if (properties.getTopics().isEmpty()) {
                String message = "KAFKA-ENCRYPTION no password(s) for encryption configured";

                log.error("* -------------------------------------------------------------- *");
                log.error("");
                log.error(message);
                log.error("");
                log.error("* -------------------------------------------------------------- *");

                throw new IllegalStateException(message);
            }
        }
    }

    /**
     * because in spring-boot 1.X @ConditionalOnXY() are combined by OR
     */
    static class Condition extends AllNestedConditions {
        private Condition(ConfigurationPhase configurationPhase) {
            super(configurationPhase);
        }

        public Condition() {
            this(ConfigurationPhase.PARSE_CONFIGURATION);
        }


        @ConditionalOnClass(KafkaTemplate.class)
        static class A {
        }

        @ConditionalOnProperty(name = "spring.kafka-encryption.enabled", matchIfMissing = true)
        static class B {
        }
    }
}
