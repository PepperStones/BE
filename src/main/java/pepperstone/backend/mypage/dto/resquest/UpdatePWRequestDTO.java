package pepperstone.backend.mypage.dto.resquest;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePWRequestDTO {
    @NotEmpty(message = "현재 비밀번호가 비어있습니다.")
    private String currentPassword;

    @NotEmpty(message = "새 비밀번호가 비어있습니다.")
    private String newPassword;

    @NotEmpty(message = "새 비밀번호 확인이 비어있습니다.")
    private String confirmPassword;
}
