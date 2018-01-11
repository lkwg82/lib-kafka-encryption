package de.idealo.kafka.config;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.common.serialization.Serializer;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
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
public class KafkaEncryptionAutoConfiguration {
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

    @EventListener
    public void checkOnMultipleValueSerializer(ApplicationReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        Map<String, Serializer> serializerMap = context.getBeansOfType(Serializer.class);
        if (serializerMap.size() > 1) {
            String canonicalName = EncryptSerializer.class.getCanonicalName();
            String message = "KAFKA-ENCRYPTION detected multiple value-serializer, only accepts exactly one derived from " + canonicalName;

            String serializersList = serializerMap.entrySet().stream()
                                                  .sorted(Comparator.comparing(Map.Entry::getKey))
                                                  .map(entry -> "\tserializer '" + entry
                                                          .getKey() + "' of type '" + entry
                                                          .getValue()
                                                          .getClass()
                                                          .getCanonicalName() + "'")
                                                  .collect(Collectors.joining("\n"));

            log.error("* -------------------------------------------------------------- *");
            log.error("");
            log.error(message);
            log.error("");
            log.error("found \n" + serializersList);
            log.error("");
            log.error("* -------------------------------------------------------------- *");

            throw new IllegalStateException(message);
        }
    }

    @EventListener
    public void checkOnMissingConfiguration(ApplicationReadyEvent event) {
        if (!properties.isAllowMissingTopicConfiguration()) {
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
