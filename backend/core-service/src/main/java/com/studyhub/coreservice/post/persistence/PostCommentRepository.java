package com.studyhub.coreservice.post.persistence;

import com.studyhub.coreservice.post.domain.Post;
import com.studyhub.coreservice.post.domain.PostComment;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @EntityGraph(attributePaths = "user")
    List<PostComment> findByPostOrderByCreatedAtAsc(Post post);

    @Transactional
    long deleteByPost(Post post);
}