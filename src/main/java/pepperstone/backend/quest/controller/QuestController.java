package pepperstone.backend.quest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.quest.QuestProgressResponseDTO;
import pepperstone.backend.quest.service.QuestService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/quest")
public class QuestController {
    private final QuestService questService;

    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> getProgress(@AuthenticationPrincipal UserEntity userInfo) {
        try {
            final UserEntity user = questService.getUserInfo(userInfo.getId());

            if (user == null)
                throw new IllegalArgumentException("나의 정보가 없습니다.");

            final List<QuestProgressResponseDTO.jobQuests> jobQuests = questService.getJobQuests(user);
            final List<QuestProgressResponseDTO.leaderQuests> leaderQuests = questService.getLeaderQuests(user);

            final QuestProgressResponseDTO resDTO = QuestProgressResponseDTO.builder()
                    .jobQuests(jobQuests)
                    .leaderQuests(leaderQuests)
                    .build();

            return ResponseEntity.ok().body(Map.of("code", 200, "data", resDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "로그인 오류. 잠시 후 다시 시도해주세요."));
        }
    }
}
