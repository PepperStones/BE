package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.JobQuestProgressEntity;
import pepperstone.backend.common.entity.JobQuestsEntity;
import pepperstone.backend.common.entity.UserEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobQuestProgressRepository extends JpaRepository<JobQuestProgressEntity, Long> {
    List<JobQuestProgressEntity> findByJobQuestAndUsers(JobQuestsEntity jobQuest, UserEntity user);
    List<JobQuestProgressEntity> findByUsersAndCreatedAtBetween(UserEntity user, LocalDate start, LocalDate end);
}
