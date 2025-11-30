package com.evaluacion.usuarios.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "test-secret-key-for-unit-testing";
    private static final long TEST_EXPIRATION_MS = 3600000;
    private static final String TEST_USERNAME = "testuser@example.com";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, TEST_EXPIRATION_MS);
    }

    @Test
    void generateToken_ShouldReturnValidJwtToken() {
        String token = jwtUtil.generateToken(TEST_USERNAME);

        assertNotNull(token);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts (header.payload.signature)");
    }

    @Test
    void generateToken_ShouldIncludeUsernameInToken() {
        String token = jwtUtil.generateToken(TEST_USERNAME);

        String extractedUsername = jwtUtil.getUsername(token);
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    void validateToken_ShouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken(TEST_USERNAME);

        boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid, "Token should be valid immediately after generation");
    }

    @Test
    void validateToken_ShouldReturnFalseForInvalidSignature() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        boolean isValid = jwtUtil.validateToken(tamperedToken);

        assertFalse(isValid, "Token with invalid signature should be rejected");
    }

    @Test
    void validateToken_ShouldReturnFalseForMalformedToken() {
        String malformedToken = "not.a.valid.jwt.token";

        boolean isValid = jwtUtil.validateToken(malformedToken);

        assertFalse(isValid, "Malformed token should be rejected");
    }

    @Test
    void validateToken_ShouldReturnFalseForTokenWithOnlyTwoParts() {
        String invalidToken = "header.payload";

        boolean isValid = jwtUtil.validateToken(invalidToken);

        assertFalse(isValid, "Token with only 2 parts should be rejected");
    }

    @Test
    void validateToken_ShouldReturnFalseForExpiredToken() {
        JwtUtil shortExpirationUtil = new JwtUtil(TEST_SECRET, 1);
        String token = shortExpirationUtil.generateToken(TEST_USERNAME);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean isValid = shortExpirationUtil.validateToken(token);

        assertFalse(isValid, "Expired token should be rejected");
    }

    @Test
    void getUsername_ShouldReturnNullForMalformedToken() {
        String malformedToken = "invalid.token";

        String username = jwtUtil.getUsername(malformedToken);

        assertNull(username, "Username extraction from malformed token should return null");
    }

    @Test
    void getUsername_ShouldReturnNullForTokenWithInvalidBase64() {
        String invalidToken = "header.@@@invalid-base64@@@.signature";

        String username = jwtUtil.getUsername(invalidToken);

        assertNull(username, "Username extraction from invalid base64 should return null");
    }

    @Test
    void getUsername_ShouldReturnNullForTokenWithoutSubClaim() {
        String tokenWithoutSub = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2MzAwMDAwMDAsImV4cCI6MTYzMDAwMzYwMH0.signature";

        String username = jwtUtil.getUsername(tokenWithoutSub);

        assertNull(username, "Username extraction from token without sub claim should return null");
    }

    @Test
    void generateToken_WithDifferentUsernames_ShouldGenerateDifferentTokens() {
        String user1 = "user1@example.com";
        String user2 = "user2@example.com";

        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);

        assertNotEquals(token1, token2, "Tokens for different users should be different");
        assertEquals(user1, jwtUtil.getUsername(token1));
        assertEquals(user2, jwtUtil.getUsername(token2));
    }

    @Test
    void validateToken_WithDifferentSecrets_ShouldRejectToken() {
        JwtUtil util1 = new JwtUtil("secret1", TEST_EXPIRATION_MS);
        JwtUtil util2 = new JwtUtil("secret2", TEST_EXPIRATION_MS);
        String token = util1.generateToken(TEST_USERNAME);

        boolean isValid = util2.validateToken(token);

        assertFalse(isValid, "Token signed with different secret should be rejected");
    }

    @Test
    void generateToken_ShouldCreateTokenWithCorrectStructure() {
        String token = jwtUtil.generateToken(TEST_USERNAME);

        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
        
        assertDoesNotThrow(() -> java.util.Base64.getUrlDecoder().decode(parts[0]), 
            "Header should be valid base64");
        assertDoesNotThrow(() -> java.util.Base64.getUrlDecoder().decode(parts[1]), 
            "Payload should be valid base64");
        assertDoesNotThrow(() -> java.util.Base64.getUrlDecoder().decode(parts[2]), 
            "Signature should be valid base64");
    }

    @Test
    void validateToken_ShouldReturnFalseForEmptyToken() {
        String emptyToken = "";

        boolean isValid = jwtUtil.validateToken(emptyToken);

        assertFalse(isValid, "Empty token should be rejected");
    }

    @Test
    void validateToken_ShouldReturnFalseForNullToken() {
        boolean isValid = jwtUtil.validateToken(null);

        assertFalse(isValid, "Null token should be rejected");
    }

    @Test
    void getUsername_ShouldHandleSpecialCharactersInUsername() {
        String specialUsername = "user+special@example.com";

        String token = jwtUtil.generateToken(specialUsername);
        String extractedUsername = jwtUtil.getUsername(token);

        assertEquals(specialUsername, extractedUsername, 
            "Special characters in username should be preserved");
    }
}
