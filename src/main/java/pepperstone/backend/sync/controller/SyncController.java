package pepperstone.backend.sync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pepperstone.backend.sync.service.*;

import java.util.*;

@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SyncController {

    private final JobSyncService jobSyncService;
    private final LeaderSyncService leaderSyncService;
    private final ProjectSyncService projectSyncService;
    private final EvaluationSyncService evaluationSyncService;
    private static final String SPREADSHEET_ID = "1knh-jdu_Zyn8dsqE7Owds6TaVlGn4XZsTQ2U6ratgFs"; // 스프레스 시트id를 복사해둔곳을 여기에 저장

    @GetMapping("/googlesheet")
    public ResponseEntity<Map<String, Object>> readSheet(@RequestParam String type) {
        Map<String, Object> response = new HashMap<>();
        try {
            String message;
            switch (type) {
                case "all" -> {
                    jobSyncService.sync(SPREADSHEET_ID);
                    leaderSyncService.sync(SPREADSHEET_ID);
                    //projectSyncService.sync(SPREADSHEET_ID);
                    //evaluationSyncService.sync(SPREADSHEET_ID);
                    message = "전체 동기화가 완료되었습니다.";
                }
                case "job" -> {
                    jobSyncService.sync(SPREADSHEET_ID);
                    message = "직무별 퀘스트 동기화가 완료되었습니다.";
                }
                case "leader" -> {
                    leaderSyncService.sync(SPREADSHEET_ID);
                    message = "리더부여 퀘스트 동기화가 완료되었습니다.";
                }
                case "project" -> {
                    //projectSyncService.sync(SPREADSHEET_ID);
                    message = "전사 프로젝트 동기화가 완료되었습니다.";
                }
                case "evaluation" -> {
                    //evaluationSyncService.sync(SPREADSHEET_ID);
                    message = "인사 평가 동기화가 완료되었습니다.";
                }
                default -> throw new IllegalArgumentException("Invalid sync type: " + type);
            }

            // 성공 응답 형식 반환
            response.put("code", 200);
            response.put("message", message);
            response.put("data", type);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // 잘못된 요청에 대한 처리
            response.put("code", 400);
            response.put("message", e.getMessage());
            response.put("data", type);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "동기화 실패: " + e.getMessage());
            response.put("data", type);

            return ResponseEntity.internalServerError().body(response);
        }
    }
}
