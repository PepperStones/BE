package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;
import pepperstone.backend.common.entity.enums.EvaluationPeriod;
import pepperstone.backend.common.entity.enums.Grade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "performanceEvaluation")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceEvaluationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationPeriod evaluationPeriod; // 상반기, 하반기

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade; // 인사평가 등급

    @Column(nullable = false)
    private int experience; // 획득 경험치

    @Column(nullable = false)
    private LocalDate createdAt; // 부여 날짜

    // users : performanceEvaluation = 1 : N
    @ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id")
    private UserEntity users;
}
