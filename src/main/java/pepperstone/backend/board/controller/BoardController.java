package pepperstone.backend.board.controller;

import io.micrometer.common.util.StringUtils;
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
import org.springframework.web.bind.annotation.*;
import pepperstone.backend.board.dto.request.BoardRequestDTO;
import pepperstone.backend.board.dto.request.BoardUpdateRequestDTO;
import pepperstone.backend.board.dto.response.BoardListResponseDTO;
import pepperstone.backend.board.dto.response.BoardResponseDTO;
import pepperstone.backend.board.service.BoardService;
import pepperstone.backend.common.entity.BoardsEntity;
import pepperstone.backend.common.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

            boardService.addOrUpdateBoard(board);

            // 게시글 등록 후 알림 전송 로직 호출
            boardService.sendNewBoardNotification(board);

            return ResponseEntity.ok().body(Map.of("code", 200, "data", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "게시글 작성 오류. 잠시 후 다시 시도해주세요."));
        }
    }

    @GetMapping("/admin/list")
    @PageableAsQueryParam
    public ResponseEntity<Map<String, Object>> adminBoardsList(@AuthenticationPrincipal UserEntity userInfo,
                                                               @Parameter(hidden = true)
                                                               @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            if (!boardService.isAdmin(userInfo.getId()))
                throw new IllegalArgumentException("어드민이 아닙니다.");

            Slice<BoardsEntity> boards = boardService.getAllBoards(pageable);

            List<BoardListResponseDTO> resDTO = boards.stream()
                    .map(board -> BoardListResponseDTO.builder()
                            .id(board.getId())
                            .centerGroup(board.getCenterGroup())
                            .jobGroup(board.getJobGroup())
                            .title(board.getTitle())
                            .createdAt(board.getCreatedAt())
                            .updatedAt(board.getUpdatedAt())
                            .build())
                    .toList();

            return ResponseEntity.ok().body(Map.of("code", 200, "data", resDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "게시글 불러오기 오류. 잠시 후 다시 시도해주세요."));
        }
    }

    @GetMapping("/admin/{boardId}")
    public ResponseEntity<Map<String, Object>> getAdminBoard(@AuthenticationPrincipal UserEntity userInfo, @PathVariable("boardId") Long boardId) {
        try {
            if (!boardService.isAdmin(userInfo.getId()))
                throw new IllegalArgumentException("어드민이 아닙니다.");

            final BoardsEntity board = boardService.getBoard(boardId);

            final BoardResponseDTO resDTO = BoardResponseDTO.builder()
                    .id(board.getId())
                    .centerGroup(board.getCenterGroup())
                    .jobGroup(board.getJobGroup())
                    .title(board.getTitle())
                    .createdAt(board.getCreatedAt())
                    .updatedAt(board.getUpdatedAt())
                    .content(board.getContent())
                    .build();

            return ResponseEntity.ok().body(Map.of("code", 200, "data", resDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "게시글 세부 내용 불러오기 오류. 잠시 후 다시 시도해주세요."));
        }
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Map<String, Object>> deleteBoard(@AuthenticationPrincipal UserEntity userInfo, @PathVariable("boardId") Long boardId) {
        try {
            if (!boardService.isAdmin(userInfo.getId()))
                throw new IllegalArgumentException("어드민이 아닙니다.");

            boardService.deleteBoard(boardId);

            return ResponseEntity.ok().body(Map.of("code", 200, "data", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "게시글 삭제하기 오류. 잠시 후 다시 시도해주세요."));
        }
    }

    @PatchMapping("/{boardId}")
    public ResponseEntity<Map<String, Object>> updateBoard(@AuthenticationPrincipal UserEntity userInfo,
                                                           @PathVariable("boardId") Long boardId,
                                                           @RequestBody BoardUpdateRequestDTO dto) {
        try {
            if (!boardService.isAdmin(userInfo.getId()))
                throw new IllegalArgumentException("어드민이 아닙니다.");

            final BoardsEntity board = boardService.getBoard(boardId);

            if (StringUtils.isNotBlank(dto.getCenterGroup()))
                board.setCenterGroup(dto.getCenterGroup());

            if (StringUtils.isNotBlank(dto.getJobGroup()))
                board.setJobGroup(dto.getJobGroup());

            if (StringUtils.isNotBlank(dto.getTitle()))
                board.setTitle(dto.getTitle());

            if (StringUtils.isNotBlank(dto.getContent()))
                board.setContent(dto.getContent());

            board.setUpdatedAt(LocalDateTime.now());

            boardService.addOrUpdateBoard(board);

            return ResponseEntity.ok().body(Map.of("code", 200, "data", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "게시글 수정하기 오류. 잠시 후 다시 시도해주세요."));
        }
    }

    @GetMapping("/list")
    @PageableAsQueryParam
    public ResponseEntity<Map<String, Object>> boardsList(@AuthenticationPrincipal UserEntity userInfo,
                                                          @Parameter(hidden = true)
                                                          @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            final UserEntity user = boardService.getUserInfo(userInfo.getId());

            if (user == null)
                throw new IllegalArgumentException("유저 정보가 없습니다.");

            Slice<BoardsEntity> boards = boardService
                    .getFilterBoards(user.getJobGroup().getCenterGroup().getCenterName(),
                    user.getJobGroup().getJobName(),
                    pageable);

            List<BoardListResponseDTO> resDTO = boards.stream()
                    .map(board -> BoardListResponseDTO.builder()
                            .id(board.getId())
                            .centerGroup(board.getCenterGroup())
                            .jobGroup(board.getJobGroup())
                            .title(board.getTitle())
                            .createdAt(board.getCreatedAt())
                            .updatedAt(board.getUpdatedAt())
                            .build())
                    .toList();

            return ResponseEntity.ok().body(Map.of("code", 200, "data", resDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "게시글 불러오기 오류. 잠시 후 다시 시도해주세요."));
        }
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<Map<String, Object>> getBoard(@AuthenticationPrincipal UserEntity userInfo, @PathVariable("boardId") Long boardId) {
        try {
            final UserEntity user = boardService.getUserInfo(userInfo.getId());

            if (user == null)
                throw new IllegalArgumentException("유저 정보가 없습니다.");

            final BoardsEntity board = boardService.getBoardUser(boardId, user);

            // 게시판 조회 완료 후, 도전과제 체크 후, 달성시 푸시 알림 로직 추가
            boardService.checkAndUpdateChallenge(user, board);

            final BoardResponseDTO resDTO = BoardResponseDTO.builder()
                    .id(board.getId())
                    .centerGroup(board.getCenterGroup())
                    .jobGroup(board.getJobGroup())
                    .title(board.getTitle())
                    .createdAt(board.getCreatedAt())
                    .updatedAt(board.getUpdatedAt())
                    .content(board.getContent())
                    .build();

            return ResponseEntity.ok().body(Map.of("code", 200, "data", resDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "게시글 불러오기 오류. 잠시 후 다시 시도해주세요."));
        }
    }
}
