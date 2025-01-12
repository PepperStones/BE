package pepperstone.backend.challenge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pepperstone.backend.challenge.service.ChallengeService;
import pepperstone.backend.common.entity.UserEntity;

import java.util.Map;

@RestController
@RequestMapping("/challenge")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getChallengeList(@AuthenticationPrincipal UserEntity userInfo) {
        try {
            return ResponseEntity.ok().body(Map.of("code", 200, "data", challengeService.getChallengeList(userInfo)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "Internal server error. Please try again later."));
        }
    }
}
