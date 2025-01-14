package pepperstone.backend.challenge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeDto {
    private Long challengesId;
    private String name;
    private String description;
    private Integer requiredCount;
    private String itemValue;
    private ChallengeProgressDto challengeProgress;
}
