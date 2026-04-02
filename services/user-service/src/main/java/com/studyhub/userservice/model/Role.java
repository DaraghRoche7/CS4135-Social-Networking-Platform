package com.studyhub.userservice.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_id", updatable = false, nullable = false)
    private UUID roleId;
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private RoleName name;

    public Role() {}

    public Role(RoleName name) {
        this.name = name;
    }
    public UUID getRoleId() { return roleId; }
    public RoleName getName() { return name; }
    public void setName(RoleName name) { this.name = name; }
}