package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.ChallengesEntity;
import pepperstone.backend.common.entity.enums.ChallengeType;

import java.util.Optional;

@Repository
public interface ChallengesRepository extends JpaRepository<ChallengesEntity, Long> {
    Optional<ChallengesEntity> findByType(ChallengeType type);
}
