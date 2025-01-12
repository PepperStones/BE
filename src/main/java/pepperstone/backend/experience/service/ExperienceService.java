package pepperstone.backend.experience.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pepperstone.backend.common.entity.*;
import pepperstone.backend.common.repository.JobQuestProgressRepository;
import pepperstone.backend.common.repository.LeaderQuestProgressRepository;
import pepperstone.backend.common.repository.PerformanceEvaluationRepository;
import pepperstone.backend.common.repository.ProjectsRepository;
import pepperstone.backend.experience.dto.response.ExperienceDto;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final JobQuestProgressRepository jobQuestProgressRepository;
    private final LeaderQuestProgressRepository leaderQuestProgressRepository;
    private final ProjectsRepository projectsRepository;
    private final PerformanceEvaluationRepository performanceEvaluationRepository;

    @Transactional
    public ExperienceDto getCurrentExperience(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("유효하지 않은 사용자입니다.");
        }

        // 올해의 시작일과 끝일
        LocalDate startOfYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate endOfYear = LocalDate.of(LocalDate.now().getYear(), 12, 31);

        // 작년까지 누적 경험치
        int accumulatedExperienceLastYear = user.getAccumulatedExperience();

        // 올해 획득한 총 경험치 합산
        int totalExperienceThisYear = getLatestJobAndLeaderQuestExperience(user, startOfYear, endOfYear)
                + getProjectExperience(user, startOfYear, endOfYear)
                + getPerformanceEvaluationExperience(user, startOfYear, endOfYear);

        return ExperienceDto.builder()
                .user(ExperienceDto.UserDto.builder()
                        .companyNum(user.getCompanyNum())
                        .centerName(user.getJobGroup().getCenterGroup().getCenterName())
                        .jobName(user.getJobGroup().getJobName())
                        .name(user.getName())
                        .level(user.getLevel())
                        .skin(user.getSkin().name())
                        .build())
                .experience(ExperienceDto.ExperienceInfoDto.builder()
                        .accumulatedExperienceLastYear(accumulatedExperienceLastYear)
                        .totalExperienceThisYear(totalExperienceThisYear)
                        .build())
                .build();
    }

    // ============== private method ================

    // 직퀘, 리퀘에서 조건에 맞는 최신 데이터의 accumulatedExperience + experience 값을 반환
    private int getLatestJobAndLeaderQuestExperience(UserEntity user, LocalDate startDate, LocalDate endDate) {
        // JobQuestProgress에서 연도가 올해이고 week가 가장 높은 데이터 조회
        Optional<JobQuestProgressEntity> latestJobQuest = jobQuestProgressRepository
                .findByUsersAndCreatedAtBetween(user, startDate, endDate)
                .stream()
                .max(Comparator.comparing(JobQuestProgressEntity::getWeek));

        // LeaderQuestProgress에서 leader_quest_id별로 연도가 올해이고 week나 month가 가장 큰 데이터 조회
        List<LeaderQuestProgressEntity> leaderQuestProgressList = leaderQuestProgressRepository
                .findByUsersAndCreatedAtBetween(user, startDate, endDate);

        // leader_quest_id별로 최신 데이터를 선택하여 누적 경험치 합산
        int leaderQuestExperience = leaderQuestProgressList.stream()
                .collect(Collectors.groupingBy(lq -> lq.getLeaderQuests().getId(), // leader_quest_id 기준으로 그룹화
                        Collectors.maxBy(Comparator.comparing(lq -> lq.getWeek() != null ? lq.getWeek() : lq.getMonth())))) // week 또는 month 기준으로 최신 데이터 선택
                .values().stream()
                .filter(Optional::isPresent) // Optional 필터링
                .mapToInt(optLq -> {
                    LeaderQuestProgressEntity lq = optLq.get();
                    return lq.getAccumulatedExperience() + lq.getExperience();
                })
                .sum();

        // 가장 최신 데이터 중 하나를 선택하여 경험치 계산
        int jobQuestExperience = latestJobQuest.map(jq -> jq.getAccumulatedExperience() + jq.getExperience()).orElse(0);

        return jobQuestExperience + leaderQuestExperience;
    }

    private int getProjectExperience(UserEntity user, LocalDate startDate, LocalDate endDate) {
        return projectsRepository.findByUsersAndCreatedAtBetween(user, startDate, endDate)
                .stream()
                .mapToInt(ProjectsEntity::getExperience)
                .sum();
    }

    private int getPerformanceEvaluationExperience(UserEntity user, LocalDate startDate, LocalDate endDate) {
        return performanceEvaluationRepository.findByUsersAndCreatedAtBetween(user, startDate, endDate)
                .stream()
                .mapToInt(PerformanceEvaluationEntity::getExperience)
                .sum();
    }
}