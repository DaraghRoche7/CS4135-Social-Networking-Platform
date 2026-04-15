package com.studyhub.coreservice.auth.domain;

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
@Table(name = "user_follows")
public class UserFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_user_id", nullable = false)
    private StudyHubUser follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "followed_user_id", nullable = false)
    private StudyHubUser followed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected UserFollow() {
    }

    public UserFollow(StudyHubUser follower, StudyHubUser followed, Instant createdAt) {
        this.follower = follower;
        this.followed = followed;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public StudyHubUser getFollower() {
        return follower;
    }

    public StudyHubUser getFollowed() {
        return followed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
