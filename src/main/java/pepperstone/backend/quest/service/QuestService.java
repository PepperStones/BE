package pepperstone.backend.quest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pepperstone.backend.common.entity.*;
import pepperstone.backend.common.repository.*;
import pepperstone.backend.quest.dto.response.QuestProgressResponseDTO;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class QuestService {
    private final UserRepository userRepo;
    private final JobQuestRepository jobQuestRepo;
    private final JobQuestProgressRepository jobQuestProgressRepo;
    private final LeaderQuestProgressRepository leaderQuestProgressRepo;
    private final LeaderQuestRepository leaderQuestRepo;

    public UserEntity getUserInfo(final Long userId) {
        return userRepo.findById(userId).orElse(null);
    }

    public List<QuestProgressResponseDTO.jobQuests> getJobQuests(final UserEntity user) {
        final List<JobQuestsEntity> quests = jobQuestRepo.findByDepartmentAndJobGroup(
                user.getJobGroup().getCenterGroup().getCenterName(),
                user.getJobGroup().getJobName()).stream().toList();

        LocalDate startOfYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate endOfYear = LocalDate.of(LocalDate.now().getYear(), 12, 31);

        // JobQuestProgress에서 연도가 올해이고 week가 가장 높은 데이터 조회
        final Optional<JobQuestProgressEntity> latestJobQuest = jobQuestProgressRepo
                .findByUsersAndCreatedAtBetween(user, startOfYear, endOfYear)
                .stream()
                .max(Comparator.comparing(JobQuestProgressEntity::getWeek));

        // 가장 최신 데이터 중 하나를 선택하여 경험치 계산
        final int jobQuestExperience = latestJobQuest.map(jq -> jq.getAccumulatedExperience() + jq.getExperience()).orElse(0);

        return quests.stream()
                .map(quest -> QuestProgressResponseDTO.jobQuests.builder()
                        .id(quest.getId())
                        .period(quest.getPeriod())
                        .accumulatedExperience(jobQuestExperience)
                        .maxStandard(quest.getMaxStandard())
                        .mediumStandard(quest.getMediumStandard())
                        .build())
                .toList();
    }

    public List<QuestProgressResponseDTO.leaderQuests> getLeaderQuests(final UserEntity user) {
        // 모든 리더 퀘스트를 가져옴
        final List<LeaderQuestsEntity> quests = leaderQuestRepo.findByDepartmentAndJobGroup(
                user.getJobGroup().getCenterGroup().getCenterName(),
                user.getJobGroup().getJobName()
        );

        // LeaderQuestProgress에서 리퀘별 최신 데이터를 가져옴
        Map<Long, LeaderQuestProgressEntity> latestProgressMap = getLatestLeaderQuestProgress(user);

        return quests.stream()
                .map(quest -> {
                    LeaderQuestProgressEntity latestProgress = latestProgressMap.get(quest.getId());
                    int accumulatedExperience = latestProgress != null
                            ? latestProgress.getAccumulatedExperience() + latestProgress.getExperience()
                            : 0; // 최신 데이터가 없으면 기본값 0

                    return QuestProgressResponseDTO.leaderQuests.builder()
                            .id(quest.getId())
                            .period(quest.getPeriod())
                            .accumulatedExperience(accumulatedExperience)
                            .questName(quest.getQuestName())
                            .maxCondition(quest.getMaxCondition())
                            .medianCondition(quest.getMedianCondition())
                            .weight(quest.getWeight())
                            .build();
                })
                .toList();
    }

    // 최신 LeaderQuestProgress 데이터를 가져오는 메서드
    private Map<Long, LeaderQuestProgressEntity> getLatestLeaderQuestProgress(UserEntity user) {
        // 해당 유저의 LeaderQuestProgress 데이터를 가져옴
        List<LeaderQuestProgressEntity> leaderQuestProgressList = leaderQuestProgressRepo.findByUsers(user);

        return leaderQuestProgressList.stream()
                .collect(Collectors.groupingBy(
                        progress -> progress.getLeaderQuests().getId(), // 리더 퀘스트 ID로 그룹화
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(
                                        progress -> progress.getWeek() != null ? progress.getWeek() : progress.getMonth()
                                )),
                                optionalProgress -> optionalProgress.orElse(null) // Optional 처리
                        )
                ));
    }

//    public List<WeeklyQuestResponseDTO> getWeeklyQuests(final UserEntity user, final QuestDetailRequestDTO dto) {
//        if (dto.getType().equals("job")) {
//
//        }
//    }
}
