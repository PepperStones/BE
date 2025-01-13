package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.JobQuestsEntity;

import java.util.Optional;

@Repository
public interface JobQuestRepository extends JpaRepository<JobQuestsEntity, Long> {
    Optional<JobQuestsEntity> findByDepartmentAndJobGroup(String department, String jobGroup);
    Optional<JobQuestsEntity> findById(Long id);
}
