package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;
import pepperstone.backend.common.entity.enums.Skin;
import pepperstone.backend.common.entity.enums.Decoration;
import pepperstone.backend.common.entity.enums.Effect;
import pepperstone.backend.common.entity.enums.UserRole;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyNum; // 사번

    @Column(nullable = false)
    private String password; // 비밀번호

    @Column(nullable = false)
    private String name; // 이름

    @Column(nullable = false)
    private LocalDate joinDate; // 입사일

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER; // Default 값: USER

    @Column(nullable = false)
    private Integer accumulatedExperience; // 올해 제외 누적 경험치

    @Column(nullable = false)
    private String level; // 레벨

    @Enumerated(EnumType.STRING)
    private Skin skin = Skin.S0; // Default 값: S0

    @Enumerated(EnumType.STRING)
    private Decoration decoration = Decoration.D0; // Default 값: D0

    @Enumerated(EnumType.STRING)
    private Effect effect = Effect.E0; // Default 값: E0

    // jobGroup : users = 1 : N
    @ManyToOne(targetEntity = JobGroupEntity.class)
    @JoinColumn(name = "jobGroup_id")
    private JobGroupEntity jobGroup;

    // users : performanceEvaluations = 1 : N
    @OneToMany(mappedBy = "users")
    private List<PerformanceEvaluationEntity> performanceEvaluations = new ArrayList<>();

    // users : jobQuestProgresses = 1 : N
    @OneToMany(mappedBy = "users")
    private List<JobQuestProgressEntity> jobQuestProgresses = new ArrayList<>();

    // users : productivityHistories = 1 : N
    @OneToMany(mappedBy = "users")
    private List<JobQuestProductivityHistoryEntity> productivityHistories = new ArrayList<>();

    // users : leaderQuestProgresses = 1 : N
    @OneToMany(mappedBy = "users")
    private List<LeaderQuestProgressEntity> leaderQuestProgresses = new ArrayList<>();

    // users : projects = 1 : N
    @OneToMany(mappedBy = "users")
    private List<ProjectsEntity> projects = new ArrayList<>();

    // users : boards = 1 : N
    @OneToMany(mappedBy = "users")
    private List<BoardsEntity> boards = new ArrayList<>();

    // users : push = 1 : N
    @OneToMany(mappedBy = "users")
    private List<PushEntity> pushes = new ArrayList<>();

    // users : fcm = 1 : N
    @OneToMany(mappedBy = "users")
    private List<FcmEntity> fcm = new ArrayList<>();

    // users : challengeProgress = 1 : N
    @OneToMany(mappedBy = "users")
    private List<ChallengeProgressEntity> challengeProgress = new ArrayList<>();

    // users : boardTracking = 1 : N
    @OneToMany(mappedBy = "users")
    private List<BoardTrackingEntity> boardTracking = new ArrayList<>();
}
