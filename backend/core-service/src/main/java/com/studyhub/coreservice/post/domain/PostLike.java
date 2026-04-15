package com.studyhub.coreservice.post.domain;

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
@Table(name = "post_likes")
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private StudyHubUser user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PostLike() {
    }

    public PostLike(Post post, StudyHubUser user, Instant createdAt) {
        this.post = post;
        this.user = user;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public StudyHubUser getUser() {
        return user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
