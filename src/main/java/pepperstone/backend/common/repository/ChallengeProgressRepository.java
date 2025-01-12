package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.ChallengeProgressEntity;
import pepperstone.backend.common.entity.ChallengesEntity;
import pepperstone.backend.common.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeProgressRepository extends JpaRepository<ChallengeProgressEntity, Long> {
    List<ChallengeProgressEntity> findByUsers(UserEntity user);
    Optional<ChallengeProgressEntity> findByUsersAndChallenges(UserEntity user, ChallengesEntity challenge);
    Optional<ChallengeProgressEntity> findByIdAndUsers(Long id, UserEntity users);
}
