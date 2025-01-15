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
public class UnlockResponseDTO {
    private Skin nowSkin;
    private Decoration nowDecoration;
    private Effect nowEffect;
    private List<String> skins;
    private String decorations;
    private String effects;
}
