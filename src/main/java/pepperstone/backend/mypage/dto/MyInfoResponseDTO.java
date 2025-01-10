package pepperstone.backend.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pepperstone.backend.common.entity.enums.EvaluationPeriod;
import pepperstone.backend.common.entity.enums.Grade;
import pepperstone.backend.common.entity.enums.Level;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyInfoResponseDTO {
    private Long id;
    private String name;
    private String companyNum;
    private LocalDate joinDate;
//    private Level level;
    private String level;
    private EvaluationPeriod evaluationPeriod;
    private Grade grade;
    private int experience;
}
