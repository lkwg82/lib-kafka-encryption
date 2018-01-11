package de.idealo.kafka.serializer;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.junit.Before;
import org.junit.Test;

public class EncryptSerializerTest {
    private Map<String, String> topics = new HashMap<>();
    private final EncryptSerializer encryptSerializer = new EncryptSerializer(topics, false);

    @Before
    public void setUp() {
        topics.put("topic1", "pass");
        topics.put("topic2", "");
    }

    @Test
    public void shouldNotEncryptIfPasswordIsEmpty() {
        String plainText = "test";

        byte[] cyptherText = encryptSerializer.serialize("topic2", plainText);

        assertThat(new String(cyptherText)).isEqualTo(plainText);
    }

    @Test
    public void shouldEncryptIfPasswordIsPresent() {
        String plainText = "test";

        byte[] cyptherText = encryptSerializer.serialize("topic1", plainText);

        String actual = new String(cyptherText);
        assertThat(actual).isNotEqualTo(plainText);
        assertThat(actual).contains("$");
        assertThat(actual).startsWith("ENC!v1!");
    }

    @Test
    public void shouldRaiseAnExceptionIfATopicIsNotConfiguredByDefault() {
        try {
            encryptSerializer.serialize("topic3", "");
            fail("must fail");
        } catch (SerializationException e) {
            String message = e.getMessage();
            assertThat(message)
                    .contains("property " + "topicPasswords.topic3 should be set");
        }
    }

    @Test
    public void shouldAllowATopicNotConfigured() {
        EncryptSerializer encryptSerializer = new EncryptSerializer(topics, true);

        String plainText = "test";

        byte[] cyptherText = encryptSerializer.serialize("topic3", plainText);

        assertThat(new String(cyptherText)).isEqualTo(plainText);
    }
}