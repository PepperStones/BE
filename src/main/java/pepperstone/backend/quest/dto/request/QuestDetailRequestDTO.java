package pepperstone.backend.quest.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestDetailRequestDTO {
    @NotEmpty(message = "퀘스트 종류가 비어있습니다.")
    private String type;

    @NotEmpty(message = "퀘스트 아이디가 비어있습니다.")
    private Long questId;
}
