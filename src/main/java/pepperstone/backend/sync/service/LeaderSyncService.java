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

        // 소속 인원 정보
        // 퀘스트 정보를 저장할 리스트
        List<Map<String, Object>> peopleList = new ArrayList<>();
        peopleList = peopleList(data);

        // 퀘스트 정보를 저장할 리스트
        List<Map<String, Object>> questList = new ArrayList<>();
        questList = questList(data);

        // 퀘스트 달성 정보 저장 리스트
        List<Map<String, Object>> questProgressList = new ArrayList<>();
        questProgressList = questProgressList(data);



/*        // 퀘스트 달성 정보 저장
        for (Map<String, Object> person : peopleList) {
            String personCompanyNum = person.get("companyNum").toString();

            // 해당 사원의 퀘스트 달성 정보 필터링
            List<Map<String, Object>> personAchievements = questProgressList.stream()
                    .filter(quest -> quest.get("companyNum").equals(personCompanyNum))
                    .toList();

            if (personAchievements.isEmpty()) continue;

            // 출력: 해당 사원에 대한 퀘스트 달성 정보
            System.out.println("사원 정보:");
            System.out.println("사번: " + personCompanyNum + ", 이름: " + person.get("name"));

            for (Map<String, Object> achievement : personAchievements) {
                String monthOrWeek = achievement.get("monthOrWeek").toString();
                String questName = achievement.get("questName").toString();
                String achievementType = achievement.get("achievementType").toString();
                int experience = (int) achievement.get("experience");

                // 출력: 퀘스트 달성 정보
                System.out.println("  월/주: " + monthOrWeek);
                System.out.println("  퀘스트명: " + questName);
                System.out.println("  달성 유형: " + achievementType);
                System.out.println("  부여 경험치: " + experience);
                System.out.println("---------------------------");
            }
        }*/

    }

    private int parseInteger(List<Object> row, int index) {
        try {
            return row.size() > index && !row.get(index).toString().isEmpty() ? Integer.parseInt(row.get(index).toString().trim()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // 사원 정보를 저장한 리스트 반환 메서드
    private List<Map<String, Object>> peopleList(List<List<Object>> data){
        // 퀘스트 정보를 저장할 리스트
        List<Map<String, Object>> peopleList = new ArrayList<>();

        // 19번째 행에서 정보 읽기
        int startRow = 18;
        // 사번이 빈칸이면 종료
        while(startRow < data.size() && data.get(startRow).size() > 9 && !data.get(startRow).get(9).toString().isEmpty()) {
            List<Object> personRow = data.get(startRow);

            // 사원 정보
            String companyNum = personRow.get(9).toString().trim(); // 사번
            String name = personRow.get(10).toString().trim(); // 이름

            // 사원 정보를 맵으로 저장
            Map<String, Object> peopleInfo = new HashMap<>();
            peopleInfo.put("companyNum", companyNum);
            peopleInfo.put("name", name);

            // 리스트에 사원 정보 추가
            peopleList.add(peopleInfo);

            startRow++;
        }

        return peopleList;
    }

    // 퀘스트 정보를 저장한 리스트 반환 메서드
    private List<Map<String, Object>> questList(List<List<Object>> data){
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

        return questList;
    }

    // 퀘스트 달성 정보 저장 리스트 반환 메서드
    private List<Map<String, Object>> questProgressList(List<List<Object>> data){
        // 퀘스트 달성 정보 저장 리스트
        List<Map<String, Object>> questProgressList = new ArrayList<>();

        // 10번째 행부터 시작
        int startRow = 9;
        while (startRow < data.size() && data.get(startRow).size() > 5) {
            List<Object> questRow = data.get(startRow);

            // 필요한 정보 추출
            int monthOrWeek = parseInteger(questRow, 1); // 월 또는 주 정보
            String companyNum = questRow.get(2).toString().trim(); // 사번
            String name = questRow.get(3).toString().trim(); // 이름
            String questName = questRow.get(4).toString().trim(); // 퀘스트명
            String achievementType = questRow.get(5).toString().trim(); // 달성내용 (Max, Median 등)
            int experience = parseInteger(questRow, 6); // 부여 경험치

            // 정보 저장
            Map<String, Object> questInfo = new HashMap<>();
            questInfo.put("companyNum", companyNum);
            questInfo.put("name", name);
            questInfo.put("questName", questName);
            questInfo.put("achievementType", achievementType);
            questInfo.put("experience", experience);
            questInfo.put("monthOrWeek", monthOrWeek);

            // 리스트에 추가
            questProgressList.add(questInfo);

            startRow++;
        }
        return questProgressList;
    }
}
