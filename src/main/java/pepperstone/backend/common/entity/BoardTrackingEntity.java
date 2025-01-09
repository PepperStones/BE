package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "boardTracking")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardTrackingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // users : boardTracking = 1 : N
    @ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id")
    private UserEntity users;

    // boards : boardTracking = 1 : N
    @ManyToOne(targetEntity = BoardsEntity.class)
    @JoinColumn(name = "board_id")
    private BoardsEntity boards;
}
