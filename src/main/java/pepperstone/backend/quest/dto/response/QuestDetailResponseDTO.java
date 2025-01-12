package pepperstone.backend.quest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pepperstone.backend.common.entity.enums.Period;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestDetailResponseDTO {
    private Period period;
    private List<Quest> questList;

    @Data
    @Builder
    @AllArgsConstructor
    public static class Quest {
        private Integer unit;
        private Integer experience;
    }
}
