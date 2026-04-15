package ie.ul.studyhub.support.web;

import ie.ul.studyhub.support.model.UserModule;
import ie.ul.studyhub.support.repo.UserModuleRepository;
import ie.ul.studyhub.support.service.dto.FollowModuleRequestDto;
import ie.ul.studyhub.support.util.ModuleCodes;
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
public class ModuleController {
  private final UserModuleRepository userModuleRepository;

  public ModuleController(UserModuleRepository userModuleRepository) {
    this.userModuleRepository = userModuleRepository;
  }

  @GetMapping("/api/modules")
  public List<String> listFollowedModules(@RequestHeader(name = "X-User-Id", required = false) String userId) {
    if (userId == null || userId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id");
    }
    return userModuleRepository.findModuleCodesByUserId(userId);
  }

  @PostMapping("/api/modules/follow")
  @ResponseStatus(HttpStatus.CREATED)
  public void followModule(
      @RequestHeader(name = "X-User-Id", required = false) String userId,
      @RequestBody FollowModuleRequestDto body) {
    if (userId == null || userId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id");
    }
    if (body == null || body.moduleCode() == null || body.moduleCode().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "moduleCode is required");
    }

    String moduleCode = ModuleCodes.normalize(body.moduleCode());
    if (userModuleRepository.existsByUserIdAndModuleCode(userId, moduleCode)) {
      return; // idempotent
    }

    UserModule um = new UserModule();
    um.setUserId(userId);
    um.setModuleCode(moduleCode);
    userModuleRepository.save(um);
  }

  @DeleteMapping("/api/modules/{moduleCode}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unfollowModule(
      @RequestHeader(name = "X-User-Id", required = false) String userId,
      @PathVariable String moduleCode) {
    if (userId == null || userId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id");
    }
    if (moduleCode == null || moduleCode.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "moduleCode is required");
    }
    String normalized = ModuleCodes.normalize(moduleCode);
    userModuleRepository
        .findByUserIdAndModuleCode(userId, normalized)
        .ifPresent(userModuleRepository::delete);
  }
}

