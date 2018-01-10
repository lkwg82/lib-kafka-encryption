import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import de.idealo.kafka.serializer.DecryptDeserializer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestingDecryptDeserializer implements Deserializer<String> {
    private final DecryptDeserializer delegate;

    @Getter
    private byte[] plain;

    @Override
    public void configure(Map<String, ?> map, boolean b) {
    }

    @Override
    public String deserialize(String s, byte[] bytes) {
        plain = bytes;
        return delegate.deserialize(s, bytes);
    }

    @Override
    public void close() {

    }
}
