package pepperstone.backend.mypage.dto.resquest;

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
public class UpdateStarRequestDTO {
    private Skin skin;
    private Decoration decoration;
    private Effect effect;
}
