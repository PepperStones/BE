package pepperstone.backend.member.controller;

import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pepperstone.backend.common.entity.CenterGroupEntity;
import pepperstone.backend.common.entity.JobGroupEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.member.dto.request.MemberAddRequestDTO;
import pepperstone.backend.member.dto.request.MemberUpdateRequestDTO;
import pepperstone.backend.member.dto.response.MemberInfoResponseDTO;
import pepperstone.backend.member.dto.response.MembersResponseDTO;
import pepperstone.backend.member.service.MemberService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/list")
    @PageableAsQueryParam
    public ResponseEntity<Map<String, Object>> getMemberList(@AuthenticationPrincipal UserEntity userInfo,
                                                             @RequestParam(value = "search", required = false) String search,
                                                             @RequestParam(value = "centerGroup", required = false) String centerGroup,
                                                             @RequestParam(value = "jobGroup", required = false) String jobGroup,
                                                             @Parameter(hidden = true) @PageableDefault(sort = "companyNum", direction = Sort.Direction.ASC) Pageable pageable) {
        try {
            if (!memberService.isAdmin(userInfo.getId()))
                throw new IllegalArgumentException("어드민이 아닙니다.");

            Slice<UserEntity> members = memberService.getAllUsers(search, centerGroup, jobGroup, pageable);

            List<MembersResponseDTO> resDTO = members.stream()
                    .map(member -> MembersResponseDTO.builder()
                            .id(member.getId())
                            .name(member.getName())
                            .companyNum(member.getCompanyNum())
                            .centerGroup(member.getJobGroup().getCenterGroup().getCenterName())
                            .jobGroup(member.getJobGroup().getJobName())
                            .build())
                    .toList();

            return ResponseEntity.ok().body(Map.of("code", 200, "data", resDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "나의 정보 불러오기 오류. 잠시 후 다시 시도해주세요."));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getMemberInfo(@PathVariable Long userId) {
        try {
            final UserEntity user = memberService.getUserInfo(userId);

            if (user == null)
                throw new IllegalArgumentException("유저 정보가 없습니다.");

            final MemberInfoResponseDTO resDTO = MemberInfoResponseDTO.builder()
                    .id(user.getId())
                    .companyNum(user.getCompanyNum())
                    .name(user.getName())
                    .joinDate(user.getJoinDate())
                    .centerGroup(user.getJobGroup().getCenterGroup().getCenterName())
                    .jobGroup(user.getJobGroup().getJobName())
                    .level(user.getLevel())
                    .userId(user.getUserId())
                    .initPassword(user.getInitPassword())
                    .password(user.getPassword())
                    .build();

            return ResponseEntity.ok().body(Map.of("code", 200, "data", resDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "구성원 정보 불러오기 오류. 잠시 후 다시 시도해주세요."));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteMember(@PathVariable Long userId) {
        try {
            final UserEntity user = memberService.getUserInfo(userId);

            if (user == null)
                throw new IllegalArgumentException("유저 정보가 없습니다.");

            memberService.deleteMember(userId);

            return ResponseEntity.ok().body(Map.of("code", 200, "data", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "구성원 정보 삭제 오류. 잠시 후 다시 시도해주세요."));
        }
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateMember(@PathVariable Long userId, @RequestBody MemberUpdateRequestDTO dto) {
        try {
            final UserEntity user = memberService.getUserInfo(userId);

            if (user == null)
                throw new IllegalArgumentException("유저 정보가 없습니다.");

           memberService.updateMember(user, dto);

            return ResponseEntity.ok().body(Map.of("code", 200, "data", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "구성원 정보 수정 오류. 잠시 후 다시 시도해주세요."));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addMember(@RequestBody MemberAddRequestDTO dto) {
        try {
            memberService.addMemberAndJobGroup(dto);

            return ResponseEntity.ok().body(Map.of("code", 200, "data", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "구성원 추가 오류. 잠시 후 다시 시도해주세요."));
        }
    }
}
