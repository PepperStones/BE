package pepperstone.backend.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.repository.UserRepository;
import pepperstone.backend.notification.service.FcmService;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class FcmController {
    private final FcmService fcmService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerFcmToken(@AuthenticationPrincipal UserEntity userInfo,
                                                                @RequestParam String token) {
        try {
            // 입력값 검증
            if (userInfo == null || token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "Missing required fields"));
            }

            // FCM 토큰 저장
            fcmService.saveFcmToken(token, userInfo);
            return ResponseEntity.ok(Map.of("code", 200, "message", "FCM token registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error registering FCM token: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", "Internal server error. Please try again later."));
        }
    }
}