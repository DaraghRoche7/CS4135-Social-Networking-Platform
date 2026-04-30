package com.studyhub.coreservice.post.persistence;

import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.post.domain.Post;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @EntityGraph(attributePaths = "author")
    @Query(
            "select p from Post p "
                    + "where p.author in :authors "
                    + "or upper(p.moduleCode) in :moduleCodes "
                    + "order by p.createdAt desc"
    )
    List<Post> findFeedByAuthorsOrModules(
            @Param("authors") Collection<StudyHubUser> authors,
            @Param("moduleCodes") Collection<String> moduleCodes
    );

    @EntityGraph(attributePaths = "author")
    @Query(
            "select p from Post p "
                    + "where (p.author in :authors or upper(p.moduleCode) in :moduleCodes) "
                    + "and upper(p.moduleCode) = :filterModule "
                    + "order by p.createdAt desc"
    )
    List<Post> findFeedByAuthorsOrModulesAndModuleFilter(
            @Param("authors") Collection<StudyHubUser> authors,
            @Param("moduleCodes") Collection<String> moduleCodes,
            @Param("filterModule") String filterModule
    );
}