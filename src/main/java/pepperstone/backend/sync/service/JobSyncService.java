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

        List<Object> row = data.get(10);
        String department = row.get(5).toString().trim();
        String jobGroup = row.get(6).toString().trim();
        String periodStr = row.get(7).toString().trim();
        int maxScore = parseInteger(row, 1);
        int mediumScore = parseInteger(row, 2);
        double maxStandard = parseDouble(data.get(13), 5);
        double mediumStandard = parseDouble(data.get(13), 6);
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

/*        for (UserEntity user : users) {
            System.out.println("User ID: " + user.getId() + ", Name: " + user.getName());
        }*/
/*        for (UserEntity user : users) {
            Optional<JobQuestProgressEntity> existingProgress = jobQuestProgressRepository.findByJobQuestId(jobQuest.getId())
                    .stream()
                    .filter(progress -> progress.getUsers().getId().equals(user.getId()) && progress.getWeek() == getCurrentWeek())
                    .findFirst();

            if (existingProgress.isEmpty()) {
                // 새로운 JobQuestProgress 생성
                JobQuestProgressEntity progress = new JobQuestProgressEntity();
                progress.setJobQuest(jobQuest);
                progress.setUsers(user);
                progress.setWeek(getCurrentWeek());
                progress.setCreatedAt(LocalDate.now());
                progress.setProductivity(0.0);
                progress.setExperience(0);
                progress.setAccumulatedExperience(user.getAccumulatedExperience());
                jobQuestProgressRepository.save(progress);
            } else {
                // 기존 JobQuestProgress 업데이트
                JobQuestProgressEntity progress = existingProgress.get();
                progress.setProductivity(0.0); // 생산성 초기화
                progress.setExperience(0);     // 경험치 초기화
                progress.setAccumulatedExperience(user.getAccumulatedExperience());
                jobQuestProgressRepository.save(progress);
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

    private double parseDouble(List<Object> row, int index) {
        try {
            return row.size() > index && !row.get(index).toString().isEmpty() ? Double.parseDouble(row.get(index).toString().trim()) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int getCurrentWeek() {
        return LocalDate.now().getDayOfYear() / 7 + 1;
    }
}
