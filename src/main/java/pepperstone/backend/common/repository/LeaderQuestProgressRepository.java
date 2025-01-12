package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.LeaderQuestProgressEntity;
import pepperstone.backend.common.entity.LeaderQuestsEntity;
import pepperstone.backend.common.entity.UserEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderQuestProgressRepository extends JpaRepository<LeaderQuestProgressEntity, Long> {
    Optional<LeaderQuestProgressEntity> findTopByLeaderQuestsAndUsersOrderByWeekDesc(LeaderQuestsEntity leaderQuest, UserEntity user);
    Optional<LeaderQuestProgressEntity> findTopByLeaderQuestsAndUsersOrderByMonthDesc(LeaderQuestsEntity leaderQuest, UserEntity user);
    List<LeaderQuestProgressEntity> findByLeaderQuestsAndUsers(LeaderQuestsEntity leaderQuest, UserEntity user);
    List<LeaderQuestProgressEntity> findByLeaderQuestsIdAndUsers(Long id, UserEntity user);
    List<LeaderQuestProgressEntity> findByUsersAndCreatedAtBetween(UserEntity user, LocalDate startDate, LocalDate endDate);
    List<LeaderQuestProgressEntity> findByUsers(UserEntity user);
}