package de.idealo.kafka.serializer;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import de.idealo.crypto.Crypter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EncryptSerializer implements Serializer<String> {
    private final Map<String, String> topicPassMap;
    private final boolean allowMissingTopicConfig;

    @Override
    public void configure(Map<String, ?> map, boolean b) {
        // intentionally left empty
    }

    @Override
    public byte[] serialize(String topic, String data) {
        if (topicPassMap.containsKey(topic)) {
            String pass = topicPassMap.get(topic);
            String encryptAndWrap = encryptAndWrap(data, pass);
            return encryptAndWrap.getBytes(UTF_8);
        } else {
            return handleMissingConfiguration(topic, data);
        }
    }

    private byte[] handleMissingConfiguration(String topic, String data) {
        if (allowMissingTopicConfig) {
            return data.getBytes(UTF_8);
        }
        throw new SerializationException("KAFKA-ENCRYPTION: missing configuration for " + topic + " - property "
                + "topicPasswords." + topic + " should be set with <password> as value");
    }

    private String encryptAndWrap(String data, String pass) {
        if ("".equals(pass)) {
            return data;
        }
        String encrypt = new Crypter(pass).encrypt(data);
        return wrapV1(encrypt);
    }

    private String wrapV1(String encrypted) {
        return "ENC!v1!" + encrypted;
    }

    @Override
    public void close() {
        // intentionally left empty
    }
}
