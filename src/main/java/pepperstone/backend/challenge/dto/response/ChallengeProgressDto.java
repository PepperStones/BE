package pepperstone.backend.challenge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeProgressDto {
    private Long challengeProgressId;
    private Integer currentCount;
    private Boolean completed;
    private Boolean receive;
}
