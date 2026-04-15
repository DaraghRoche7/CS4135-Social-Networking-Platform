package ie.ul.studyhub.support.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "user_modules",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_user_modules_user_module", columnNames = {"userId", "moduleCode"})
    },
    indexes = {
      @Index(name = "idx_user_modules_user", columnList = "userId"),
      @Index(name = "idx_user_modules_module", columnList = "moduleCode")
    })
public class UserModule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String moduleCode;
}

