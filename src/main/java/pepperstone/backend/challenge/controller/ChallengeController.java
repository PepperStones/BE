package pepperstone.backend.challenge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pepperstone.backend.challenge.service.ChallengeService;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.repository.UserRepository;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/challenge")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;
    private final UserRepository userRepository;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getChallengeList(@AuthenticationPrincipal UserEntity userInfo) {
        try {
            return ResponseEntity.ok().body(Map.of("code", 200, "data", challengeService.getChallengeList(userInfo)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", "Internal server error. Please try again later."));
        }
    }

    @PatchMapping("/receive")
    public ResponseEntity<Map<String, Object>> receiveChallengeReward(
            @AuthenticationPrincipal UserEntity userInfo,
            @RequestParam("challengeProgressId") Long challengeProgressId) {
        try {
            Optional<UserEntity> user = userRepository.findById(userInfo.getId());

            if (user.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("code", 404, "message", "Not Found: 사용자 정보가 존재하지 않습니다."));
            }

            return ResponseEntity.ok().body(Map.of("code", 200, "data", challengeService.receiveReward(user.get(), challengeProgressId)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", "Internal server error. Please try again later."));
        }
    }
}
