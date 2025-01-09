package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "challengeProgress")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeProgressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer currentCount; // 현재 달성 점수

    @Column(nullable = false)
    @ColumnDefault("0")
    private Boolean completed; // 완료 여부

    // users : challengeProgress = 1 : N
    @ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id")
    private UserEntity users;

    // challenges : challengeProgress = 1 : N
    @ManyToOne(targetEntity = ChallengesEntity.class)
    @JoinColumn(name = "challenge_id")
    private ChallengesEntity challenges;
}
