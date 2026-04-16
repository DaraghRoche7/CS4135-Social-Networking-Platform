package com.studyhub.coreservice.post.persistence;

import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.post.domain.Post;
import com.studyhub.coreservice.post.domain.PostLike;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostAndUser(Post post, StudyHubUser user);

    void deleteByPost(Post post);

    @Query("select postLike.post.id, count(postLike) from PostLike postLike where postLike.post.id in :postIds group by postLike.post.id")
    List<Object[]> countLikesByPostIds(@Param("postIds") Collection<Long> postIds);

    @Query("select postLike.post.id from PostLike postLike where postLike.post.id in :postIds and postLike.user.publicId = :userId")
    List<Long> findLikedPostIds(@Param("postIds") Collection<Long> postIds, @Param("userId") String userId);
}
