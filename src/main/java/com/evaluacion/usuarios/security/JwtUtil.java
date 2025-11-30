package com.evaluacion.usuarios.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final byte[] secretBytes;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret:default-secret-key-please-change}") String secret,
                   @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        try {
            ObjectMapper om = new ObjectMapper();
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", username);
            payload.put("iat", now.getTime());
            payload.put("exp", expiry.getTime());

            String headerJson = om.writeValueAsString(header);
            String payloadJson = om.writeValueAsString(payload);

            String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

            String signingInput = encodedHeader + "." + encodedPayload;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            byte[] sig = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
            String encodedSig = Base64.getUrlEncoder().withoutPadding().encodeToString(sig);

            return signingInput + "." + encodedSig;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            ObjectMapper om = new ObjectMapper();
            Map<?, ?> payload = om.readValue(payloadJson, Map.class);
            Object sub = payload.get("sub");
            return sub != null ? sub.toString() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;
            String signingInput = parts[0] + "." + parts[1];
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            byte[] expected = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
            byte[] provided = Base64.getUrlDecoder().decode(parts[2]);
            boolean sigOk = java.security.MessageDigest.isEqual(expected, provided);
            if (!sigOk) return false;

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            ObjectMapper om = new ObjectMapper();
            Map<?, ?> payload = om.readValue(payloadJson, Map.class);
            Object expObj = payload.get("exp");
            if (expObj == null) return false;
            long exp = Long.parseLong(expObj.toString());
            return System.currentTimeMillis() <= exp;
        } catch (Exception ex) {
            return false;
        }
    }
}
