package ie.ul.studyhub.support.repo;

import ie.ul.studyhub.support.model.Post;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
  boolean existsByTitleAndModuleCodeAndUploaderUserId(String title, String moduleCode, String uploaderUserId);

  boolean existsByModuleCodeAndTitle(String moduleCode, String title);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update Post p set p.likeCount = p.likeCount + 1 where p.id = :id")
  int incrementLikeCount(@Param("id") Long id);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update Post p set p.likeCount = p.likeCount - 1 where p.id = :id and p.likeCount > 0")
  int decrementLikeCount(@Param("id") Long id);

  /**
   * Returns {@link Post} rows only (no {@code SELECT NEW} DTO). Hibernate is much more reliable
   * paginating entity queries than DTO constructor queries that embed {@code EXISTS} in the select
   * list — those often surfaced as HTTP 500 for {@code GET /api/feed}.
   */
  @Query(
      """
      select p from Post p
      where (:moduleCode is null or p.moduleCode = :moduleCode)
        and (:modulesEmpty = true or p.moduleCode in :modules)
      order by
        case when (
          exists (select 1 from PostLike pl2 where pl2.postId = p.id and pl2.userId = :userId)
          or
          exists (select 1 from PostComment pc2 where pc2.postId = p.id and pc2.userId = :userId)
        ) then 1 else 0 end asc,
        p.createdAt desc
      """)
  Page<Post> findFeedPosts(
      @Param("userId") String userId,
      @Param("moduleCode") String moduleCode,
      @Param("modules") Collection<String> modules,
      @Param("modulesEmpty") boolean modulesEmpty,
      Pageable pageable);
}

