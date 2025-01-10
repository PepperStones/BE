package pepperstone.backend.mypage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pepperstone.backend.common.entity.PerformanceEvaluationEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.ItemType;
import pepperstone.backend.mypage.dto.response.MyInfoResponseDTO;
import pepperstone.backend.mypage.dto.response.StarResponseDTO;
import pepperstone.backend.mypage.dto.resquest.UpdatePWRequestDTO;
import pepperstone.backend.mypage.dto.resquest.UpdateStarRequestDTO;
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

            if (user == null)
                throw new IllegalArgumentException("나의 정보가 없습니다.");

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

    @PatchMapping("/update")
    public ResponseEntity<Map<String, Object>> updatePW(@AuthenticationPrincipal UserEntity userInfo,
                                                        @RequestBody UpdatePWRequestDTO dto,
                                                        BindingResult bindingResult) {
        try {
            mypageService.validation(bindingResult, "currentPassword");
            mypageService.validation(bindingResult, "newPassword");
            mypageService.validation(bindingResult, "confirmPassword");

            final UserEntity user = mypageService.getUserInfo(userInfo.getId());

            if (user == null)
                throw new IllegalArgumentException("나의 정보가 없습니다.");

            final Boolean checkUser = mypageService.checkPW(user.getId(), dto.getCurrentPassword());

            if (!checkUser)
                throw new IllegalArgumentException("현재 사용 중인 비밀번호를 입력해주세요");

            if (!dto.getNewPassword().equals(dto.getConfirmPassword()))
                throw new IllegalArgumentException("새 비밀번호 확인이 일치하지 않습니다.");

            if (!mypageService.validPW(dto.getNewPassword()))
                throw new IllegalArgumentException("비밀번호는 8~30자의 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다.");

            user.setPassword(dto.getNewPassword());

            mypageService.updateUser(user);

            return ResponseEntity.ok().body(Map.of("code", 200, "data", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "나의 정보 불러오기 오류. 잠시 후 다시 시도해주세요."));
        }
    }

    @PatchMapping("/star")
    public ResponseEntity<Map<String, Object>> updateStar(@AuthenticationPrincipal UserEntity userInfo, @RequestBody UpdateStarRequestDTO dto) {
        try {
            final UserEntity user = mypageService.getUserInfo(userInfo.getId());

            if (user == null)
                throw new IllegalArgumentException("나의 정보가 없습니다.");

            if (dto.getSkin() != null) {
                mypageService.validateUnlocked(userInfo.getId(), ItemType.SKIN, dto.getSkin().name());
                user.setSkin(dto.getSkin());
            }

            if (dto.getDecoration() != null) {
                mypageService.validateUnlocked(userInfo.getId(), ItemType.DECORATION, dto.getDecoration().name());
                user.setDecoration(dto.getDecoration());
            }

            if (dto.getEffect() != null) {
                mypageService.validateUnlocked(userInfo.getId(), ItemType.EFFECT, dto.getEffect().name());
                user.setEffect(dto.getEffect());
            }

            final UserEntity starInfo = mypageService.updateUser(user);

            final StarResponseDTO resDTO = StarResponseDTO.builder()
                    .skin(starInfo.getSkin())
                    .decoration(starInfo.getDecoration())
                    .effect(starInfo.getEffect())
                    .build();

            return ResponseEntity.ok().body(Map.of("code", 200, "data", resDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "로그인 오류. 잠시 후 다시 시도해주세요."));
        }
    }
}
