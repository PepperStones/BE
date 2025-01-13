package pepperstone.backend.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardResponseDTO {
    private Long id;
    private String centerGroup;
    private String jobGroup;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String content;
}
