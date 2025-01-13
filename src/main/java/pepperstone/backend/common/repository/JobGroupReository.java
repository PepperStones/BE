package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pepperstone.backend.common.entity.JobGroupEntity;

public interface JobGroupReository extends JpaRepository<JobGroupEntity, Long> {
}
