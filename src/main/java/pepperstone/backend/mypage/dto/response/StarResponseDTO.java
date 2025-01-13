package pepperstone.backend.mypage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pepperstone.backend.common.entity.enums.Decoration;
import pepperstone.backend.common.entity.enums.Effect;
import pepperstone.backend.common.entity.enums.Skin;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StarResponseDTO {
    private Skin skin;
    private Decoration decoration;
    private Effect effect;
    private List<unlockList> unlocklist;

    @Data
    @Builder
    @AllArgsConstructor
    public static class unlockList {
        private String skin;
        private String decoration;
        private String effect;
    }
}
