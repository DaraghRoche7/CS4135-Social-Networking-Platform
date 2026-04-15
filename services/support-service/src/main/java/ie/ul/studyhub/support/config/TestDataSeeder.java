package ie.ul.studyhub.support.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ie.ul.studyhub.support.model.Post;
import ie.ul.studyhub.support.repo.PostRepository;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Local/dev convenience: creates a background user via the API gateway and seeds posts from
 * {@code test-assets/} so they appear in the feed after following the module.
 *
 * <p>This does NOT log in any browser session; it only writes rows in the support-service DB.
 */
@Component
public class TestDataSeeder {
  private static final Logger log = LoggerFactory.getLogger(TestDataSeeder.class);

  private final PostRepository postRepository;
  private final ObjectMapper objectMapper;
  private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

  private final boolean enabled;
  private final String gatewayBaseUrl;
  private final String testAssetsDir;
  private final String moduleCode;
  private final String seedEmail;
  private final String seedPassword;
  private final String seedName;

  public TestDataSeeder(
      PostRepository postRepository,
      ObjectMapper objectMapper,
      @Value("${app.seed.enabled:false}") boolean enabled,
      @Value("${app.seed.gatewayBaseUrl:http://localhost:8080}") String gatewayBaseUrl,
      @Value("${app.seed.testAssetsDir:../../test-assets}") String testAssetsDir,
      @Value("${app.seed.moduleCode:CS4135}") String moduleCode,
      @Value("${app.seed.email:seedbot@studentmail.ul.ie}") String seedEmail,
      @Value("${app.seed.password:StudyHub!2026}") String seedPassword,
      @Value("${app.seed.name:Seed Bot}") String seedName) {
    this.postRepository = postRepository;
    this.objectMapper = objectMapper;
    this.enabled = enabled;
    this.gatewayBaseUrl = gatewayBaseUrl;
    this.testAssetsDir = testAssetsDir;
    this.moduleCode = moduleCode.trim().toUpperCase();
    this.seedEmail = seedEmail;
    this.seedPassword = seedPassword;
    this.seedName = seedName;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void seed() {
    if (!enabled) {
      return;
    }

    Exception last = null;
    for (int attempt = 1; attempt <= 10; attempt++) {
      try {
        String userId = registerOrLoginUserId();
        seedPostsFromTestAssets(userId);
        ensureDemoPost(userId);
        log.info("Test data seeding complete for userId={} module={}", userId, moduleCode);
        return;
      } catch (Exception e) {
        last = e;
        try {
          Thread.sleep(2000L);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          log.warn("Test data seeding interrupted");
          return;
        }
      }
    }

    log.warn("Test data seeding failed after retries: {}", last == null ? "unknown" : last.getMessage());
  }

  private String registerOrLoginUserId() throws IOException, InterruptedException {
    String registerBody =
        objectMapper
            .createObjectNode()
            .put("name", seedName)
            .put("email", seedEmail)
            .put("password", seedPassword)
            .toString();

    postJson(gatewayBaseUrl + "/api/auth/register", registerBody);

    String loginBody =
        objectMapper.createObjectNode().put("email", seedEmail).put("password", seedPassword).toString();

    String loginResp = postJson(gatewayBaseUrl + "/api/auth/login", loginBody);
    JsonNode root = objectMapper.readTree(loginResp);
    JsonNode userId = root.get("userId");
    if (userId == null || userId.isNull()) {
      throw new IllegalStateException("Auth response missing userId");
    }
    return userId.asText();
  }

  private String postJson(String url, String jsonBody) throws IOException, InterruptedException {
    HttpRequest req =
        HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

    HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
    int code = resp.statusCode();
    if (code >= 200 && code < 300) {
      return resp.body();
    }

    // Register may fail if user exists; caller may still proceed to login.
    if (url.endsWith("/api/auth/register") && code == 409) {
      return "{}";
    }

    throw new IllegalStateException("HTTP " + code + " for " + url + ": " + resp.body());
  }

  private void seedPostsFromTestAssets(String uploaderUserId) throws IOException {
    Path base = Paths.get(testAssetsDir).toAbsolutePath().normalize();
    if (!Files.exists(base)) {
      log.warn("test-assets directory not found at {}", base);
      return;
    }

    try (Stream<Path> s = Files.list(base)) {
      var files =
          s.filter(Files::isRegularFile)
              .filter(p -> !p.getFileName().toString().equals(".gitkeep"))
              .sorted(Comparator.comparing(p -> p.getFileName().toString()))
              .toList();

      int i = 0;
      for (Path p : files) {
        i++;
        String filename = p.getFileName().toString();
        String title = filename;
        String description = "Seeded test note from test-assets: " + filename;

        if (postRepository.existsByTitleAndModuleCodeAndUploaderUserId(title, moduleCode, uploaderUserId)) {
          continue;
        }

        Post post = new Post();
        post.setTitle(title);
        post.setDescription(description);
        post.setModuleCode(moduleCode);
        post.setUploaderUserId(uploaderUserId);
        post.setCreatedAt(Instant.now().minusSeconds((long) i * 60));
        post.setLikeCount(0);
        postRepository.save(post);
      }
    }
  }

  private void ensureDemoPost(String uploaderUserId) {
    String demoTitle = "[StudyHub] Demo — CS4135";
    if (postRepository.existsByModuleCodeAndTitle(moduleCode, demoTitle)) {
      return;
    }
    Post post = new Post();
    post.setTitle(demoTitle);
    post.setDescription(
        "Seeded demo post for this module. Follow 4135 or CS4135, then refresh your feed. "
            + "You can like, comment, or upload a PNG note.");
    post.setModuleCode(moduleCode);
    post.setUploaderUserId(uploaderUserId);
    post.setCreatedAt(Instant.now());
    post.setLikeCount(0);
    postRepository.save(post);
  }
}
