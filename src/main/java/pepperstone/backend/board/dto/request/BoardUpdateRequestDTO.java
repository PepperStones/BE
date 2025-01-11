package pepperstone.backend.board.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardUpdateRequestDTO {
    private String centerGroup;
    private String jobGroup;
    private String title;
    private String content;
}
