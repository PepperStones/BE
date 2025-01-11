package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pepperstone.backend.common.entity.BoardsEntity;

public interface BoardsRepository extends JpaRepository<BoardsEntity, Long> {
}
