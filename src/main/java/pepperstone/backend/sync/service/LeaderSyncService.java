package pepperstone.backend.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pepperstone.backend.common.entity.*;
import pepperstone.backend.common.entity.enums.ChallengeType;
import pepperstone.backend.common.entity.enums.Period;
import pepperstone.backend.common.repository.*;
import pepperstone.backend.notification.service.FcmService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderSyncService {
    private final SyncService syncService;
    private final FcmService fcmService;
    private final LeaderQuestRepository leaderQuestRepository;
    private final LeaderQuestProgressRepository leaderQuestProgressRepository;
    private final UserRepository userRepository;
    private final ChallengeProgressRepository challengeProgressRepository;
    private final ChallengesRepository challengesRepository;

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

        // 유저별 리더 부여 퀘스트에서 MAX에 도달한 횟수를 저장
        //Map<UserEntity, Integer> maxCountMap = new HashMap<>();
        Map<UserEntity, Integer> newMaxCountMap = new HashMap<>();

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

//                // LeaderQuestsEntity 조회 또는 생성
//                LeaderQuestsEntity leaderQuest = leaderQuestRepository.findByDepartmentAndJobGroupAndQuestName(
//                                user.getJobGroup().getCenterGroup().getCenterName(),
//                                user.getJobGroup().getJobName(),
//                                questName)
//                        .orElseGet(() -> {
//                            // 새로운 리더 퀘스트 생성
//                            LeaderQuestsEntity newQuest = new LeaderQuestsEntity();
//                            newQuest.setDepartment(user.getJobGroup().getCenterGroup().getCenterName());
//                            newQuest.setJobGroup(user.getJobGroup().getJobName());
//                            newQuest.setQuestName(questName);
//                            newQuest.setPeriod(period);
//                            newQuest.setMaxPoints((Integer) questInfo.get("maxPoints"));
//                            newQuest.setMedianPoints((Integer) questInfo.get("medianPoints"));
//                            newQuest.setWeight((Integer) questInfo.get("weight"));
//                            newQuest.setMaxCondition(questInfo.get("maxCondition").toString());
//                            newQuest.setMedianCondition(questInfo.get("medianCondition").toString());
//                            return leaderQuestRepository.save(newQuest);
//                        });

                // LeaderQuestsEntity 조회 또는 생성
                String department = user.getJobGroup().getCenterGroup().getCenterName();
                String jobGroup = user.getJobGroup().getJobName();

                // 변수화
                int maxPoints = (Integer) questInfo.get("maxPoints");
                int medianPoints = (Integer) questInfo.get("medianPoints");
                int weight = (Integer) questInfo.get("weight");
                String maxCondition = questInfo.get("maxCondition").toString();
                String medianCondition = questInfo.get("medianCondition").toString();

                LeaderQuestsEntity leaderQuest = leaderQuestRepository.findByDepartmentAndJobGroupAndQuestName(
                                department,
                                jobGroup,
                                questName)
                        .orElseGet(() -> {
                            // 새로운 리더 퀘스트 생성
                            LeaderQuestsEntity newQuest = new LeaderQuestsEntity();
                            newQuest.setDepartment(department);
                            newQuest.setJobGroup(jobGroup);
                            newQuest.setQuestName(questName);
                            newQuest.setPeriod(period);
                            newQuest.setMaxPoints(maxPoints);
                            newQuest.setMedianPoints(medianPoints);
                            newQuest.setWeight(weight);
                            newQuest.setMaxCondition(maxCondition);
                            newQuest.setMedianCondition(medianCondition);
                            return leaderQuestRepository.save(newQuest);
                        });

                // 해당 사원의 퀘스트 달성 정보 필터링
                List<Map<String, Object>> personAchievements = questProgressList.stream()
                        .filter(quest -> quest.get("companyNum").equals(companyNum) && quest.get("questName").equals(questName))
                        .toList();

                // 이미 진행된 주(Week) 또는 월(Month) 정보를 조회하여 Set으로 수집
                Set<Integer> existingWeeksOrMonths = leaderQuestProgressRepository.findByLeaderQuestsAndUsers(leaderQuest, user)
                        .stream()
                        .map(progress -> period == Period.WEEKLY ? progress.getWeek() : progress.getMonth()) // 주간이면 주(Week), 월간이면 월(Month)을 가져옴
                        .collect(Collectors.toSet());

                // 사용자별 업적을 순회하며 진행 상황을 업데이트
                for (Map<String, Object> achievement : personAchievements) {
                    int monthOrWeek = (int) achievement.get("monthOrWeek"); // 주 또는 월 정보 추출
                    String achievementType = achievement.get("achievementType").toString();// 업적 유형(예: "Max")을 문자열로 추출
                    int experience = (int) achievement.get("experience");

                    // 업적 유형이 "Max"인지 대소문자를 무시하고 비교하여 확인
                    boolean isMaxAchieved = "Max".equalsIgnoreCase(achievementType);

                    // 해당 주 또는 월에 진행된 기록이 없는 경우에만 진행 상황 추가 또는 업데이트
                    if (!existingWeeksOrMonths.contains(monthOrWeek)) {
                        // 새로운 진행 상황을 추가하거나 기존 진행 상황을 업데이트
                        addOrUpdateProgress(user, leaderQuest, monthOrWeek, experience, achievementType, period == Period.WEEKLY, maxPoints, medianPoints);

                        // 업적 유형이 "Max"인 경우 새로 달성된 Max 업적 횟수를 증가
                        if (isMaxAchieved) {
                            newMaxCountMap.put(user, newMaxCountMap.getOrDefault(user, 0) + 1);
                        }
                    }
                }
            }
        }
        // 업적 유형이 "Max"인 것들로 도전과제 체크 및 업데이트
        newMaxCountMap.forEach((user, count) -> {
            if (count > 0) {
                checkAndUpdateChallenge(user, count);
            }
        });
    }

    // ============== private method ================

    private void addOrUpdateProgress(UserEntity user, LeaderQuestsEntity leaderQuest, int weekOrMonth,
                                     int experience, String achievementType, boolean isWeekly, int maxScore, int medianScore) {

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
        fcmService.sendExperienceNotification(user, experience, maxScore, medianScore,"leader");
    }

    private void checkAndUpdateChallenge(UserEntity user, int count) {
        // 'LEADER_QUEST_MAX' 유형의 도전 과제를 조회
        // 도전 과제가 없으면 새로 생성 후 저장
        ChallengesEntity challenge = challengesRepository.findByType(ChallengeType.LEADER_QUEST_MAX)
                .orElseGet(() -> {
                    // 새로운 도전 과제 엔티티 생성
                    ChallengesEntity newChallenge = ChallengesEntity.builder()
                            .name("리더부여 퀘스트 MAX 기준 10회 달성") // 도전 과제 이름 설정
                            .description("리더부여 퀘스트에서 MAX 기준을 10회 달성해보세요!") // 도전 과제 설명 설정
                            .requiredCount(10) // 도전 과제 완료에 필요한 횟수 설정
                            .type(ChallengeType.LEADER_QUEST_MAX) // 도전 과제 유형 설정
                            .build();
                    // 새로 생성한 도전 과제를 저장하고 반환
                    return challengesRepository.save(newChallenge);
                });

        // 해당 유저와 도전 과제에 대한 진행 상황을 조회
        // 진행 상황이 없으면 새로 생성 후 저장
        ChallengeProgressEntity progress = challengeProgressRepository.findByUsersAndChallenges(user, challenge)
                .orElseGet(() -> {
                    // 새로운 도전 과제 진행 상황 엔티티 생성
                    ChallengeProgressEntity newProgress = new ChallengeProgressEntity();
                    newProgress.setUsers(user);
                    newProgress.setChallenges(challenge);
                    newProgress.setCurrentCount(0);
                    newProgress.setCompleted(false);
                    newProgress.setReceive(false);
                    return challengeProgressRepository.save(newProgress);
                });

        // 도전 과제가 이미 완료된 경우 메서드를 종료
        if (progress.getCompleted()) {
            return;
        }

        // 현재 진행 횟수에 새로운 횟수를 더함
        progress.setCurrentCount(progress.getCurrentCount() + count);

        // 누적된 진행 횟수가 도전 과제 완료 기준에 도달했을 때
        if (progress.getCurrentCount() >= challenge.getRequiredCount()) {
            progress.setCompleted(true); // 완료 상태로 변경

            // FCM 푸시 알림 전송
            String title = "도전과제 달성!";
            String body = "도전과제를 달성하셨습니다! 더 자세한 내용은 홈 탭 > 도전과제에서 확인해보세요.";
            fcmService.sendPushChallenge(user, title, body);
        }

        challengeProgressRepository.save(progress);
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
