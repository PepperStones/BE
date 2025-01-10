package pepperstone.backend.mypage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pepperstone.backend.common.entity.enums.Decoration;
import pepperstone.backend.common.entity.enums.Effect;
import pepperstone.backend.common.entity.enums.Skin;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StarResponseDTO {
    private Skin skin;
    private Decoration decoration;
    private Effect effect;
}
