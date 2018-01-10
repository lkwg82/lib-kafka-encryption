package de.idealo.kafka.serializer;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import de.idealo.crypto.Crypter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DecryptDeserializer implements Deserializer<String> {
    private final String ENC_V1 = "ENC!v1!";

    private final Map<String, String> topicPassMap;

    @Override
    public void configure(Map<String, ?> map, boolean b) {
        // intentionally left
    }

    @Override
    public String deserialize(String topic, byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        String data = new String(bytes, UTF_8);
        if (data.startsWith(ENC_V1)) {
            return handleEncryptedData(topic, data);
        }
        return data;
    }

    private String handleEncryptedData(String topic, String wrappedCiphertext) {
        String cipherText = wrappedCiphertext.replaceFirst(ENC_V1, "");
        if (topicPassMap.containsKey(topic)) {
            String password = topicPassMap.get(topic);
            return new Crypter(password).decrypt(cipherText);
        } else {
            throw new SerializationException("KAFKA-ENCRYPTION: missing configuration for topic " + topic + " - property "
                    + "topics." + topic
                    + " should be set with <password> as value");

        }
    }

    @Override
    public void close() {
        // intentionally left empty
    }
}
