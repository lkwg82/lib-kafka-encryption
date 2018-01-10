package de.idealo.kafka.serializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class DecryptDeserializerTest {
    private final Map<String, String> topics = new HashMap<>();
    private final DecryptDeserializer decryptDeserializer = new DecryptDeserializer(topics);

    @Before
    public void setUp() {
        topics.put("topic1", "pass");
        topics.put("topic2", "");
    }

    @Test
    public void shouldReturnNullOnNullData() {
        String deserialize = decryptDeserializer.deserialize("any", null);

        assertThat(deserialize).isNull();
    }

    @Test
    public void shouldReturnPlainTextIfNotEncrypted() {
        String deserialize = decryptDeserializer.deserialize("any", "hallo".getBytes());

        assertThat(deserialize).isEqualTo("hallo");
    }

    @Test
    public void shouldRaiseExceptionWhenConfigurationForTopicIsMissing() {
        try {
            decryptDeserializer.deserialize("any", "ENC!v1!hallo".getBytes());
            fail("should fail");
        } catch (SerializationException e) {
            Assertions.assertThat(e.getMessage())
                      .contains("property " + "topics.any should be set");
        }
    }
}