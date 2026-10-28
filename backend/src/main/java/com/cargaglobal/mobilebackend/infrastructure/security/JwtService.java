package com.cargaglobal.mobilebackend.infrastructure.security;

import com.cargaglobal.mobilebackend.application.port.TokenService;
import com.cargaglobal.mobilebackend.domain.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtService implements TokenService {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final String HEADER = ENCODER.encodeToString(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8)
    );
    private static final Pattern SUBJECT = Pattern.compile("\\\"sub\\\":\\\"(\\d+)\\\"");
    private static final Pattern EXPIRATION = Pattern.compile("\\\"exp\\\":(\\d+)");
    private final byte[] secret;
    private final long expirationSeconds;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-seconds:86400}") long expirationSeconds
    ) {
        if (secret.length() < 32) throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 caracteres");
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public String create(User user) {
        try {
            long issuedAt = Instant.now().getEpochSecond();
            long expiration = issuedAt + expirationSeconds;
            String json = "{\"sub\":\"" + user.id() + "\",\"iat\":" + issuedAt +
                    ",\"exp\":" + expiration + "}";
            String payload = ENCODER.encodeToString(json.getBytes(StandardCharsets.UTF_8));
            String content = HEADER + "." + payload;
            return content + "." + ENCODER.encodeToString(sign(content));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo crear el token", ex);
        }
    }

    public Optional<Long> validateAndGetUserId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return Optional.empty();
            String content = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(content), DECODER.decode(parts[2]))) return Optional.empty();
            String claims = new String(DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            Matcher subject = SUBJECT.matcher(claims);
            Matcher expires = EXPIRATION.matcher(claims);
            if (!subject.find() || !expires.find()) return Optional.empty();
            long expiration = Long.parseLong(expires.group(1));
            if (Instant.now().getEpochSecond() >= expiration) return Optional.empty();
            return Optional.of(Long.valueOf(subject.group(1)));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private byte[] sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
    }
}
