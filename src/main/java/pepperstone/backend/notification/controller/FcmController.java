package pepperstone.backend.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.notification.dto.response.NotificationDto;
import pepperstone.backend.notification.service.FcmService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class FcmController {
    private final FcmService fcmService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerFcmToken(@AuthenticationPrincipal UserEntity userInfo,
                                                                @RequestParam("token") String token) {
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

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getNotificationList(@AuthenticationPrincipal UserEntity userInfo) {
        try {
            return ResponseEntity.ok(Map.of("code", 200, "data", fcmService.getNotificationList(userInfo)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "Unauthorized: 인증에 실패했습니다."));
        } catch (RuntimeException e) {
            log.error("Error retrieving notifications: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", "Internal Server Error: 알림 목록을 가져오는 도중 오류가 발생했습니다."));
        }
    }

    @PatchMapping("/open")
    public ResponseEntity<Map<String, Object>> openNotification(@AuthenticationPrincipal UserEntity userInfo,
                                                                @RequestParam("pushId") Long pushId) {
        try {
            return ResponseEntity.ok(Map.of("code", 200, "data", fcmService.openNotification(userInfo, pushId)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", "Internal server error. Please try again later."));
        }
    }
}