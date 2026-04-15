package ie.ul.studyhub.support.repo;

import ie.ul.studyhub.support.model.PostLike;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

  @Query("select distinct pl.postId from PostLike pl where pl.userId = :userId and pl.postId in :ids")
  List<Long> findPostIdsByUserAndPostIdIn(
      @Param("userId") String userId, @Param("ids") Collection<Long> ids);

  boolean existsByPostIdAndUserId(Long postId, String userId);

  void deleteByPostIdAndUserId(Long postId, String userId);
}
