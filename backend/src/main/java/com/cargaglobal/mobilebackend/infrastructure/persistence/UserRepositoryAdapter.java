package com.cargaglobal.mobilebackend.infrastructure.persistence;

import com.cargaglobal.mobilebackend.domain.model.User;
import com.cargaglobal.mobilebackend.domain.port.UserRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
class UserRepositoryAdapter implements UserRepository {
    private final SpringDataUserRepository repository;
    UserRepositoryAdapter(SpringDataUserRepository repository) { this.repository = repository; }

    public Optional<User> findById(Long id) { return repository.findById(id).map(this::toDomain); }
    public Optional<User> findByUsername(String username) { return repository.findByUsername(username).map(this::toDomain); }
    public boolean existsByEmail(String email) { return repository.existsByEmail(email); }
    public boolean existsByUsername(String username) { return repository.existsByUsername(username); }
    public java.util.List<User> findFaceCandidates(int limit) {
        return repository.findFaceCandidates(org.springframework.data.domain.PageRequest.of(0,limit))
                .stream().map(this::toDomain).toList();
    }
    public User save(User user) {
        return toDomain(repository.save(new UserJpaEntity(
                user.id(), user.name(), user.lastName(), user.username(), user.email(),
                user.passwordHash(), user.gender(), user.birthDate(), user.phone(), user.avatarBase64(), user.role()
        )));
    }
    private User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(), entity.getName(), entity.getLastName(), entity.getUsername(),
                entity.getEmail(), entity.getPasswordHash(), entity.getGender(), entity.getBirthDate(),
                entity.getPhone(), entity.getAvatarBase64(), entity.getRole()
        );
    }
}
