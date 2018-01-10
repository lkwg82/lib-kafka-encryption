package de.idealo.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import lombok.val;

public class CrypterTest {

    private final String secret = "key";
    private final Crypter crypter = new Crypter(secret);

    @Test
    public void shouldEncrypt() {
        val encrypted = crypter.encrypt("test");

        assertThat(encrypted).isNotEqualTo("test");
        assertThat(encrypted).contains("$");
    }

    @Test
    public void shouldDecrypt() {
        val encrypted = "FAkE34xJm54yk6W35Tf4g7fy/EzcRoL1LIa9S9Sej+CP3jiC3uKe3z2v+UrCiPWYj7OxfRb4/4K+e0XXxct3Z5RzNoRrjovnWx3p2Kqq9UMzqsIASqck1PJmQCHdbODd$WRL6IUkxFuagc50K66srvf1u9gUO/S/KcMxvHxRJCPi4NCg4";
        val plain = crypter.decrypt(encrypted);

        assertThat(plain).isEqualTo("test");
    }

    @Test
    public void shouldEncryptAndDecryptWithSameCrypter() {
        val crypterEncrypt = new Crypter(secret);

        testCryptoRoundTrip(crypterEncrypt, crypterEncrypt, "test");
    }

    @Test
    public void shouldEncryptAndDecryptWithDifferentCrypter() {
        val crypterEncrypt = new Crypter(secret);
        val crypterDecrypt = new Crypter(secret);

        testCryptoRoundTrip(crypterEncrypt, crypterDecrypt, "test");
    }

    private void testCryptoRoundTrip(Crypter crypterEncrypt, Crypter crypterDecrypt, String plain) {
        val encrypted = crypterEncrypt.encrypt(plain);
        val decrypted = crypterDecrypt.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plain);
    }

    /*
      some error handling tests
     */

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenInvalidCyphertext_isNull() {
        crypter.decrypt(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenInvalidCyphertext_IsShorterThan173Chars() {
        val encrypted = "93q/r5Zd+E5m/gkGpoOtnTtktIWbnBSPwr5yGE4ieUpzFapJw9nGohG1bxl8wtVQnHYD6mqzhU4N1+7k9+5lscGVbaGcqZOr6bfKkbx81R8Web2LdhWg8vbNTd0CMG2C$rlPf0FjJFDApEkbT76/NxKIS2lP3zGndJtUk9bWH2eV";
        crypter.decrypt(encrypted);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenInvalidPlaintext_isNull() {
        crypter.encrypt(null);
    }

    @Test
    public void cyphertextHasSeparatorAtGivenPosition() {
        val cyphertext = crypter.encrypt("");

        assertThat(cyphertext.charAt(128)).isEqualTo('$');
    }

    @Test
    public void saltShouldBeRandomized() {
        val saltBytes = new byte[Crypter.SALT_BYTES_SIZE];
        val saltAsBase64 = java.util.Base64.getEncoder()
                                           .encodeToString(saltBytes);

        val parts = crypter.encrypt("").split("\\$");
        val splittedSaltB64 = parts[0];

        assertThat(saltAsBase64).isNotEqualTo(splittedSaltB64);
    }
}
