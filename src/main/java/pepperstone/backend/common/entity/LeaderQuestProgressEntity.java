package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "leaderQuestProgress")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaderQuestProgressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String achievement; // 달성 내용

    @Column
    private Integer week; // 현재 주차 (leaderQuest의 period가 MONTHLY면 null)

    @Column
    private Integer month; // 현재 월차 (leaderQuest의 period가 WEEKLY면 null)

    @Column(nullable = false)
    private Integer accumulatedExperience; // 이전 주차 누적 경험치

    @Column(nullable = false)
    private Integer experience; // 획득 경험치

    @Column(nullable = false)
    private LocalDate createdAt; // 부여 날짜

    // leaderQuest : leaderQuestProgress = 1 : N
    @ManyToOne(targetEntity = LeaderQuestsEntity.class)
    @JoinColumn(name = "leaderQuest_id")
    private LeaderQuestsEntity leaderQuests;

    // user : leaderQuestProgress = 1 : N
    @ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id")
    private UserEntity users;
}
