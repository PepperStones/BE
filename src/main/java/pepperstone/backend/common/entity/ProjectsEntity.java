package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "projects")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String projectName; // 프로젝트명

    @Column(nullable = false)
    private LocalDate createdAt; // 부여 일자 (동기화한 날짜)

    @Column(nullable = false)
    private Integer experience; // 부여된 경험치

    // users : projects = 1 : N
    @ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id")
    private UserEntity users;
}
