package pepperstone.backend.googlesheet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pepperstone.backend.googlesheet.service.GoogleService;

import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/google")
@RequiredArgsConstructor
public class GoogleController {

    private final GoogleService googleService;

    private static final String SPREADSHEET_ID = "1knh-jdu_Zyn8dsqE7Owds6TaVlGn4XZsTQ2U6ratgFs"; // 1. 기존에 스프레스 시트id를 복사해둔곳을 여기에 저장해둔다.
    private static final String RANGE = "A1"; // 2. 작성할 행을 입력

    @PostMapping("/write")
    public ResponseEntity<String> writeToSheet(@RequestParam String word) {
        try {
            // 3. 데이터를 스프레드시트에 쓰기 위해 전달되는 형식
            // 행과 열에 데이터를 매핑하기 위함
            List<List<Object>> values = List.of(Collections.singletonList(word));
            // 4. 서비스 로직 호출
            googleService.writeToSheet(SPREADSHEET_ID, RANGE, values);
            return ResponseEntity.ok("Data written successfully to the spreadsheet: " + word);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to write data: " + e.getMessage());
        }
    }

    @GetMapping("/read")
    public ResponseEntity<List<List<Object>>> readSheet(
            @RequestParam String sheetName,
            @RequestParam(required = false, defaultValue = "A1:Z100") String range) {
        try {
            String fullRange = sheetName + "!" + range; // 시트 이름과 범위를 결합
            List<List<Object>> values = googleService.readSheet(SPREADSHEET_ID, fullRange);
            return ResponseEntity.ok(values);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }

    @GetMapping("/read-by-column")
    public ResponseEntity<List<List<Object>>> readSheetByColumn(
            @RequestParam String sheetName,
            @RequestParam(required = false, defaultValue = "A:Z") String range) {
        try {
            String fullRange = sheetName + "!" + range; // 시트 이름과 범위를 결합
            List<List<Object>> values = googleService.readSheetByColumn(SPREADSHEET_ID, fullRange);
            return ResponseEntity.ok(values);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(List.of("Failed to read data: " + e.getMessage())));
        }
    }
}