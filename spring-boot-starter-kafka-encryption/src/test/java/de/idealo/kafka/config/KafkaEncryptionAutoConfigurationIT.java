package de.idealo.kafka.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerializer;

import de.idealo.kafka.serializer.DecryptDeserializer;
import de.idealo.kafka.serializer.EncryptSerializer;

import lombok.val;

public class KafkaEncryptionAutoConfigurationIT {
    @Rule
    public final EnvironmentVariables environment = new EnvironmentVariables();

    /*
      (de)serializer-tests
     */
    @Test
    public void shouldHaveExactlyOneSerializerOfTypeEncryptSerializer() {
        val context = new AnnotationConfigApplicationContext(DefaultConfig.class);

        Map<String, Serializer> serializerBeans = context.getBeansOfType(Serializer.class);

        assertThat(serializerBeans).hasSize(1);
        Serializer theOnlyOne = new ArrayList<>(serializerBeans.values()).get(0);
        assertThat(theOnlyOne).isInstanceOf(EncryptSerializer.class);
    }

    @Test
    public void shouldHaveExactlyOneDeserializerOfTypeDecryptSerializer() {
        val context = new AnnotationConfigApplicationContext(DefaultConfig.class);

        Map<String, Deserializer> serializerBeans = context.getBeansOfType(Deserializer.class);

        assertThat(serializerBeans).hasSize(1);
        Deserializer theOnlyOne = new ArrayList<>(serializerBeans.values()).get(0);
        assertThat(theOnlyOne).isInstanceOf(DecryptDeserializer.class);
    }

    /*
      missing config - tests
     */
    @Test
    public void shouldFailToStartWhenConfigIsMissing() {
        try {
            SpringApplication.run(DefaultConfig.class);
            fail("should fail");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).endsWith("KAFKA-ENCRYPTION no password(s) for encryption configured");
        }
    }

    @Test
    public void shouldNotFailToStartWhenMissingConfigIsOk() {
        environment.set("spring.kafka-encryption.allow-missing-topic-configuration", "true");

        val context = new AnnotationConfigApplicationContext(DefaultConfig.class);

        context.start();
    }

    @Test
    public void shouldNotFailToStartWhenConfigIsPresent() {
        environment.set("spring.kafka-encryption.topicPasswords.topic1", "obscure-password");

        val context = new AnnotationConfigApplicationContext(DefaultConfig.class);

        context.start();
    }

    /*
      activation - tests
     */
    @Test
    public void shouldBeActivatedByDefault() {
        val context = new AnnotationConfigApplicationContext(DefaultConfig.class);

        val configuration = context.getBean(KafkaEncryptionAutoConfiguration.class);

        assertThat(configuration).isNotNull();
    }

    @Test
    public void shouldBeActivatedExplicitly() {
        environment.set("spring.kafka-encryption.enabled", "true");
        val context = new AnnotationConfigApplicationContext(DefaultConfig.class);

        val configuration = context.getBean(KafkaEncryptionAutoConfiguration.class);

        assertThat(configuration).isNotNull();
    }

    @Test
    public void shouldBeDeactivated() {
        environment.set("spring.kafka-encryption.enabled", "false");
        val context = new AnnotationConfigApplicationContext(DefaultConfig.class);

        val beans = context.getBeansOfType(KafkaEncryptionAutoConfiguration.class);

        assertThat(beans).isEmpty();
    }

    /*
      security check - tests
     */

    @Test
    public void shouldFailIfItDetectsOverridenSerializer() {
        /*
           this should avoid confusion when
           - having a custom Serializer configured
           - added the kafka-encryption lib
           - have multiple Serialzer and the EncryptionSerializer is used
         */

        try {
            SpringApplication.run(ConfigWithAddtionalSerializerBeans.class);
            fail("should fail");
        } catch (IllegalStateException e) {
            String canonicalName = EncryptSerializer.class.getCanonicalName();
            String m = "KAFKA-ENCRYPTION detected multiple value-serializer, only accepts exactly one derived from " + canonicalName;
            assertThat(e.getMessage()).endsWith(m);
        }
    }

    @Configuration
    @EnableAutoConfiguration
    static class DefaultConfig {
    }

    @Configuration
    @EnableAutoConfiguration
    static class ConfigWithAddtionalSerializerBeans {

        @Bean
        Serializer anotherSerializer() {
            return mock(Serializer.class);
        }

        @Bean
        JsonSerializer jsonSerializer() {
            return new JsonSerializer();
        }

        @Bean
        LongSerializer longSerializer() {
            return new LongSerializer();
        }
    }


}
