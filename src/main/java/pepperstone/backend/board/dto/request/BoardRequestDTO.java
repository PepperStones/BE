package pepperstone.backend.board.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardRequestDTO {
    private String centerGroup;
    private String jobGroup;

    @NotEmpty(message = "제목이 비어있습니다.")
    private String title;

    @NotEmpty(message = "내용이 비어있습니다.")
    private String content;
}
