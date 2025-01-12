package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.ProjectsEntity;
import pepperstone.backend.common.entity.UserEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjectsRepository extends JpaRepository<ProjectsEntity, Long> {
    boolean existsByUsersAndProjectName(UserEntity users, String projectName);
    List<ProjectsEntity> findByUsersAndCreatedAtBetween(UserEntity user, LocalDate startDate, LocalDate endDate);
}