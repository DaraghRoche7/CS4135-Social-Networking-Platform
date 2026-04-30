package com.studyhub.coreservice.module.domain;

import com.studyhub.coreservice.auth.domain.StudyHubUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "followed_modules")
public class FollowedModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private StudyHubUser user;

    @Column(name = "module_code", nullable = false, length = 40)
    private String moduleCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected FollowedModule() {
    }

    public FollowedModule(StudyHubUser user, String moduleCode, Instant createdAt) {
        this.user = user;
        this.moduleCode = moduleCode;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public StudyHubUser getUser() {
        return user;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}