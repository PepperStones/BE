package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;
import pepperstone.backend.common.entity.enums.Period;

import java.util.List;

@Entity
@Table(name = "jobQuests")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobQuestsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String department; // 소속

    @Column(nullable = false)
    private String jobGroup; // 직무 그룹

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Period period; // 주기

    @Column(nullable = false)
    private Integer maxScore; // MAX 점수

    @Column(nullable = false)
    private Integer mediumScore; // MEDIUM 점수

    @Column(nullable = false)
    private Double maxStandard; // MAX 기준

    @Column(nullable = false)
    private Double mediumStandard; // MEDIUM 기준

    // jobQuests : jobQuestProgresses = 1 : N
    @OneToMany(mappedBy = "jobQuest")
    private List<JobQuestProgressEntity> jobQuestProgresses;
}
