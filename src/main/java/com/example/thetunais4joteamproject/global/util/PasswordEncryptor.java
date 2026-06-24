package com.example.thetunais4joteamproject.global.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncryptor {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATION_COUNT = 120000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String DELIMITER = ":";

    private final SecureRandom secureRandom = new SecureRandom();

    public String encrypt(String rawPassword) {
        byte[] salt = createSalt();
        byte[] hash = hash(rawPassword, salt);

        return ITERATION_COUNT
                + DELIMITER
                + Base64.getEncoder().encodeToString(salt)
                + DELIMITER
                + Base64.getEncoder().encodeToString(hash);
    }

    private byte[] createSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);

        return salt;
    }

    private byte[] hash(String rawPassword, byte[] salt) {
        try {
            PBEKeySpec keySpec = new PBEKeySpec(
                    rawPassword.toCharArray(),
                    salt,
                    ITERATION_COUNT,
                    KEY_LENGTH
            );
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM);

            return secretKeyFactory.generateSecret(keySpec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalStateException("Password encryption failed", exception);
        }
    }
}