package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.LeaderQuestsEntity;

import java.util.Optional;

@Repository
public interface LeaderQuestRepository extends JpaRepository<LeaderQuestsEntity, Long> {
    Optional<LeaderQuestsEntity> findByDepartmentAndJobGroupAndQuestName(String department, String jobGroup, String questName);
}