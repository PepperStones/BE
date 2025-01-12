package pepperstone.backend.quest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyQuestResponseDTO {
    private Integer week;
    private Integer experience;
}
