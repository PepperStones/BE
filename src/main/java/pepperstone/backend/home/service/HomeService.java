package pepperstone.backend.home.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pepperstone.backend.common.entity.*;
import pepperstone.backend.common.repository.*;
import pepperstone.backend.home.dto.response.HomeDto;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeService {

    private final JobQuestProgressRepository jobQuestProgressRepository;
    private final LeaderQuestProgressRepository leaderQuestProgressRepository;
    private final ProjectsRepository projectsRepository;
    private final PerformanceEvaluationRepository performanceEvaluationRepository;
    private final UserRepository userRepository;

    @Transactional
    public HomeDto getHomeData(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("Not Found: 데이터가 존재하지 않습니다.");
        }

        return HomeDto.builder()
                .user(getUserData(user))
                .team(getTeamData(user))
                .build();
    }

    // ============== private methods ================

    // 사용자 데이터를 생성하는 메서드
    private HomeDto.UserDto getUserData(UserEntity user) {
        // 최근 획득 경험치
        int recentExperience = getRecentExperience(user);

        // 올해의 시작일과 끝일
        LocalDate startOfYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate endOfYear = LocalDate.of(LocalDate.now().getYear(), 12, 31);

        // 올해 획득한 총 경험치 합산
        int totalExperienceThisYear = getLatestJobAndLeaderQuestExperience(user, startOfYear, endOfYear)
                + getProjectExperience(user, startOfYear, endOfYear)
                + getPerformanceEvaluationExperience(user, startOfYear, endOfYear);

        return HomeDto.UserDto.builder()
                .name(user.getName())
                .level(user.getLevel())
                .centerName(user.getJobGroup().getCenterGroup().getCenterName())
                .jobName(user.getJobGroup().getJobName())
                .skin(user.getSkin())
                .decoration(user.getDecoration())
                .effect(user.getEffect())
                .recentExperience(recentExperience)
                .totalExperienceThisYear(totalExperienceThisYear)
                .build();
    }

    // 팀원 데이터를 생성하는 메서드
    private HomeDto.TeamDto getTeamData(UserEntity user) {
        List<UserEntity> teamMembers = userRepository.findByJobGroup_JobNameAndJobGroup_CenterGroup_Id(
                user.getJobGroup().getJobName(),
                user.getJobGroup().getCenterGroup().getId()
        );

        List<String> levels = teamMembers.stream()
                .map(UserEntity::getLevel)
                .collect(Collectors.toList());

        return HomeDto.TeamDto.builder()
                .count(teamMembers.size()) // 팀원 수
                .levels(levels) // 팀원 레벨 리스트
                .build();
    }

    // 최근 획득 경험치를 계산하는 메서드
    private int getRecentExperience(UserEntity user) {
        // 각 엔티티에서 (경험치, 날짜) 쌍을 추출
        // 직무 퀘스트
        Optional<Pair<Integer, LocalDate>> jobExperience = jobQuestProgressRepository.findByUsers(user).stream()
                .max(Comparator.comparing(JobQuestProgressEntity::getCreatedAt)
                        .thenComparing(JobQuestProgressEntity::getWeek, Comparator.reverseOrder()))
                .map(jq -> Pair.of(jq.getExperience(), jq.getCreatedAt()));

        // 리더부여 퀘스트
        Optional<Pair<Integer, LocalDate>> leaderExperience = leaderQuestProgressRepository.findByUsers(user).stream()
                .max(Comparator.comparing(LeaderQuestProgressEntity::getCreatedAt)
                        .thenComparing(lq -> lq.getWeek() != null ? lq.getWeek() : lq.getMonth(), Comparator.reverseOrder()))
                .map(lq -> Pair.of(lq.getExperience(), lq.getCreatedAt()));

        // 전사 프로젝트
        Optional<Pair<Integer, LocalDate>> projectExperience = projectsRepository.findByUsers(user).stream()
                .max(Comparator.comparing(ProjectsEntity::getCreatedAt))
                .map(p -> Pair.of(p.getExperience(), p.getCreatedAt()));

        // 인사평가
        Optional<Pair<Integer, LocalDate>> evaluationExperience = performanceEvaluationRepository.findByUsers(user).stream()
                .max(Comparator.comparing(PerformanceEvaluationEntity::getCreatedAt))
                .map(e -> Pair.of(e.getExperience(), e.getCreatedAt()));

        // 4개의 Optional<Pair> 중 가장 최신 날짜의 경험치 선택
        return Stream.of(jobExperience, leaderExperience, projectExperience, evaluationExperience)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparing(Pair::getRight)) // 날짜 기준으로 최대값 찾기
                .map(Pair::getLeft) // 경험치 값 반환
                .orElse(0); // 데이터가 없으면 0 반환
    }

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