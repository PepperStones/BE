package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "jobQuestProductivityHistory")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobQuestProductivityHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer year; // 년

    @Column(nullable = false)
    private Integer week; // 월

    @Column(nullable = false)
    private Double productivity; // 생산성

    // users : jobQuestProductivityHistory = 1 : N
    @ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id")
    private UserEntity users;
}
