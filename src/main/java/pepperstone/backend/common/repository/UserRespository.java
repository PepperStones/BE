package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pepperstone.backend.common.entity.UserEntity;

import java.util.Optional;

public interface UserRespository extends JpaRepository<UserEntity, Long> {
    UserEntity findByUserId(String userId);
    Optional<UserEntity> findById(Long id);
}
