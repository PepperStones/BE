// JobSyncService.java
package pepperstone.backend.sync.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pepperstone.backend.common.entity.*;
import pepperstone.backend.common.entity.enums.Period;
import pepperstone.backend.common.repository.*;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JobSyncService {

    private final SyncService syncService;
    private final JobQuestRepository jobQuestRepository;
    private final JobQuestProgressRepository jobQuestProgressRepository;
    private final UserRepository userRepository;

    private static final String RANGE = "'참고. 직무별 퀘스트'!A1:Z100";

    @Transactional
    public void sync(String spreadsheetId) {
        List<List<Object>> data = syncService.readSheet(spreadsheetId, RANGE);

        if (data == null || data.size() <= 10) {
            throw new RuntimeException("Insufficient data in the spreadsheet.");
        }

        // 11번째 행에서 정보 읽기
        List<Object> row11 = data.get(10);
        String department = row11.get(5).toString().trim(); // 소속 센터
        String jobGroup = row11.get(6).toString().trim(); // 소속 그룹
        String periodStr = row11.get(7).toString().trim(); // 주기
        int maxScore = parseInteger(row11, 1); // max 점수
        int mediumScore = parseInteger(row11, 2); // medium 점수
        double maxStandard = parseDouble(data.get(13), 5); // max 기준
        double mediumStandard = parseDouble(data.get(13), 6); // medium 기준
        Period period = periodStr.equals("주") ? Period.WEEKLY : Period.MONTHLY;

        // JobQuestsEntity 동기화 -> 조회 후, 없다면 생성o / 있다면 생성x
        JobQuestsEntity jobQuest = jobQuestRepository.findByDepartmentAndJobGroup(department, jobGroup)
                .orElseGet(() -> {
                    JobQuestsEntity newJobQuest = new JobQuestsEntity();
                    newJobQuest.setDepartment(department);
                    newJobQuest.setJobGroup(jobGroup);
                    newJobQuest.setPeriod(period);
                    newJobQuest.setMaxScore(maxScore);
                    newJobQuest.setMediumScore(mediumScore);
                    newJobQuest.setMaxStandard(maxStandard);
                    newJobQuest.setMediumStandard(mediumStandard);
                    return jobQuestRepository.save(newJobQuest);
                });

        // 해당 직무 그룹과 센터 그룹에 속한 유저 조회
        List<UserEntity> users = userRepository.findByJobGroupJobNameAndJobGroupCenterGroupCenterName(jobGroup, department);

        for (UserEntity user : users) {
            // jobQuest와 user로 jobQuestProgress를 조회
            List<JobQuestProgressEntity> progressList = jobQuestProgressRepository.findByJobQuestAndUsers(jobQuest, user);
            // week값이 가장 높은 jobQuestProgress를 조회
            JobQuestProgressEntity latestProgress = progressList.stream()
                    .max(Comparator.comparingInt(JobQuestProgressEntity::getWeek))
                    .orElse(null);

            int currentWeek;
            int accumulatedExperience;
            if (latestProgress == null) {
                // 진행 상태가 없는 경우, 1주차로 새로 생성
                currentWeek = 1;
                accumulatedExperience = 0;
            } else {
                // 가장 최근 진행 상태의 week에 +1
                currentWeek = latestProgress.getWeek() + 1;
                accumulatedExperience = latestProgress.getAccumulatedExperience() + latestProgress.getExperience();
            }

            List<Object> rowExp = data.get(12 + currentWeek);
            // 12 + currentWeek 행의 경험치 열이 비어있는 경우 예외처리
            if (rowExp == null || rowExp.get(2) == null || rowExp.get(2).toString().isEmpty()) {
                throw new RuntimeException("해당 주차에 부여된 경험치가 없습니다. 주차: " + currentWeek);
            }

            int experience = parseInteger(rowExp, 2); // 해당 주차에 맞는 부여 경험치
            double productivity = parseDouble(rowExp, 8); // 해당 주차에 맞는 생산성

            JobQuestProgressEntity newProgress = new JobQuestProgressEntity();
            newProgress.setJobQuest(jobQuest);
            newProgress.setUsers(user);
            newProgress.setWeek(currentWeek);
            newProgress.setCreatedAt(LocalDate.now());
            newProgress.setProductivity(productivity);
            newProgress.setExperience(experience);
            newProgress.setAccumulatedExperience(accumulatedExperience);
            jobQuestProgressRepository.save(newProgress);
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
