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
public class QuestProgressResponseDTO {
    private List<jobQuests> jobQuests;
    private List<leaderQuests> leaderQuests;

    @Data
    @Builder
    @AllArgsConstructor
    public static class jobQuests {
        private Long id;
        private Period period;
        private Integer accumulatedExperience;
        private Integer maxScore;
        private Integer mediumScore;
        private Double maxStandard;
        private Double mediumStandard;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class leaderQuests {
        private Long id;
        private Period period;
        private Integer accumulatedExperience;
        private String questName;
        private Integer maxPoints;
        private Integer medianPoints;
        private String maxCondition;
        private String medianCondition;
        private Integer weight;
    }
}
