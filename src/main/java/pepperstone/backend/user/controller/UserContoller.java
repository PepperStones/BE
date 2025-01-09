package pepperstone.backend.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.security.TokenProvider;
import pepperstone.backend.user.dto.request.SignInRequestDTO;
import pepperstone.backend.user.dto.response.TokenResponseDTO;
import pepperstone.backend.user.service.UserService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/auth")
public class UserContoller {
    private final UserService userService;

    private final PasswordEncoder pwEncoder = new BCryptPasswordEncoder();
    private final TokenProvider tokenProvider;

    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signin(@RequestBody @Valid SignInRequestDTO dto, BindingResult bindingResult) {
        try {
            userService.validation(bindingResult, "userId");
            userService.validation(bindingResult, "password");

            final UserEntity user = userService.getByCredentials(dto.getUserId(), dto.getPassword());

            if(user.getId() == null)
                throw new IllegalArgumentException("로그인에 실패했습니다.");

            final String accessToken = tokenProvider.createAccessToken(user);
            final String refreshToken = tokenProvider.createRefreshToken(user);

            final TokenResponseDTO resDTO = TokenResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

            return ResponseEntity.ok().body(Map.of("code", 200, "data", resDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "로그인 오류. 잠시 후 다시 시도해주세요."));
        }
    }
}
