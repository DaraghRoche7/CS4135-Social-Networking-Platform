package ie.ul.studyhub.support.repo;

import ie.ul.studyhub.support.model.UserModule;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserModuleRepository extends JpaRepository<UserModule, Long> {
  @Query("select um.moduleCode from UserModule um where um.userId = :userId")
  List<String> findModuleCodesByUserId(@Param("userId") String userId);

  boolean existsByUserIdAndModuleCode(String userId, String moduleCode);

  Optional<UserModule> findByUserIdAndModuleCode(String userId, String moduleCode);
}

