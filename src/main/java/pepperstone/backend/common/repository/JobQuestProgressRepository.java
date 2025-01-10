package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.JobQuestProgressEntity;

@Repository
public interface JobQuestProgressRepository extends JpaRepository<JobQuestProgressEntity, Long> {
}
