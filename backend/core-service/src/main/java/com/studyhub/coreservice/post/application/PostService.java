package com.studyhub.coreservice.post.application;

import com.studyhub.coreservice.auth.application.UserNotFoundException;
import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.auth.persistence.StudyHubUserRepository;
import com.studyhub.coreservice.auth.persistence.UserFollowRepository;
import com.studyhub.coreservice.post.api.dto.CreatePostRequest;
import com.studyhub.coreservice.post.api.dto.PostListResponse;
import com.studyhub.coreservice.post.api.dto.PostResponse;
import com.studyhub.coreservice.post.api.dto.UpdatePostRequest;
import com.studyhub.coreservice.post.domain.Post;
import com.studyhub.coreservice.post.domain.PostLike;
import com.studyhub.coreservice.post.persistence.PostLikeRepository;
import com.studyhub.coreservice.post.persistence.PostRepository;
import java.time.Clock;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final StudyHubUserRepository studyHubUserRepository;
    private final UserFollowRepository userFollowRepository;
    private final PostStorageService postStorageService;
    private final Clock clock;

    public PostService(
        PostRepository postRepository,
        PostLikeRepository postLikeRepository,
        StudyHubUserRepository studyHubUserRepository,
        UserFollowRepository userFollowRepository,
        PostStorageService postStorageService,
        Clock clock
    ) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.studyHubUserRepository = studyHubUserRepository;
        this.userFollowRepository = userFollowRepository;
        this.postStorageService = postStorageService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PostListResponse getFeed(String currentUserId, String moduleFilter) {
        StudyHubUser currentUser = getRequiredUser(currentUserId);
        Collection<StudyHubUser> feedAuthors = buildFeedAuthors(currentUser);
        List<Post> posts = normalizeModule(moduleFilter) == null
            ? postRepository.findByAuthorInOrderByCreatedAtDesc(feedAuthors)
            : postRepository.findByAuthorInAndModuleCodeIgnoreCaseOrderByCreatedAtDesc(feedAuthors, normalizeModule(moduleFilter));
        return new PostListResponse(toResponses(posts, currentUserId));
    }

    @Transactional(readOnly = true)
    public PostListResponse getPosts(String currentUserId, String moduleFilter) {
        List<Post> posts = normalizeModule(moduleFilter) == null
            ? postRepository.findAllByOrderByCreatedAtDesc()
            : postRepository.findByModuleCodeIgnoreCaseOrderByCreatedAtDesc(normalizeModule(moduleFilter));
        return new PostListResponse(toResponses(posts, currentUserId));
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId, String currentUserId) {
        return toResponse(getRequiredPost(postId), currentUserId);
    }

    @Transactional
    public PostResponse createPost(CreatePostRequest request, String currentUserId) {
        StudyHubUser author = getRequiredUser(currentUserId);
        String storagePath = postStorageService.storePdf(request.getFile());
        try {
            Post post = postRepository.save(new Post(
                author,
                request.getTitle().trim(),
                request.getDescription().trim(),
                normalizeRequiredModule(request.getModule()),
                request.getFile().getOriginalFilename(),
                request.getFile().getContentType() == null ? "application/pdf" : request.getFile().getContentType(),
                request.getFile().getSize(),
                storagePath,
                clock.instant(),
                clock.instant()
            ));
            return toResponse(post, currentUserId);
        } catch (RuntimeException ex) {
            postStorageService.deleteIfExists(storagePath);
            throw ex;
        }
    }

    @Transactional
    public PostResponse updatePost(Long postId, UpdatePostRequest request, String currentUserId, boolean admin) {
        Post post = getRequiredPost(postId);
        requireOwnershipOrAdmin(post, currentUserId, admin);
        post.updateMetadata(
            request.title().trim(),
            request.description().trim(),
            normalizeRequiredModule(request.module()),
            clock.instant()
        );
        return toResponse(post, currentUserId);
    }

    @Transactional
    public void deletePost(Long postId, String currentUserId, boolean admin) {
        Post post = getRequiredPost(postId);
        requireOwnershipOrAdmin(post, currentUserId, admin);
        postLikeRepository.deleteByPost(post);
        postRepository.delete(post);
        postStorageService.deleteIfExists(post.getStoragePath());
    }

    @Transactional
    public PostResponse likePost(Long postId, String currentUserId) {
        StudyHubUser currentUser = getRequiredUser(currentUserId);
        Post post = getRequiredPost(postId);
        postLikeRepository.findByPostAndUser(post, currentUser)
            .orElseGet(() -> postLikeRepository.save(new PostLike(post, currentUser, clock.instant())));
        return toResponse(post, currentUserId);
    }

    @Transactional
    public PostResponse unlikePost(Long postId, String currentUserId) {
        StudyHubUser currentUser = getRequiredUser(currentUserId);
        Post post = getRequiredPost(postId);
        postLikeRepository.findByPostAndUser(post, currentUser)
            .ifPresent(postLikeRepository::delete);
        return toResponse(post, currentUserId);
    }

    @Transactional(readOnly = true)
    public PostFileDownload downloadPostFile(Long postId) {
        Post post = getRequiredPost(postId);
        Resource resource = postStorageService.loadAsResource(post.getStoragePath());
        return new PostFileDownload(resource, post.getContentType(), post.getOriginalFileName(), post.getFileSize());
    }

    private Collection<StudyHubUser> buildFeedAuthors(StudyHubUser currentUser) {
        Map<String, StudyHubUser> authors = new LinkedHashMap<>();
        authors.put(currentUser.getPublicId(), currentUser);
        userFollowRepository.findByFollowerOrderByCreatedAtDesc(currentUser).forEach(follow ->
            authors.put(follow.getFollowed().getPublicId(), follow.getFollowed()));
        return authors.values();
    }

    private List<PostResponse> toResponses(List<Post> posts, String currentUserId) {
        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, Long> likeCounts = new HashMap<>();
        for (Object[] row : postLikeRepository.countLikesByPostIds(postIds)) {
            likeCounts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        Set<Long> likedPostIds = new HashSet<>(postLikeRepository.findLikedPostIds(postIds, currentUserId));

        return posts.stream()
            .map(post -> PostResponse.from(
                post,
                likeCounts.getOrDefault(post.getId(), 0L),
                likedPostIds.contains(post.getId())
            ))
            .toList();
    }

    private PostResponse toResponse(Post post, String currentUserId) {
        return toResponses(List.of(post), currentUserId).get(0);
    }

    private StudyHubUser getRequiredUser(String currentUserId) {
        return studyHubUserRepository.findByPublicId(currentUserId)
            .orElseThrow(() -> new UserNotFoundException(currentUserId));
    }

    private Post getRequiredPost(Long postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));
    }

    private void requireOwnershipOrAdmin(Post post, String currentUserId, boolean admin) {
        if (!admin && !post.getAuthor().getPublicId().equals(currentUserId)) {
            throw new AccessDeniedException("You cannot modify another user's post");
        }
    }

    private String normalizeRequiredModule(String module) {
        String normalized = normalizeModule(module);
        if (normalized == null) {
            throw new IllegalArgumentException("Module is required");
        }
        return normalized;
    }

    private String normalizeModule(String module) {
        if (module == null || module.isBlank()) {
            return null;
        }
        return module.trim().toUpperCase(Locale.ROOT);
    }

    public record PostFileDownload(
        Resource resource,
        String contentType,
        String originalFileName,
        long fileSize
    ) {
    }
}
