package pepperstone.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pepperstone.backend.common.entity.UnlockStatusEntity;
import pepperstone.backend.common.entity.enums.ItemType;

public interface UnlockStatusRepository extends JpaRepository<UnlockStatusEntity, Long> {
    Boolean existsByUsersIdAndItemTypeAndItemValue(Long userId, ItemType itemType, String itemValue);
}