package pepperstone.backend.common.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pepperstone.backend.common.entity.UserEntity;

import java.util.Optional;

public interface UserRespository extends JpaRepository<UserEntity, Long> {
    UserEntity findByUserId(String userId);
    Optional<UserEntity> findById(Long id);

    @Query("SELECT u FROM UserEntity u " +
            "JOIN u.jobGroup j " +
            "JOIN j.centerGroup c " +
            "WHERE (:search IS NULL OR u.name LIKE %:search% OR u.companyNum LIKE %:search%) " +
            "AND (:centerGroup IS NULL OR c.centerName = :centerGroup) " +
            "AND (:jobGroup IS NULL OR j.jobName = :jobGroup)")
    Slice<UserEntity> findAllWithFilters(@Param("search") String search,
                                         @Param("centerGroup") String centerGroup,
                                         @Param("jobGroup") String jobGroup,
                                         Pageable pageable);
}