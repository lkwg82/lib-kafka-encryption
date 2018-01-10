package de.idealo.kafka.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.SneakyThrows;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.kafka-encryption.topics.test-topic=password")
public class DemoAppIT {

    @ClassRule
    public static RuleChain chain = RuleChain
            .outerRule(new KafkaEmbedded(1, false, 1, "test-topic"))
            .around(new ExternalResource() {
                @Override
                protected void before() {
                    String property = System.getProperty(KafkaEmbedded.SPRING_EMBEDDED_KAFKA_BROKERS);
                    System.setProperty("spring.kafka.bootstrapServers", property);
                }
            });

    @Autowired
    DemoApp.Producer producer;
    @Autowired
    DemoApp.Consumer consumer;

    @Test(timeout = 20_000)
    @SneakyThrows
    public void should_show_a_successful_roundtrip() {

        String message = "hello + " + System.currentTimeMillis();

        producer.send(message);

        String receivedMessage = consumer.getReceivedMessages().take();
        assertThat(receivedMessage).isEqualTo(message);
    }
}
