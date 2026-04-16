package ie.ul.studyhub.support.web;

import ie.ul.studyhub.support.model.Post;
import ie.ul.studyhub.support.repo.PostRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import ie.ul.studyhub.support.util.ModuleCodes;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PostUploadController {
  private final PostRepository postRepository;
  private final Path uploadRoot;

  public PostUploadController(
      PostRepository postRepository, @Value("${app.upload.dir:uploads}") String uploadDir) {
    this.postRepository = postRepository;
    this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
  }

  @PostMapping(value = "/api/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public CreatedPostResponse upload(
      @RequestHeader(name = "X-User-Id", required = false) String userId,
      @RequestParam("title") String title,
      @RequestParam("description") String description,
      @RequestParam("module") String module,
      @RequestParam("file") MultipartFile file)
      throws IOException {
    if (userId == null || userId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id");
    }
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is required");
    }

    String original = Objects.requireNonNullElse(file.getOriginalFilename(), "note.png");
    String safeName = Paths.get(original).getFileName().toString();
    if (!safeName.toLowerCase().endsWith(".png")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PNG images are supported");
    }
    String ct = file.getContentType();
    if (ct != null && !ct.isBlank() && !ct.equalsIgnoreCase("image/png")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must be image/png");
    }

    Files.createDirectories(uploadRoot);
    String storedName = UUID.randomUUID() + "_" + safeName;
    Path dest = uploadRoot.resolve(storedName).normalize();
    if (!dest.startsWith(uploadRoot)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
    }
    file.transferTo(dest);

    String moduleCode = ModuleCodes.normalize(module);
    Post post = new Post();
    post.setTitle(title.trim());
    post.setDescription(description.trim());
    post.setModuleCode(moduleCode);
    post.setUploaderUserId(userId.trim());
    post.setCreatedAt(Instant.now());
    post.setLikeCount(0);
    post.setAttachmentFilename(storedName);

    Post saved = postRepository.save(post);
    return new CreatedPostResponse(saved.getId(), saved.getTitle(), saved.getModuleCode());
  }

  public record CreatedPostResponse(long id, String title, String moduleCode) {}
}
