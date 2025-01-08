package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "levels")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LevelsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String levelName; // 예: F1-Ⅰ, F1-Ⅱ 등

    @Column(nullable = false)
    private Integer requiredExperience; // 필요 경험치
}
