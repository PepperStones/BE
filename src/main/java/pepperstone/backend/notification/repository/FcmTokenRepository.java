package pepperstone.backend.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.FcmEntity;

import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmEntity, Long> {
    Optional<FcmEntity> findByToken(String token);
}