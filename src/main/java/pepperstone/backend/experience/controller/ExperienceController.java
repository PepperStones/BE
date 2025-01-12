package pepperstone.backend.experience.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.repository.UserRepository;
import pepperstone.backend.experience.service.ExperienceService;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/exp")
@RequiredArgsConstructor
public class ExperienceController {
    private final ExperienceService experienceService;
    private final UserRepository userRepository;
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentExperience(@AuthenticationPrincipal UserEntity userInfo) {
        try {
            Optional<UserEntity> user = userRepository.findById(userInfo.getId());

            if (user.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("code", 404, "message", "Not Found: 사용자 정보가 존재하지 않습니다."));
            }

            return ResponseEntity.ok(Map.of("code", 200, "data", experienceService.getCurrentExperience(user.get())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", e.getMessage()));
        }
    }
}