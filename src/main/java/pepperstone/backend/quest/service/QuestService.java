package pepperstone.backend.quest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pepperstone.backend.common.entity.JobQuestProgressEntity;
import pepperstone.backend.common.entity.LeaderQuestProgressEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.repository.JobQuestProgressRepository;
import pepperstone.backend.common.repository.LeaderQuestProgressRepository;
import pepperstone.backend.common.repository.UserRepository;
import pepperstone.backend.quest.dto.request.QuestDetailRequestDTO;
import pepperstone.backend.quest.dto.response.QuestProgressResponseDTO;
import pepperstone.backend.quest.dto.response.WeeklyQuestResponseDTO;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class QuestService {
    private final UserRepository userRepo;
    private final JobQuestProgressRepository jobQuestProgressRepo;
    private final LeaderQuestProgressRepository leaderQuestProgressRepo;

    public UserEntity getUserInfo(final Long userId) {
        return userRepo.findById(userId).orElse(null);
    }

    public List<QuestProgressResponseDTO.jobQuests> getJobQuests(final UserEntity user) {
        final List<JobQuestProgressEntity> quests = jobQuestProgressRepo.findByUsers(user);

        return quests.stream()
                .map(quest -> QuestProgressResponseDTO.jobQuests.builder()
                        .id(quest.getJobQuest().getId())
                        .period(quest.getJobQuest().getPeriod())
                        .accumulatedExperience(quest.getAccumulatedExperience() + quest.getExperience())
                        .maxStandard(quest.getJobQuest().getMaxStandard())
                        .mediumStandard(quest.getJobQuest().getMediumStandard())
                        .build())
                .toList();
    }

    public List<QuestProgressResponseDTO.leaderQuests> getLeaderQuests(final UserEntity user) {
        final List<LeaderQuestProgressEntity> quests = leaderQuestProgressRepo.findByUsers(user);

        return quests.stream()
                .map(quest -> QuestProgressResponseDTO.leaderQuests.builder()
                        .id(quest.getLeaderQuests().getId())
                        .period(quest.getLeaderQuests().getPeriod())
                        .accumulatedExperience(quest.getAccumulatedExperience() + quest.getExperience())
                        .questName(quest.getLeaderQuests().getQuestName())
                        .maxCondition(quest.getLeaderQuests().getMaxCondition())
                        .medianCondition(quest.getLeaderQuests().getMedianCondition())
                        .weight(quest.getLeaderQuests().getWeight())
                        .build())
                .toList();
    }

//    public List<WeeklyQuestResponseDTO> getWeeklyQuests(final UserEntity user, final QuestDetailRequestDTO dto) {
//        if (dto.getType().equals("job")) {
//
//        }
//    }
}
