package com.cargaglobal.mobilebackend.infrastructure.persistence;

import jakarta.persistence.*;
import com.cargaglobal.mobilebackend.domain.model.Role;
import java.time.LocalDate;

@Entity
@Table(name = "users")
class UserJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(name = "last_name", nullable = false, length = 120)
    private String lastName;
    @Column(nullable = false, unique = true, length = 180)
    private String username;
    @Column(nullable = false, unique = true, length = 180)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(length = 30)
    private String gender;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    @Column(length = 30)
    private String phone;
    @Lob
    @Column(name = "avatar_base64", columnDefinition = "LONGTEXT")
    private String avatarBase64;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    protected UserJpaEntity() {}
    UserJpaEntity(Long id, String name, String lastName, String username, String email,
                  String passwordHash, String gender, LocalDate birthDate, String phone,
                  String avatarBase64, Role role) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phone = phone;
        this.avatarBase64 = avatarBase64;
        this.role = role;
    }
    Long getId() { return id; }
    String getName() { return name; }
    String getLastName() { return lastName; }
    String getUsername() { return username; }
    String getEmail() { return email; }
    String getPasswordHash() { return passwordHash; }
    String getGender() { return gender; }
    LocalDate getBirthDate() { return birthDate; }
    String getPhone() { return phone; }
    String getAvatarBase64() { return avatarBase64; }
    Role getRole() { return role; }
}
