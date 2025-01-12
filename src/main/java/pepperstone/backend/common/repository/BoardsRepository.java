package pepperstone.backend.common.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pepperstone.backend.common.entity.BoardsEntity;

public interface BoardsRepository extends JpaRepository<BoardsEntity, Long> {
    @Query("SELECT b FROM BoardsEntity b " +
            "WHERE (:centerGroup IS NULL AND :jobGroup IS NULL) " +
            "OR (b.centerGroup IS NULL AND b.jobGroup IS NULL) " +
            "OR (b.centerGroup = :centerGroup AND b.jobGroup IS NULL) " +
            "OR (b.centerGroup = :centerGroup AND b.jobGroup = :jobGroup)")
    Slice<BoardsEntity> findAllWithFilters(@Param("centerGroup") String centerGroup,
                                           @Param("jobGroup") String jobGroup,
                                           Pageable pageable);
}
