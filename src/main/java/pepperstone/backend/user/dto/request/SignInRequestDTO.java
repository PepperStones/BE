package pepperstone.backend.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignInRequestDTO {
    @Pattern(regexp = "^.{1,30}$", message = "아이디를 1 ~ 30자로 입력해주세요.")
    private String userId;

    @Pattern(regexp = "^.{1,30}$", message = "비밀번호를 1 ~ 30자로 입력해주세요.")
    private String password;
}
