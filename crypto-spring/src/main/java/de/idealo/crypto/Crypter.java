package de.idealo.crypto;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class Crypter {
    static final int SALT_BYTES_SIZE = 96;

    private final String secret;

    /**
     * encrypts with a per message salt
     *
     * @param plain text
     * @return <code>base64(salt) + $ + base64(cyphertext)</code>
     */
    public String encrypt(String plain) {
        if (null == plain) {
            throw new IllegalArgumentException("plaintext must not be null");
        }

        val salt = createSalt();
        val encrypted = encryptWithBackend(plain, salt);

        /*
         * base64 encoding is shorter than hex encoding given by HexEncryptor
         */
        val saltB64 = B64.encode(salt);
        val encryptedB64 = B64.encode(encrypted);

        return saltB64 + "$" + encryptedB64;
    }

    private byte[] encryptWithBackend(String plain, byte[] salt) {
        val encryptor = createEncryptor(salt);
        return encryptor.encrypt(plain.getBytes(UTF_8));
    }

    /**
     * I chose {@link Encryptors#stronger}
     * because it uses AEAD {@see https://en.wikipedia.org/wiki/Authenticated_encryption}</li>
     * <p/>
     * so I get protection against on transit message manipulation
     */
    private BytesEncryptor createEncryptor(byte[] salt) {
        val saltAsHex = new String(Hex.encode(salt));
        return Encryptors.stronger(secret, saltAsHex);
    }

    /**
     * decrypts a message with a per message salt
     */
    public String decrypt(String cyphertext) {

        if (null == cyphertext) {
            throw new IllegalArgumentException("cyphertext must not be null");
        }

        // salt as base64 is 128 chars
        // $ = 1 char
        // minimal single letter is 44 chars
        // 128 + 1 + 44 = 173
        if (cyphertext.length() < 173) {
            throw new IllegalArgumentException("cypthertext is invalid: too short");
        }

        val split = cyphertext.split("\\$");
        val saltB64 = split[0];
        val encryptedB64 = split[1];

        val salt = B64.decode(saltB64);
        val encrypted = B64.decode(encryptedB64);

        return decryptWithBackend(salt, encrypted);
    }

    private String decryptWithBackend(byte[] salt, byte[] encrypted) {
        val encryptor = createEncryptor(salt);
        val decrypted = encryptor.decrypt(encrypted);
        return new String(decrypted, UTF_8);
    }

    private byte[] createSalt() {
        val saltBytes = new byte[SALT_BYTES_SIZE];

        val secRandom = new SecureRandom();
        secRandom.nextBytes(saltBytes);

        return saltBytes;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class B64 {
        static String encode(byte[] plain) {
            return Base64.getEncoder()
                         .encodeToString(plain);
        }

        static byte[] decode(String base64) {
            return Base64.getDecoder()
                         .decode(base64);
        }
    }
}
