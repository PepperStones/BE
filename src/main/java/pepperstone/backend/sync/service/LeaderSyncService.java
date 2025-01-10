package pepperstone.backend.sync.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pepperstone.backend.common.entity.JobQuestProgressEntity;
import pepperstone.backend.common.entity.JobQuestsEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.Period;
import pepperstone.backend.common.repository.UserRepository;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LeaderSyncService {
    private final SyncService syncService;

    private final UserRepository userRepository;

    private static final String RANGE = "'참고. 리더부여 퀘스트'!A1:Z100";

    @Transactional
    public void sync(String spreadsheetId) {
        List<List<Object>> data = syncService.readSheet(spreadsheetId, RANGE);

        if (data == null || data.size() <= 10) {
            throw new RuntimeException("Insufficient data in the spreadsheet.");
        }

        String department = data.get(18).get(12).toString().trim(); // 소속 센터
        String jobGroup = data.get(18).get(13).toString().trim(); // 소속 그룹

        // 퀘스트 정보를 저장할 리스트
        List<Map<String, Object>> questList = new ArrayList<>();

        // 11번째 행에서 정보 읽기
        int startRow = 10;
        // 퀘스트명이 빈칸이면 종료 || "합산"이 나오면 종료
        while(!data.get(startRow).get(9).toString().isEmpty() && !data.get(startRow).get(9).toString().trim().equals("합산")) {
            List<Object> questRow = data.get(startRow);

            // 퀘스트 정보
            String questName = questRow.get(9).toString().trim(); // 퀘스트명
            String periodStr = questRow.get(10).toString().trim(); // 주기
            String weightStr = questRow.get(11).toString().replace("%", "").trim(); // 불러온 비중에서 % 제거
            int weight = Integer.parseInt(weightStr); // 비중
            int maxPoints = parseInteger(questRow, 13); // max 점수
            int medianPoints = parseInteger(questRow, 14); // medium 점수
            String maxCondition = questRow.get(15).toString().trim(); // max 조건
            String medianCondition = questRow.get(16).toString().trim(); // medium 조건
            Period period = periodStr.equals("주") ? Period.WEEKLY : Period.MONTHLY;

            // 퀘스트 정보를 맵으로 저장
            Map<String, Object> questInfo = new HashMap<>();
            questInfo.put("questName", questName);
            questInfo.put("period", period);
            questInfo.put("weight", weight);
            questInfo.put("maxPoints", maxPoints);
            questInfo.put("medianPoints", medianPoints);
            questInfo.put("maxCondition", maxCondition);
            questInfo.put("medianCondition", medianCondition);

            // 리스트에 퀘스트 정보 추가
            questList.add(questInfo);

            startRow++;
        }


    }

    private int parseInteger(List<Object> row, int index) {
        try {
            return row.size() > index && !row.get(index).toString().isEmpty() ? Integer.parseInt(row.get(index).toString().trim()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDouble(List<Object> row, int index) {
        try {
            return row.size() > index && !row.get(index).toString().isEmpty() ? Double.parseDouble(row.get(index).toString().trim()) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
