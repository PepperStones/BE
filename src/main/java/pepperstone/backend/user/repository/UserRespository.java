package pepperstone.backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pepperstone.backend.common.entity.UserEntity;

public interface UserRespository extends JpaRepository<UserEntity, Long> {
    UserEntity findByUserId(String userId);
}
