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
import pepperstone.backend.experience.dto.response.RecentExperienceDto;

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

    /**
     * 현재 경험치 현황 정보 조회 메서드
     *
     * @param user
     * @return 사용자 정보, 경험치 정보 반환
     */
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

    /**
     * 수령 경험치 목룍 조회 메서드
     *
     * @param user
     * @return 인사평가, 직무별 퀘스트, 리더 부여 퀘스트, 전사 퀘스트에서 얻은 경험치 중 최신순으로 모든 경험치, 날짜, 퀘스트명 반환
     */
    @Transactional
    public RecentExperienceDto getRecentExperience(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("유효하지 않은 사용자입니다.");
        }

        return RecentExperienceDto.builder()
                .job(getJobExperience(user))
                .leader(getLeaderExperience(user))
                .project(getProjectExperience(user))
                .evaluation(getEvaluationExperience(user))
                .build();
    }

    // ============== private methods ================

    private List<RecentExperienceDto.JobDto> getJobExperience(UserEntity user) {
        return jobQuestProgressRepository.findByUsers(user).stream()
                .sorted(Comparator.comparing(JobQuestProgressEntity::getCreatedAt).reversed()
                        .thenComparing(JobQuestProgressEntity::getWeek).reversed())
                .map(jq -> new RecentExperienceDto.JobDto(jq.getExperience(), jq.getCreatedAt()))
                .collect(Collectors.toList());
    }

    private List<RecentExperienceDto.LeaderDto> getLeaderExperience(UserEntity user) {
        return leaderQuestProgressRepository.findByUsers(user).stream()
                .sorted(Comparator.comparing(LeaderQuestProgressEntity::getCreatedAt).reversed()
                        .thenComparing(lq -> lq.getWeek() != null ? lq.getWeek() : lq.getMonth(), Comparator.reverseOrder()))
                .map(lq -> new RecentExperienceDto.LeaderDto(lq.getExperience(), lq.getCreatedAt(), lq.getLeaderQuests().getQuestName()))
                .collect(Collectors.toList());
    }

    private List<RecentExperienceDto.ProjectDto> getProjectExperience(UserEntity user) {
        return projectsRepository.findByUsers(user).stream()
                .sorted(Comparator.comparing(ProjectsEntity::getCreatedAt).reversed())
                .map(p -> new RecentExperienceDto.ProjectDto(p.getExperience(), p.getCreatedAt(), p.getProjectName()))
                .collect(Collectors.toList());
    }

    private List<RecentExperienceDto.EvaluationDto> getEvaluationExperience(UserEntity user) {
        return performanceEvaluationRepository.findByUsers(user).stream()
                .sorted(Comparator.comparing(PerformanceEvaluationEntity::getCreatedAt).reversed())
                .map(e -> new RecentExperienceDto.EvaluationDto(e.getExperience(), e.getCreatedAt()))
                .collect(Collectors.toList());
    }
}