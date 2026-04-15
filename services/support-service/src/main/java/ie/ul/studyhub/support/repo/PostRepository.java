package ie.ul.studyhub.support.repo;

import ie.ul.studyhub.support.service.dto.FeedItemDto;
import ie.ul.studyhub.support.model.Post;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
  @Query(
      """
      select new ie.ul.studyhub.support.service.dto.FeedItemDto(
        p.id,
        p.title,
        p.description,
        p.moduleCode,
        p.uploaderUserId,
        p.createdAt,
        p.likeCount,
        (
          exists (select 1 from PostLike pl where pl.postId = p.id and pl.userId = :userId)
          or
          exists (select 1 from PostComment pc where pc.postId = p.id and pc.userId = :userId)
        )
      )
      from Post p
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
  Page<FeedItemDto> getFeed(
      @Param("userId") long userId,
      @Param("moduleCode") String moduleCode,
      @Param("modules") Collection<String> modules,
      @Param("modulesEmpty") boolean modulesEmpty,
      Pageable pageable);
}

