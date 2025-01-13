package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.FcmEntity;
import pepperstone.backend.common.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmRepository extends JpaRepository<FcmEntity, Long> {
    Optional<FcmEntity> findByToken(String token);
    List<FcmEntity> findByUsers(UserEntity user);
}