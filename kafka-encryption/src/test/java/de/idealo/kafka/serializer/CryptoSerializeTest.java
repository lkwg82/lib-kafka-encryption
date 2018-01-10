package de.idealo.kafka.serializer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class CryptoSerializeTest {
    private final Map<String, String> topics = new HashMap<>();
    private final EncryptSerializer encryptSerializer = new EncryptSerializer(topics, false);
    private final DecryptDeserializer decryptDeserializer = new DecryptDeserializer(topics);

    @Before
    public void setUp() {
        topics.put("topic1", "pass");
        topics.put("topic2", "");
    }

    @Test
    public void shouldTest() {
        String plainText = System.currentTimeMillis() + "";

        byte[] cyptherText = encryptSerializer.serialize("topic1", plainText);
        String actualPlainText = decryptDeserializer.deserialize("topic1", cyptherText);

        assertThat(actualPlainText).isEqualTo(plainText);
    }
}
