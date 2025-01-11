package pepperstone.backend.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pepperstone.backend.common.entity.ChallengeProgressEntity;
import pepperstone.backend.common.entity.ChallengesEntity;
import pepperstone.backend.common.entity.PerformanceEvaluationEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.ChallengeType;
import pepperstone.backend.common.entity.enums.EvaluationPeriod;
import pepperstone.backend.common.entity.enums.Grade;
import pepperstone.backend.common.repository.ChallengeProgressRepository;
import pepperstone.backend.common.repository.ChallengesRepository;
import pepperstone.backend.common.repository.PerformanceEvaluationRepository;
import pepperstone.backend.common.repository.UserRepository;
import pepperstone.backend.notification.service.FcmService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationSyncService {
    private final SyncService syncService;
    private final FcmService fcmService;
    private final UserRepository userRepository;
    private final PerformanceEvaluationRepository performanceEvaluationRepository;
    private final ChallengesRepository challengesRepository;
    private final ChallengeProgressRepository challengeProgressRepository;

    private static final String RANGE = "'참고. 인사평가'!A1:Z100";

    @Transactional
    public void sync(String spreadsheetId) {
        List<List<Object>> data = syncService.readSheet(spreadsheetId, RANGE);

        if (data == null || data.size() <= 9) {
            throw new RuntimeException("Insufficient data in the spreadsheet.");
        }

        // 현재 날짜 기준으로 상반기 또는 하반기 결정
        LocalDate today = LocalDate.now();
        EvaluationPeriod evaluationPeriod = (today.getMonthValue() <= 6) ? EvaluationPeriod.H1 : EvaluationPeriod.H2;

        // 인사평가 정보 가져오기
        List<Map<String, Object>> evaluationList = evaluationList(data, evaluationPeriod);

        // 인사평가 동기화
        for (Map<String, Object> evaluationInfo : evaluationList) {
            String companyNum = evaluationInfo.get("companyNum").toString();
            String name = evaluationInfo.get("name").toString();
            Grade grade = (Grade) evaluationInfo.get("grade");
            int experience = (int) evaluationInfo.get("experience");

            // users 테이블에서 해당 사원 조회
            UserEntity user = userRepository.findByCompanyNumAndName(companyNum, name)
                    .orElseThrow(() -> new RuntimeException("User not found: " + companyNum + ", " + name));

            // 기존에 동일한 평가 기간과 연도에 대한 데이터가 있는 경우 삭제
            LocalDate startOfPeriod = evaluationPeriod == EvaluationPeriod.H1 ? LocalDate.of(today.getYear(), 1, 1) : LocalDate.of(today.getYear(), 7, 1);
            LocalDate endOfPeriod = evaluationPeriod == EvaluationPeriod.H1 ? LocalDate.of(today.getYear(), 6, 30) : LocalDate.of(today.getYear(), 12, 31);
            performanceEvaluationRepository.deleteByUsersAndEvaluationPeriodAndCreatedAtBetween(user, evaluationPeriod, startOfPeriod, endOfPeriod);

            // 새로운 인사평가 데이터 생성 및 저장
            PerformanceEvaluationEntity evaluation = new PerformanceEvaluationEntity();
            evaluation.setUsers(user);
            evaluation.setEvaluationPeriod(evaluationPeriod);
            evaluation.setGrade(grade);
            evaluation.setExperience(experience);
            evaluation.setCreatedAt(LocalDate.now());

            performanceEvaluationRepository.save(evaluation);

            // A 등급 달성 시 도전 과제 체크 및 업데이트
            if (grade == Grade.A) {
                checkAndUpdateChallenge(user);
            }

            // 푸시 알림 전송
            fcmService.sendEvaluationNotification(user, evaluationPeriod);
        }
    }

    private void checkAndUpdateChallenge(UserEntity user) {
        // 'GRADE_A_ACHIEVEMENT' 유형의 도전 과제를 조회
        ChallengesEntity challenge = challengesRepository.findByType(ChallengeType.PERFORMANCE_A)
                .orElseGet(() -> {
                    // 새로운 도전 과제 엔티티 생성
                    ChallengesEntity newChallenge = ChallengesEntity.builder()
                            .name("인사평가 A 등급 달성")
                            .description("인사평가에서 A 등급을 달성해보세요!")
                            .requiredCount(1)
                            .type(ChallengeType.PERFORMANCE_A)
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
        String title = "도전과제 달성!";
        String body = "도전과제를 달성하셨습니다! 더 자세한 내용은 홈 탭 > 도전과제에서 확인해보세요.";
        fcmService.sendPushChallenge(user, title, body);

        challengeProgressRepository.save(progress);
    }

    private List<Map<String, Object>> evaluationList(List<List<Object>> data, EvaluationPeriod period) {
        // 인사평가 정보를 저장할 리스트
        List<Map<String, Object>> evaluationList = new ArrayList<>();

        int startRow = 9; // 상반기: 10행, 하반기: 10행
        int startCol = (period == EvaluationPeriod.H1) ? 1 : 7; // 상반기: 2열, 하반기: 8열

        while (startRow < data.size() && data.get(startRow).size() > startCol && !data.get(startRow).get(startCol).toString().isEmpty()) {
            List<Object> row = data.get(startRow);

            // 인사평가 정보 파싱
            String companyNum = row.get(startCol).toString().trim(); // 사번
            String name = row.get(startCol + 1).toString().trim(); // 이름
            Grade grade = Grade.valueOf(row.get(startCol + 2).toString().trim().substring(0, 1)); // 등급 (첫 글자)
            int experience = parseInteger(row, startCol + 3); // 부여 경험치

            // 정보를 맵에 저장
            Map<String, Object> evaluationInfo = new HashMap<>();
            evaluationInfo.put("companyNum", companyNum);
            evaluationInfo.put("name", name);
            evaluationInfo.put("grade", grade);
            evaluationInfo.put("experience", experience);

            evaluationList.add(evaluationInfo);
            startRow++;
        }

        return evaluationList;
    }

    private int parseInteger(List<Object> row, int index) {
        try {
            return row.size() > index && !row.get(index).toString().isEmpty() ? Integer.parseInt(row.get(index).toString().trim()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
