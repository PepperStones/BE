package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pepperstone.backend.common.entity.enums.ChallengeType;

import java.util.List;

@Entity
@Table(name = "challenges")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 도전 과제 이름

    @Column(nullable = false)
    private String description; // 도전 과제 설명

    @Column(nullable = false)
    private Integer requiredCount; // 달성 필요 횟수

    @Enumerated(EnumType.STRING)
    private ChallengeType type;

    // challenges : challengeProgress = 1 : N
    @OneToMany(mappedBy = "challenges")
    private List<ChallengeProgressEntity> challengeProgress;
}
