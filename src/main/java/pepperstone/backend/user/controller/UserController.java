package pepperstone.backend.user.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pepperstone.backend.common.config.JwtProperties;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.security.TokenProvider;
import pepperstone.backend.user.dto.request.SignInRequestDTO;
import pepperstone.backend.user.dto.response.NewTokenResponseDTO;
import pepperstone.backend.user.dto.response.TokenResponseDTO;
import pepperstone.backend.user.service.UserService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final JwtProperties jwtProperties;

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
                    .userRole(user.getRole().name())
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

    @PostMapping("/newToken")
    public ResponseEntity<Map<String, Object>> createNewToken(HttpServletRequest request){
        try {
            String token = request.getHeader("Authorization").substring(7);

            Claims claims = Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .parseClaimsJws(token)
                    .getBody();

            Long id = Long.parseLong(claims.getSubject());

            UserEntity userInfo = userService.getUserInfo(id);
            final NewTokenResponseDTO resDTO = NewTokenResponseDTO.builder()
                    .id(userInfo.getId())
                    .accessToken(tokenProvider.createAccessToken(userInfo))
                    .build();

            return ResponseEntity.ok().body(Map.of("code", 200, "data", resDTO));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("code", 500, "data", "newToken fail"));
        }
    }
}
