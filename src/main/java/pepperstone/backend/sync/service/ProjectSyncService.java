package pepperstone.backend.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pepperstone.backend.common.entity.ChallengeProgressEntity;
import pepperstone.backend.common.entity.ChallengesEntity;
import pepperstone.backend.common.entity.ProjectsEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.ChallengeType;
import pepperstone.backend.common.repository.ChallengeProgressRepository;
import pepperstone.backend.common.repository.ChallengesRepository;
import pepperstone.backend.common.repository.ProjectsRepository;
import pepperstone.backend.common.repository.UserRepository;
import pepperstone.backend.notification.service.FcmService;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectSyncService {
    private final SyncService syncService;
    private final FcmService fcmService;
    private final UserRepository userRepository;
    private final ProjectsRepository projectsRepository;
    private final ChallengeProgressRepository challengeProgressRepository;
    private final ChallengesRepository challengesRepository;

    private static final String RANGE = "'참고. 전사 프로젝트'!A1:Z100";

    @Transactional
    public void sync(String spreadsheetId) {
        List<List<Object>> data = syncService.readSheet(spreadsheetId, RANGE);

        if (data == null || data.size() <= 7) {
            throw new RuntimeException("Insufficient data in the spreadsheet.");
        }

        // 전사 프로젝트 정보 가져오기(사번, 이름, 전사 프로젝트명, 부여경험치)
        List<Map<String, Object>> projectList = projectList(data);

        // 전사 프로젝트 동기화
        for (Map<String, Object> projectInfo : projectList) {
            String companyNum = projectInfo.get("companyNum").toString();
            String name = projectInfo.get("name").toString();
            String projectName = projectInfo.get("projectName").toString();
            int experience = (int) projectInfo.get("experience");

            // users 테이블에서 해당 사원 조회
            UserEntity user = userRepository.findByCompanyNumAndName(companyNum, name)
                    .orElseThrow(() -> new RuntimeException("User not found: " + companyNum + ", " + name));

            // 이미 동일한 프로젝트가 저장되어 있는지 확인
            boolean projectExists = projectsRepository.existsByUsersAndProjectName(user, projectName);
            if (projectExists) {
                System.out.println("이미 등록된 프로젝트입니다: " + projectName + " (" + companyNum + ")");
                continue;
            }

            // 새로운 프로젝트 생성 및 저장
            ProjectsEntity project = new ProjectsEntity();
            project.setUsers(user);
            project.setProjectName(projectName);
            project.setExperience(experience);
            project.setCreatedAt(LocalDate.now());

            projectsRepository.save(project);

            // 푸시 알림 전송
            fcmService.sendExperienceNotification(user, experience);

            // 도전 과제 체크 및 업데이트
            checkAndUpdateChallenge(user);
        }

    }

    private void checkAndUpdateChallenge(UserEntity user) {
        // 'PROJECT_PARTICIPATION' 유형의 도전 과제를 조회
        ChallengesEntity challenge = challengesRepository.findByType(ChallengeType.PROJECT_PARTICIPATION)
                .orElseGet(() -> {
                    // 새로운 도전 과제 엔티티 생성
                    ChallengesEntity newChallenge = ChallengesEntity.builder()
                            .name("전사프로젝트 1회 참여")
                            .description("전사프로젝트에 1회 참여해보세요!")
                            .requiredCount(1)
                            .type(ChallengeType.PROJECT_PARTICIPATION)
                            .build();
                    return challengesRepository.save(newChallenge);
                });

        // 해당 유저와 도전 과제에 대한 진행 상황을 조회
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

        // 현재 진행 횟수를 1로 설정하고 도전 과제 완료로 표시
        progress.setCurrentCount(1);
        progress.setCompleted(true);

        // FCM 푸시 알림 전송
        String title = "도전 과제 완료!";
        String body = "전사프로젝트 1회 참여 도전 과제를 완료하셨습니다!";
        fcmService.sendPushChallenge(user, title, body);

        challengeProgressRepository.save(progress);
    }

    private int parseInteger(List<Object> row, int index) {
        try {
            return row.size() > index && !row.get(index).toString().isEmpty() ? Integer.parseInt(row.get(index).toString().trim()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // 전사 프로젝트 정보를 저장한 리스트 반환 메서드
    private List<Map<String, Object>> projectList(List<List<Object>> data){
        // 전사 프로젝트 정보를 저장할 리스트
        List<Map<String, Object>> projectList = new ArrayList<>();

        // 8번째 행에서 정보 읽기
        int startRow = 7;
        // 사번이 빈칸이면 종료
        while(startRow < data.size() && data.get(startRow).size() > 6 && !data.get(startRow).get(3).toString().isEmpty()) {
            List<Object> projectRow = data.get(startRow);

            // 프로젝트 사원 및 경험치 정보
            String companyNum = projectRow.get(3).toString().trim(); // 사번
            String name = projectRow.get(4).toString().trim(); // 이름
            String projectName = projectRow.get(5).toString().trim(); // 전사 프로젝트명
            int experience = parseInteger(projectRow, 6); // 부여 경험치

            // 사원 정보를 맵으로 저장
            Map<String, Object> peopleInfo = new HashMap<>();
            peopleInfo.put("companyNum", companyNum);
            peopleInfo.put("name", name);
            peopleInfo.put("projectName", projectName);
            peopleInfo.put("experience", experience);

            // 리스트에 사원 정보 추가
            projectList.add(peopleInfo);

            startRow++;
        }

        return projectList;
    }
}
