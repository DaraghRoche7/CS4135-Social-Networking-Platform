package ie.ul.studyhub.support.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "post_comments",
    indexes = {
      @Index(name = "idx_post_comments_user", columnList = "userId"),
      @Index(name = "idx_post_comments_post", columnList = "postId")
    })
public class PostComment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long postId;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false, length = 4000)
  private String body;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();
}

