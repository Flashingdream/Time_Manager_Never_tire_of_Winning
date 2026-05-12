package time_manager.demo.java.com.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private static final String SECRET = "time-manager-hmac-secret-2026-key";
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 hours
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String generateToken(String userId, String role) {
        try {
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("role", role);
            payload.put("exp", System.currentTimeMillis() + EXPIRATION_MS);

            String headerB64 = base64Encode(mapper.writeValueAsString(header));
            String payloadB64 = base64Encode(mapper.writeValueAsString(payload));
            String signingInput = headerB64 + "." + payloadB64;
            String signature = hmacSha256(signingInput, SECRET);

            return signingInput + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Token generation failed", e);
        }
    }

    public static Map<String, Object> parseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String signingInput = parts[0] + "." + parts[1];
            String expectedSig = hmacSha256(signingInput, SECRET);
            if (!expectedSig.equals(parts[2])) return null;

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = mapper.readValue(payloadJson, Map.class);

            long exp = ((Number) claims.get("exp")).longValue();
            if (System.currentTimeMillis() > exp) return null;

            return claims;
        } catch (Exception e) {
            return null;
        }
    }

    private static String base64Encode(String str) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    private static String hmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
}
