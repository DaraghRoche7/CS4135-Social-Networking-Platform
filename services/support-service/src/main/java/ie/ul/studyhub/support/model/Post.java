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
    name = "posts",
    indexes = {
      @Index(name = "idx_posts_created_at", columnList = "createdAt"),
      @Index(name = "idx_posts_module", columnList = "moduleCode")
    })
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 4000)
  private String description;

  // Keep module as a simple string for now (e.g., "CS4135").
  @Column(nullable = false)
  private String moduleCode;

  @Column(nullable = false)
  private Long uploaderUserId;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  // Denormalized counter for simple ranking (can be recomputed later).
  @Column(nullable = false)
  private long likeCount = 0;
}

