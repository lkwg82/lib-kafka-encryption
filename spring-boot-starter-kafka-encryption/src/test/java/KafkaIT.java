import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.messaging.Message;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import de.idealo.kafka.config.KafkaEncryptionAutoConfiguration;
import de.idealo.kafka.serializer.DecryptDeserializer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = KafkaIT.TestConfig.class,
        properties = {
                "spring.kafka.consumer.group-id=test",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "spring.kafka-encryption.topicPasswords.topic1=pass",
                "spring.kafka-encryption.topicPasswords.topic2=pass2",
                "spring.kafka-encryption.topicPasswords.dead_letter_queue=pass3",
        })
@TestPropertySource(locations = "classpath:test.properties")
public class KafkaIT {

    private static final String BREAKING_NEWS = "BREAKING_NEWS";

    @ClassRule
    public static RuleChain chain = RuleChain
            .outerRule(new KafkaEmbedded(1, false, 1, "topic1"))
            .around(new ExternalResource() {
                @Override
                protected void before() {
                    String property = System.getProperty(KafkaEmbedded.SPRING_EMBEDDED_KAFKA_BROKERS);
                    System.setProperty("spring.kafka.bootstrapServers", property);
                }
            });

    @Autowired
    private KafkaTemplate<Object, Object> template;

    @Autowired
    private MyKafkaListener listener;

    @Autowired
    private MyKafkaDeadLetterListener deadLetterListener;

    @Autowired
    private TestingDecryptDeserializer testingDecryptDeserializer;

    @Test(timeout = 2_000)
    public void testEncryptionRoundTrip() throws ExecutionException, InterruptedException {
        MILLISECONDS.sleep(2_00);

        String aMessage = "{ \"World_1" + new Random().nextLong() + "\":1}";
        template.send("topic1", aMessage)
                .get();

        MILLISECONDS.sleep(1_000);

        String plain = new String(testingDecryptDeserializer.getPlain());
        assertThat(plain).isNotEqualToIgnoringCase(aMessage);

        assertThat(listener.getMessages()).contains(aMessage);
    }

    @Test
    public void testDeadLetterQueue() throws ExecutionException, InterruptedException {
        MILLISECONDS.sleep(2_00);

        String aMessage = BREAKING_NEWS;
        template.send("topic2", aMessage)
                .get();

        MILLISECONDS.sleep(1_000);

        String plain = new String(testingDecryptDeserializer.getPlain());
        assertThat(plain).isNotEqualToIgnoringCase(aMessage);

        assertThat(deadLetterListener.getMessages()).contains(aMessage);
    }

    @Configuration
    @Import({ KafkaAutoConfiguration.class, KafkaEncryptionAutoConfiguration.class })
    public static class TestConfig {
        @Bean
        TestingDecryptDeserializer wrappingDecryptDeserializer(DefaultKafkaConsumerFactory<String, String> consumerFactory) {
            DecryptDeserializer decryptDeserializer = (DecryptDeserializer) consumerFactory.getValueDeserializer();
            TestingDecryptDeserializer testableDecryptDeserializer = new TestingDecryptDeserializer(decryptDeserializer);
            consumerFactory.setValueDeserializer(testableDecryptDeserializer);

            return testableDecryptDeserializer;
        }

        @Bean
        MyKafkaListener listener() {
            return new MyKafkaListener();
        }

        @Bean
        MyKafkaListenerRaisesError listenerRaiseError() {
            return new MyKafkaListenerRaisesError();
        }

        @Bean
        MyKafkaDeadLetterListener deadLetterListener() {
            return new MyKafkaDeadLetterListener();
        }

        @Bean(name = "KafkaListenerErrorHandler")
        KafkaListenerErrorHandler getErrorHandler(KafkaTemplate<String, String> template) {
            return new MyErrorHandler(template);
        }
    }

    @Getter
    public static class MyKafkaListener {
        private List<String> messages = new ArrayList<>();

        @KafkaListener(topics = "topic1")
        public void listen(String record) {
            messages.add(record);
        }
    }

    @Getter
    public static class MyKafkaListenerRaisesError {
        @KafkaListener(topics = "topic2", errorHandler = "KafkaListenerErrorHandler")
        public void listen(String record) {
            throw new IllegalArgumentException(record);
        }
    }

    @Getter
    public static class MyKafkaDeadLetterListener {
        private List<String> messages = new ArrayList<>();

        @KafkaListener(topics = "dead_letter_queue")
        public void listen(String record) {
            messages.add(record);
        }
    }

    @RequiredArgsConstructor
    public static class MyErrorHandler implements KafkaListenerErrorHandler {
        private final KafkaTemplate<String, String> template;

        @Override
        public Object handleError(Message<?> message, ListenerExecutionFailedException e) throws Exception {
            String payload = (String) message.getPayload();
            template.send("dead_letter_queue", payload);
            return null; // return value is ignored (from javadoc, wtf?)
        }
    }
}
