package pepperstone.backend.sync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pepperstone.backend.sync.service.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<Map<String, Object>>> readSheet(@RequestParam String type) {
        try {
            List<Map<String, Object>> values = new ArrayList<>();
            switch (type) {
                case "all" -> {
                    values = jobSyncService.sync(SPREADSHEET_ID);
                }
                case "job" -> values = jobSyncService.sync(SPREADSHEET_ID);
                case "leader" -> {
                }
                case "project" -> {
                }
                case "evaluation" -> {
                }
            }
            return ResponseEntity.ok(values);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }
}
