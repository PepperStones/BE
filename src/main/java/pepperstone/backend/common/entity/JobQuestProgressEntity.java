package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.checker.units.qual.C;

import java.time.LocalDate;

@Entity
@Table(name = "jobQuestProgress")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobQuestProgressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer week; // 현재 주차

    @Column(nullable = false)
    private Integer accumulatedExperience; // 이전 주차 누적 경험치

    @Column(nullable = false)
    private LocalDate createdAt; // 부여 날짜

    @Column(nullable = false)
    private Double productivity; // 생산성

    @Column(nullable = false)
    private Integer experience; // 획득 경험치

    // jobQuests : jobQuestProgress = 1 : N
    @ManyToOne(targetEntity = JobQuestsEntity.class)
    @JoinColumn(name = "jobQuest_id")
    private JobQuestsEntity jobQuest;

    // users : jobQuestProgress = 1 : N
    @ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id")
    private UserEntity users;
}
