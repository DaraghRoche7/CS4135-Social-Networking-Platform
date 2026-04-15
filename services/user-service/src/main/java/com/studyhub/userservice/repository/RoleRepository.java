package com.studyhub.userservice.repository;

import com.studyhub.userservice.model.Role;
import com.studyhub.userservice.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleName name);
}