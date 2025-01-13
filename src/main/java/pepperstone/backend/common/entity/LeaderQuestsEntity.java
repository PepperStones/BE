package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.*;
import pepperstone.backend.common.entity.enums.Period;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "leaderQuests")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaderQuestsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String department; // 소속

    @Column(nullable = false)
    private String jobGroup; // 직무 그룹

    @Column(nullable = false)
    private String questName; // 퀘스트명

    @Enumerated(EnumType.STRING)
    private Period period; // 주기

    @Column(nullable = false)
    private Integer weight;  // 비중 (%)

    @Column(nullable = false)
    private String maxCondition; // MAX 조건

    @Column(nullable = false)
    private String medianCondition; // MEDIAN 조건

    @Column(nullable = false)
    private Integer maxPoints; // MAX 경험치

    @Column(nullable = false)
    private Integer medianPoints; // MEDIAN 경험치

    // leaderQuests : leaderQuestProgresses = 1 : N
    @OneToMany(mappedBy = "leaderQuests")
    private List<LeaderQuestProgressEntity> leaderQuestProgresses = new ArrayList<>();
}
