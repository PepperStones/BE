package pepperstone.backend.challenge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeReceiveResponseDTO {
    private Long challengeProgressId;
    private Boolean receive;
}