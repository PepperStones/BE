package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pepperstone.backend.common.entity.PerformanceEvaluationEntity;

import java.util.List;

public interface PerformanceEvaluationRepository extends JpaRepository<PerformanceEvaluationEntity, Long> {
    List<PerformanceEvaluationEntity> findAllByUsersId(Long userId);
}
