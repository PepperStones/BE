package pepperstone.backend.mypage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pepperstone.backend.common.entity.enums.EvaluationPeriod;
import pepperstone.backend.common.entity.enums.Grade;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyInfoResponseDTO {
    private Long id;
    private String name;
    private String companyNum;
    private String centerGroup;
    private LocalDate joinDate;
    private String level;
    private EvaluationPeriod evaluationPeriod;
    private Grade grade;
    private int experience;
}
