package com.cargaglobal.mobilebackend.application.usecase;

import com.cargaglobal.mobilebackend.application.exception.UserNotFoundException;
import com.cargaglobal.mobilebackend.domain.model.*;
import com.cargaglobal.mobilebackend.domain.port.CensusRepository;
import com.cargaglobal.mobilebackend.domain.port.UserRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;

public class CensusUseCase {
    private final CensusRepository censuses;
    private final UserRepository users;

    public CensusUseCase(CensusRepository censuses, UserRepository users) {
        this.censuses = censuses;
        this.users = users;
    }

    public Census capture(long userId, String name, String lastName, int totalMembers, int televisionCount,
                          double latitude, double longitude) {
        requireRole(userId, Role.CENSISTA);
        if (name == null || name.isBlank() || name.trim().length() > 120
                || lastName == null || lastName.isBlank() || lastName.trim().length() > 120
                || totalMembers < 1 || totalMembers > 1000 || televisionCount < 0 || televisionCount > 1000
                || !Double.isFinite(latitude) || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Datos de censo inválidos");
        }
        return censuses.save(new Census(null, name.trim(), lastName.trim(), totalMembers, televisionCount,
                latitude, longitude, CensusOrigin.NORMAL, userId, Instant.now()));
    }

    public CensusPage list(long userId, long afterId, int limit, CensusOrigin origin) {
        requireRole(userId, Role.ADMINISTRADOR);
        if (afterId < 0 || limit < 1 || limit > 500) throw new IllegalArgumentException("Paginación inválida");
        List<Census> found = censuses.findAfter(afterId, limit + 1, origin);
        boolean hasMore = found.size() > limit;
        List<Census> items = found.subList(0, Math.min(found.size(), limit));
        long nextCursor = items.isEmpty() ? afterId : items.getLast().id();
        return new CensusPage(items, nextCursor, hasMore);
    }

    private void requireRole(long userId, Role role) {
        User user = users.findById(userId).orElseThrow(UserNotFoundException::new);
        if (user.role() != role) throw new AccessDeniedException("Tu rol no tiene permiso para esta acción");
    }

    public record CensusPage(List<Census> items, long nextCursor, boolean hasMore) {}
}
