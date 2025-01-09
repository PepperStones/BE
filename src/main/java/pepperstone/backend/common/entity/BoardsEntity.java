package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;
import pepperstone.backend.common.entity.enums.TargetType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boards")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 게시글 제목

    @Column(nullable = false)
    private String content; // 게시글 내용

    @Column(nullable = false)
    private LocalDateTime createdAt; // 작성 시간

    @Column(nullable = false)
    private LocalDateTime updatedAt; // 수정 시간

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    private TargetType targetType;

    // users : boards = 1 : N
    @ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id")
    private UserEntity users;

    // centerGroup : boards = 1 : N
    @ManyToOne(targetEntity = CenterGroupEntity.class)
    @JoinColumn(name = "centerGroup_id")
    private CenterGroupEntity centerGroup;

    // jobGroup : boards = 1 : N
    @ManyToOne(targetEntity = JobGroupEntity.class)
    @JoinColumn(name = "jobGroup_id")
    private JobGroupEntity jobGroup;

    // boards : boardTracking = 1 : N
    @OneToMany(mappedBy = "boards")
    private List<BoardTrackingEntity> boardTracking = new ArrayList<>();
}
