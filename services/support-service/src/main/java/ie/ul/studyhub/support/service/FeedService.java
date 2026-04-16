package ie.ul.studyhub.support.service;

import ie.ul.studyhub.support.model.Post;
import ie.ul.studyhub.support.repo.PostLikeRepository;
import ie.ul.studyhub.support.repo.PostRepository;
import ie.ul.studyhub.support.repo.UserModuleRepository;
import ie.ul.studyhub.support.service.dto.FeedItemDto;
import ie.ul.studyhub.support.service.dto.FeedResponseDto;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class FeedService {
  private final PostRepository postRepository;
  private final UserModuleRepository userModuleRepository;
  private final PostLikeRepository postLikeRepository;

  public FeedService(
      PostRepository postRepository,
      UserModuleRepository userModuleRepository,
      PostLikeRepository postLikeRepository) {
    this.postRepository = postRepository;
    this.userModuleRepository = userModuleRepository;
    this.postLikeRepository = postLikeRepository;
  }

  /**
   * Not cached: the result depends on followed modules, likes, and new posts. A naive cache key
   * (user + page only) served stale empty feeds after users followed their first module.
   */
  public FeedResponseDto getFeed(String userId, String moduleCode, int page, int size) {
    int safePage = Math.max(0, page);
    int safeSize = Math.min(50, Math.max(1, size));

    // If a module filter is provided, respect it. Otherwise use the user's enrolled modules if present.
    List<String> userModules = moduleCode == null ? userModuleRepository.findModuleCodesByUserId(userId) : List.of();
    boolean modulesEmpty = moduleCode != null || userModules.isEmpty();

    // Never pass an empty collection to `IN :modules` — Hibernate/PostgreSQL can emit invalid `IN ()` SQL.
    List<String> modulesForQuery =
        userModules.isEmpty() ? List.of("__NO_MATCH__") : userModules;

    // Ordering is fully defined in the repository JPQL; extra Sort here can break pagination SQL.
    PageRequest pageable = PageRequest.of(safePage, safeSize);

    Page<Post> postPage =
        postRepository.findFeedPosts(userId, moduleCode, modulesForQuery, modulesEmpty, pageable);

    List<Long> postIds = postPage.getContent().stream().map(Post::getId).toList();
    Set<Long> liked = new HashSet<>();
    if (!postIds.isEmpty()) {
      liked.addAll(postLikeRepository.findPostIdsByUserAndPostIdIn(userId, postIds));
    }

    List<FeedItemDto> items =
        postPage.getContent().stream()
            .map(
                p ->
                    new FeedItemDto(
                        p.getId(),
                        p.getTitle(),
                        p.getDescription(),
                        p.getModuleCode(),
                        p.getUploaderUserId(),
                        p.getCreatedAt(),
                        p.getLikeCount(),
                        liked.contains(p.getId())))
            .toList();

    return new FeedResponseDto(items, safePage, safeSize, postPage.getTotalElements());
  }
}

