package ie.ul.studyhub.support.web;

import ie.ul.studyhub.support.service.FeedService;
import ie.ul.studyhub.support.service.dto.FeedResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class FeedController {
  private final FeedService feedService;

  public FeedController(FeedService feedService) {
    this.feedService = feedService;
  }

  /**
   * Temporary identity: until JWT is implemented, callers must provide X-User-Id.
   * In the planned architecture this comes from the API Gateway after validating JWT.
   */
  @GetMapping("/api/feed")
  @ResponseStatus(HttpStatus.OK)
  public FeedResponseDto getFeed(
      @RequestHeader(name = "X-User-Id", required = false) Long userId,
      @RequestParam(name = "module", required = false) String moduleCode,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "20") int size) {
    if (userId == null || userId <= 0) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id");
    }
    return feedService.getFeed(userId, moduleCode, page, size);
  }
}

