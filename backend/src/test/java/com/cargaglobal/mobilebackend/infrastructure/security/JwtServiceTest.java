package com.cargaglobal.mobilebackend.infrastructure.security;

import com.cargaglobal.mobilebackend.domain.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    private static final String SECRET = "test-secret-with-more-than-32-characters";

    @Test
    void createsAndValidatesSignedToken() {
        JwtService jwt = new JwtService(SECRET, 3600);
        User user = new User(7L, "Ana", "López", "ana", "ana@example.com", "hash",
                "Femenino", null, null, null);
        String token = jwt.create(user);

        assertThat(jwt.validateAndGetUserId(token)).contains(7L);
        assertThat(jwt.validateAndGetUserId(token + "altered")).isEmpty();
    }
}
