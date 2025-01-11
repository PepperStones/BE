package pepperstone.backend.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pepperstone.backend.common.repository.UserRepository;
import pepperstone.backend.notification.dto.FcmSendDto;
import pepperstone.backend.notification.service.FcmService;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class FcmController {
    private final FcmService fcmService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<Object> registerFcmToken(@RequestParam("id") Long id, @RequestParam("token") String token) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));
        fcmService.saveFcmToken(token, user);
        return ResponseEntity.ok(Map.of("success", true, "message", "FCM token registered successfully"));
    }

    @PostMapping("/send")
    public ResponseEntity<Object> sendNotification(@RequestBody FcmSendDto fcmSendDto) {
        if (fcmSendDto.getTitle() == null || fcmSendDto.getBody() == null || fcmSendDto.getToken() == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Missing required fields"));
        }
        try {
            fcmService.sendPushNotification(fcmSendDto.getTitle(), fcmSendDto.getBody(), fcmSendDto.getToken());
            return ResponseEntity.ok(Map.of("success", true, "message", "Notification sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", "Failed to send notification"));
        }
    }
}