package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    List<UserEntity> findByJobGroupJobNameAndJobGroupCenterGroupCenterName(String jobName, String centerName);
    Optional<UserEntity> findByCompanyNumAndName(String companyNum, String name);
}