package pepperstone.backend.sync.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class JobSyncService {
    private final SyncService syncService;

    public List<Map<String, Object>> sync(String spreadsheetId) {
        // 결과 데이터를 저장할 리스트 초기화
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // Google Sheet에서 데이터를 읽어옴
            List<List<Object>> data = syncService.readSheet(spreadsheetId, "'참고. 직무별 퀘스트'!A1:Z100");

            // 데이터가 비어 있거나 null이면 빈 리스트 반환
            if (data == null || data.isEmpty()) {
                return Collections.emptyList();
            }

            // Max, Medium 점수, 소속 센터, 직무 그룹, 주기 체크
            List<Object> row11 = data.get(10);
            System.out.println(row11);
            Map<String, Object> row11map = new HashMap<>();
            row11map.put("MaxScore",row11.get(1).toString().trim()); // Max 점수
            row11map.put("MediumScore",row11.get(2).toString().trim()); // Medium 점수
            row11map.put("CenterName", row11.get(5).toString().trim()); // 소속 센터
            row11map.put("jobName", row11.get(6).toString().trim()); // 직무 그룹
            // 주기 체크 [WEEKLY, MONTHLY]
            if (row11.get(7).toString().trim().equals("주")){
                row11map.put("period", "WEEKLY");
            } else if (row11.get(7).toString().trim().equals("월")) {
                row11map.put("period", "MONTHLY");
            }

            result.add(row11map);

            // 결과 리스트 반환
            return result;
        } catch (Exception e) {
            // 예외 발생 시 런타임 예외로 던지기
            throw new RuntimeException("Error during job quest sync: " + e.getMessage(), e);
        }
    }
}
