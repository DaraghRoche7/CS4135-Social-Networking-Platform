package ie.ul.studyhub.support.service;

import ie.ul.studyhub.support.model.PostComment;
import ie.ul.studyhub.support.model.PostLike;
import ie.ul.studyhub.support.repo.PostCommentRepository;
import ie.ul.studyhub.support.repo.PostLikeRepository;
import ie.ul.studyhub.support.repo.PostRepository;
import ie.ul.studyhub.support.service.dto.CommentResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostEngagementService {
  private static final int MAX_COMMENT_LEN = 4000;

  private final PostRepository postRepository;
  private final PostLikeRepository postLikeRepository;
  private final PostCommentRepository postCommentRepository;

  public PostEngagementService(
      PostRepository postRepository,
      PostLikeRepository postLikeRepository,
      PostCommentRepository postCommentRepository) {
    this.postRepository = postRepository;
    this.postLikeRepository = postLikeRepository;
    this.postCommentRepository = postCommentRepository;
  }

  @Transactional
  public void likePost(long postId, String userId) {
    ensurePost(postId);
    if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
      return;
    }
    PostLike row = new PostLike();
    row.setPostId(postId);
    row.setUserId(userId);
    row.setCreatedAt(Instant.now());
    postLikeRepository.save(row);
    postRepository.incrementLikeCount(postId);
  }

  @Transactional
  public void unlikePost(long postId, String userId) {
    ensurePost(postId);
    if (!postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
      return;
    }
    postLikeRepository.deleteByPostIdAndUserId(postId, userId);
    postRepository.decrementLikeCount(postId);
  }

  @Transactional(readOnly = true)
  public List<CommentResponse> listComments(long postId) {
    ensurePost(postId);
    return postCommentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
        .map(c -> new CommentResponse(c.getId(), c.getUserId(), c.getBody(), c.getCreatedAt()))
        .toList();
  }

  @Transactional
  public CommentResponse addComment(long postId, String userId, String body) {
    ensurePost(postId);
    if (body == null || body.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment body is required");
    }
    String trimmed = body.trim();
    if (trimmed.length() > MAX_COMMENT_LEN) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment is too long");
    }

    PostComment c = new PostComment();
    c.setPostId(postId);
    c.setUserId(userId);
    c.setBody(trimmed);
    c.setCreatedAt(Instant.now());
    PostComment saved = postCommentRepository.save(c);
    return new CommentResponse(saved.getId(), saved.getUserId(), saved.getBody(), saved.getCreatedAt());
  }

  private void ensurePost(long postId) {
    if (!postRepository.existsById(postId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
    }
  }
}
