package pepperstone.backend.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pepperstone.backend.common.entity.*;
import pepperstone.backend.common.entity.enums.Period;
import pepperstone.backend.common.repository.LeaderQuestProgressRepository;
import pepperstone.backend.common.repository.LeaderQuestRepository;
import pepperstone.backend.common.repository.UserRepository;
import pepperstone.backend.notification.service.FcmService;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderSyncService {
    private final SyncService syncService;
    private final FcmService fcmService;
    private final LeaderQuestRepository leaderQuestRepository;
    private final LeaderQuestProgressRepository leaderQuestProgressRepository;
    private final UserRepository userRepository;

    private static final String RANGE = "'참고. 리더부여 퀘스트'!A1:Z100";

    @Transactional
    public void sync(String spreadsheetId) {
        List<List<Object>> data = syncService.readSheet(spreadsheetId, RANGE);

        if (data == null || data.size() <= 10) {
            throw new RuntimeException("Insufficient data in the spreadsheet.");
        }

        // 사원 정보 및 퀘스트 정보 가져오기
        List<Map<String, Object>> peopleList = peopleList(data);
        List<Map<String, Object>> questList = questList(data);
        List<Map<String, Object>> questProgressList = questProgressList(data);

        // 퀘스트 동기화 시작
        for (Map<String, Object> person : peopleList) {
            String companyNum = person.get("companyNum").toString();
            String name = person.get("name").toString();

            // users 테이블에서 해당 사원 조회
            UserEntity user = userRepository.findByCompanyNumAndName(companyNum, name)
                    .orElseThrow(() -> new RuntimeException("User not found: " + companyNum + ", " + name));

            // 각 퀘스트에 대해 동기화 처리
            for (Map<String, Object> questInfo : questList) {
                String questName = questInfo.get("questName").toString();
                Period period = (Period) questInfo.get("period");

                // LeaderQuestsEntity 조회 또는 생성
                LeaderQuestsEntity leaderQuest = leaderQuestRepository.findByDepartmentAndJobGroupAndQuestName(
                                user.getJobGroup().getCenterGroup().getCenterName(),
                                user.getJobGroup().getJobName(),
                                questName)
                        .orElseGet(() -> {
                            // 새로운 리더 퀘스트 생성
                            LeaderQuestsEntity newQuest = new LeaderQuestsEntity();
                            newQuest.setDepartment(user.getJobGroup().getCenterGroup().getCenterName());
                            newQuest.setJobGroup(user.getJobGroup().getJobName());
                            newQuest.setQuestName(questName);
                            newQuest.setPeriod(period);
                            newQuest.setMaxPoints((Integer) questInfo.get("maxPoints"));
                            newQuest.setMedianPoints((Integer) questInfo.get("medianPoints"));
                            newQuest.setWeight((Integer) questInfo.get("weight"));
                            newQuest.setMaxCondition(questInfo.get("maxCondition").toString());
                            newQuest.setMedianCondition(questInfo.get("medianCondition").toString());
                            return leaderQuestRepository.save(newQuest);
                        });

                // 해당 사원의 퀘스트 달성 정보 필터링
                List<Map<String, Object>> personAchievements = questProgressList.stream()
                        .filter(quest -> quest.get("companyNum").equals(companyNum) && quest.get("questName").equals(questName))
                        .toList();

                // 각 달성 정보에 대해 진행 상황 추가 또는 업데이트
                for (Map<String, Object> achievement : personAchievements) {
                    int monthOrWeek = (int) achievement.get("monthOrWeek");
                    String achievementType = achievement.get("achievementType").toString();
                    int experience = (int) achievement.get("experience");

                    // 주기별 처리: 주별 또는 월별로 구분하여 진행 상황 업데이트
                    if (period == Period.WEEKLY) {
                        addOrUpdateProgress(user, leaderQuest, monthOrWeek, experience, achievementType, true);
                    } else {
                        addOrUpdateProgress(user, leaderQuest, monthOrWeek, experience, achievementType, false);
                    }
                }
            }
        }
    }

    private void addOrUpdateProgress(UserEntity user, LeaderQuestsEntity leaderQuest, int weekOrMonth,
                                     int experience, String achievementType, boolean isWeekly) {

        // 주기별 진행 상황 조회
        Optional<LeaderQuestProgressEntity> existingProgress = isWeekly
                ? leaderQuestProgressRepository.findByLeaderQuestsAndUsersAndWeek(leaderQuest, user, weekOrMonth)
                : leaderQuestProgressRepository.findByLeaderQuestsAndUsersAndMonth(leaderQuest, user, weekOrMonth);

        if (existingProgress.isEmpty()) {
            // 누적 경험치 계산: 가장 최신 week 또는 month 기준으로 진행 정보 조회
            int accumulatedExperience = isWeekly
                    ? leaderQuestProgressRepository.findTopByLeaderQuestsAndUsersOrderByWeekDesc(leaderQuest, user)
                    .map(progress -> progress.getAccumulatedExperience() + progress.getExperience())
                    .orElse(0)
                    : leaderQuestProgressRepository.findTopByLeaderQuestsAndUsersOrderByMonthDesc(leaderQuest, user)
                    .map(progress -> progress.getAccumulatedExperience() + progress.getExperience())
                    .orElse(0);

            // 새로운 LeaderQuestProgressEntity 생성 및 저장
            LeaderQuestProgressEntity newProgress = new LeaderQuestProgressEntity();
            newProgress.setLeaderQuests(leaderQuest);
            newProgress.setUsers(user);
            newProgress.setAchievement(achievementType);
            newProgress.setExperience(experience);
            newProgress.setAccumulatedExperience(accumulatedExperience);
            newProgress.setCreatedAt(LocalDate.now());

            if (isWeekly) {
                newProgress.setWeek(weekOrMonth);
                newProgress.setMonth(null);
            } else {
                newProgress.setMonth(weekOrMonth);
                newProgress.setWeek(null);
            }

            leaderQuestProgressRepository.save(newProgress);

            // 푸시 알림 전송
            int result = fcmService.sendExperienceNotification(user, experience);
            if (result == 1) {
                log.info("리더 퀘스트 푸시 알림 전송 성공: 사용자 ID={}, 경험치={}do", user.getId(), experience);
            } else {
                log.error("리더 퀘스트 푸시 알림 전송 실패: 사용자 ID={}, 경험치={}do", user.getId(), experience);
            }
        }
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
        // 사원 정보를 저장할 리스트
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
