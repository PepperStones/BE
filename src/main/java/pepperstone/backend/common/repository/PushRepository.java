package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.PushEntity;

@Repository
public interface PushRepository extends JpaRepository<PushEntity, Long> {
}
