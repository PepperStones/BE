package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column
    private String centerGroup; // 센터 그룹

    @Column
    private String jobGroup; // 직무 그룹

    @Column(nullable = false)
    private LocalDateTime createdAt; // 작성 시간

    @Column(nullable = false)
    private LocalDateTime updatedAt; // 수정 시간

    // users : boards = 1 : N
    @ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id")
    private UserEntity users;

    // boards : boardTracking = 1 : N
    @OneToMany(mappedBy = "boards", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardTrackingEntity> boardTracking = new ArrayList<>();
}
