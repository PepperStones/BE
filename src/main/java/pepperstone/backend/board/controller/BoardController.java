package pepperstone.backend.board.controller;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pepperstone.backend.board.BoardRequestDTO;
import pepperstone.backend.board.service.BoardService;
import pepperstone.backend.common.entity.BoardsEntity;
import pepperstone.backend.common.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> writeBoard(@AuthenticationPrincipal UserEntity userInfo, @RequestBody BoardRequestDTO dto) {
        try {
            if (!boardService.isAdmin(userInfo.getId()))
                throw new IllegalArgumentException("어드민이 아닙니다.");

            if (StringUtils.isEmpty(dto.getTitle()) || StringUtils.isEmpty(dto.getContent()))
                throw new IllegalArgumentException("제목과 내용을 작성하십시오.");

            final BoardsEntity board = new BoardsEntity();

            board.setTitle(dto.getTitle());
            board.setContent(dto.getContent());
            board.setCreatedAt(LocalDateTime.now());
            board.setUpdatedAt(LocalDateTime.now());
            board.setUsers(userInfo);

            if (StringUtils.isNotBlank(dto.getCenterGroup())) {
                if (!boardService.checkCenterGroup(dto.getCenterGroup()))
                    throw new IllegalArgumentException("존재하지 않는 센터입니다.");
                board.setCenterGroup(dto.getCenterGroup());
            }

            if (StringUtils.isNotBlank(dto.getJobGroup()))
                board.setJobGroup(dto.getJobGroup());

            boardService.addBoard(board);

            return ResponseEntity.ok().body(Map.of("code", 200, "data", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "구성원 정보 불러오기 오류. 잠시 후 다시 시도해주세요."));
        }
    }
}
