package ie.ul.studyhub.support.service;

import ie.ul.studyhub.support.repo.PostRepository;
import ie.ul.studyhub.support.repo.UserModuleRepository;
import ie.ul.studyhub.support.service.dto.FeedItemDto;
import ie.ul.studyhub.support.service.dto.FeedResponseDto;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class FeedService {
  private final PostRepository postRepository;
  private final UserModuleRepository userModuleRepository;

  public FeedService(PostRepository postRepository, UserModuleRepository userModuleRepository) {
    this.postRepository = postRepository;
    this.userModuleRepository = userModuleRepository;
  }

  @Cacheable(cacheNames = "feed", key = "'u=' + #userId + '|m=' + (#moduleCode == null ? '' : #moduleCode) + '|p=' + #page + '|s=' + #size")
  public FeedResponseDto getFeed(long userId, String moduleCode, int page, int size) {
    int safePage = Math.max(0, page);
    int safeSize = Math.min(50, Math.max(1, size));

    // If a module filter is provided, respect it. Otherwise use the user's enrolled modules if present.
    List<String> userModules = moduleCode == null ? userModuleRepository.findModuleCodesByUserId(userId) : List.of();
    boolean modulesEmpty = moduleCode != null || userModules.isEmpty();

    PageRequest pageable =
        PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<FeedItemDto> result =
        postRepository.getFeed(userId, moduleCode, userModules, modulesEmpty, pageable);

    return new FeedResponseDto(result.getContent(), safePage, safeSize, result.getTotalElements());
  }
}

