package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepperstone.backend.common.entity.BoardTrackingEntity;
import pepperstone.backend.common.entity.BoardsEntity;
import pepperstone.backend.common.entity.UserEntity;

@Repository
public interface BoardTrackingRepository extends JpaRepository<BoardTrackingEntity, Long> {
    boolean existsByUsersAndBoards(UserEntity user, BoardsEntity board);
}