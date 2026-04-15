package ie.ul.studyhub.support.web;

import ie.ul.studyhub.support.service.PostEngagementService;
import ie.ul.studyhub.support.service.dto.CommentResponse;
import ie.ul.studyhub.support.service.dto.NewCommentRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PostEngagementController {
  private final PostEngagementService postEngagementService;

  public PostEngagementController(PostEngagementService postEngagementService) {
    this.postEngagementService = postEngagementService;
  }

  @PostMapping("/api/posts/{postId}/like")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void like(
      @PathVariable long postId, @RequestHeader(name = "X-User-Id", required = false) String userId) {
    requireUserId(userId);
    postEngagementService.likePost(postId, userId.trim());
  }

  @DeleteMapping("/api/posts/{postId}/like")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unlike(
      @PathVariable long postId, @RequestHeader(name = "X-User-Id", required = false) String userId) {
    requireUserId(userId);
    postEngagementService.unlikePost(postId, userId.trim());
  }

  @GetMapping("/api/posts/{postId}/comments")
  public List<CommentResponse> listComments(@PathVariable long postId) {
    return postEngagementService.listComments(postId);
  }

  @PostMapping("/api/posts/{postId}/comments")
  @ResponseStatus(HttpStatus.CREATED)
  public CommentResponse addComment(
      @PathVariable long postId,
      @RequestHeader(name = "X-User-Id", required = false) String userId,
      @RequestBody NewCommentRequest body) {
    requireUserId(userId);
    if (body == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body required");
    }
    return postEngagementService.addComment(postId, userId.trim(), body.body());
  }

  private static void requireUserId(String userId) {
    if (userId == null || userId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id");
    }
  }
}
