package com.studyhub.coreservice.post.persistence;

import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.post.domain.Post;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "author")
    List<Post> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "author")
    List<Post> findByModuleCodeIgnoreCaseOrderByCreatedAtDesc(String moduleCode);

    @EntityGraph(attributePaths = "author")
    List<Post> findByAuthorInOrderByCreatedAtDesc(Collection<StudyHubUser> authors);

    @EntityGraph(attributePaths = "author")
    List<Post> findByAuthorInAndModuleCodeIgnoreCaseOrderByCreatedAtDesc(
        Collection<StudyHubUser> authors,
        String moduleCode
    );
}
