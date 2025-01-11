package pepperstone.backend.member.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.member.dto.MembersResponseDTO;
import pepperstone.backend.member.service.MemberService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
}
