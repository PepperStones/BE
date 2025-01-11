package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.LeaderQuestProgressEntity;
import pepperstone.backend.common.entity.LeaderQuestsEntity;
import pepperstone.backend.common.entity.UserEntity;

import java.util.Optional;

@Repository
public interface LeaderQuestProgressRepository extends JpaRepository<LeaderQuestProgressEntity, Long> {
    Optional<LeaderQuestProgressEntity> findByLeaderQuestsAndUsersAndWeek(LeaderQuestsEntity leaderQuests, UserEntity user, int week);
    Optional<LeaderQuestProgressEntity> findByLeaderQuestsAndUsersAndMonth(LeaderQuestsEntity leaderQuests, UserEntity user, int month);
    Optional<LeaderQuestProgressEntity> findTopByLeaderQuestsAndUsersOrderByWeekDesc(LeaderQuestsEntity leaderQuest, UserEntity user);
    Optional<LeaderQuestProgressEntity> findTopByLeaderQuestsAndUsersOrderByMonthDesc(LeaderQuestsEntity leaderQuest, UserEntity user);

}