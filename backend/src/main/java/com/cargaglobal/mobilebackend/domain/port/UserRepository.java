package com.cargaglobal.mobilebackend.domain.port;

import com.cargaglobal.mobilebackend.domain.model.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    User save(User user);
    java.util.List<User> findFaceCandidates(int limit);
}
