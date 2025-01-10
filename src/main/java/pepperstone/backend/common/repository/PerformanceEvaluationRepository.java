package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.PerformanceEvaluationEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.EvaluationPeriod;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PerformanceEvaluationRepository extends JpaRepository<PerformanceEvaluationEntity, Long> {
    // 특정 유저, 평가 기간, 연도에 해당하는 인사평가 데이터를 삭제하는 메서드
    void deleteByUsersAndEvaluationPeriodAndCreatedAtBetween(UserEntity users, EvaluationPeriod evaluationPeriod, LocalDate startDate, LocalDate endDate);
}