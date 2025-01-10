package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.ProjectsEntity;
import pepperstone.backend.common.entity.UserEntity;

@Repository
public interface ProjectsRepository extends JpaRepository<ProjectsEntity, Long> {
    boolean existsByUsersAndProjectName(UserEntity users, String projectName);
}