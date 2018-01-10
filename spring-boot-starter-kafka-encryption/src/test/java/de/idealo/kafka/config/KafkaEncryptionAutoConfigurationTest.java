package de.idealo.kafka.config;

import org.apache.kafka.clients.producer.Producer;
import org.junit.Test;
import org.springframework.kafka.core.ProducerFactory;

import lombok.val;

public class KafkaEncryptionAutoConfigurationTest {
    private KafkaEncryptionAutoConfiguration kafkaEncryptionAutoConfiguration = new KafkaEncryptionAutoConfiguration(new KafkaEncryptionProperties());

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenNotHavingDefaultKafkaProducerFactory() {
        val properties = new KafkaEncryptionProperties();
        val producerFactory = new ProducerFactory<String, String>() {
            @Override
            public Producer<String, String> createProducer() {
                return null;
            }

            @Override
            public boolean transactionCapable() {
                return false;
            }
        };

        kafkaEncryptionAutoConfiguration.encryptSerializer(producerFactory, properties);
    }
}