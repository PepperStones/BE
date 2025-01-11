package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pepperstone.backend.common.entity.CenterGroupEntity;

public interface CenterGroupRepository extends JpaRepository<CenterGroupEntity, Long> {
    CenterGroupEntity findByCenterName(String centerName);
    Boolean existsByCenterName(String centerName);
}
