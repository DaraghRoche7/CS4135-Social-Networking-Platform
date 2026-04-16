package ie.ul.studyhub.support.repo;

import ie.ul.studyhub.support.model.PostComment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

  @Query("select distinct pc.postId from PostComment pc where pc.userId = :userId and pc.postId in :ids")
  List<Long> findPostIdsByUserAndPostIdIn(
      @Param("userId") String userId, @Param("ids") Collection<Long> ids);

  List<PostComment> findByPostIdOrderByCreatedAtAsc(Long postId);
}
