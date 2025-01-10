package pepperstone.backend.mypage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pepperstone.backend.common.entity.PerformanceEvaluationEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.mypage.dto.MyInfoResponseDTO;
import pepperstone.backend.mypage.service.MypageService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/mypage")
public class MypageController {
    private final MypageService mypageService;

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getMyInfo(@AuthenticationPrincipal UserEntity userInfo) {
        try {
            final UserEntity user = mypageService.getUserInfo(userInfo.getId());
            final PerformanceEvaluationEntity evaluation = mypageService.getPerformanceEvaluation(userInfo.getId());

            final MyInfoResponseDTO resDTO = MyInfoResponseDTO.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .companyNum(user.getCompanyNum())
                    .joinDate(user.getJoinDate())
                    .level(user.getLevel())
                    .evaluationPeriod(evaluation.getEvaluationPeriod())
                    .grade(evaluation.getGrade())
                    .experience(evaluation.getExperience())
                    .build();

            return ResponseEntity.ok().body(Map.of("code", 200, "data", resDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "나의 정보 불러오기 오류. 잠시 후 다시 시도해주세요."));
        }
    }
}
