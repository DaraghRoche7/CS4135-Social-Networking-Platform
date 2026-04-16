package ie.ul.studyhub.support.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "post_likes",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_post_likes_post_user", columnNames = {"postId", "userId"})
    },
    indexes = {
      @Index(name = "idx_post_likes_user", columnList = "userId"),
      @Index(name = "idx_post_likes_post", columnList = "postId")
    })
public class PostLike {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long postId;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();
}

