package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.PushEntity;
import pepperstone.backend.common.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushRepository extends JpaRepository<PushEntity, Long> {
    List<PushEntity> findByUsersOrderByCreatedAtDesc(UserEntity user);
    Optional<PushEntity> findByIdAndUsers(Long id, UserEntity user);
}
