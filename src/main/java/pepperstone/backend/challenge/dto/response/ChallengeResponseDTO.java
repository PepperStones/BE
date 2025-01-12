package pepperstone.backend.challenge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeResponseDTO {
    private Long challengesId;
    private String name;
    private String description;
    private Integer requiredCount;
    private ChallengeProgressResponseDTO challengeProgress;
}
