package pepperstone.backend.challenge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pepperstone.backend.challenge.dto.response.ChallengeProgressDto;
import pepperstone.backend.challenge.dto.response.ChallengeReceiveDto;
import pepperstone.backend.challenge.dto.response.ChallengeDto;
import pepperstone.backend.common.entity.ChallengeProgressEntity;
import pepperstone.backend.common.entity.ChallengesEntity;
import pepperstone.backend.common.entity.UnlockStatusEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.ChallengeType;
import pepperstone.backend.common.entity.enums.ItemType;
import pepperstone.backend.common.repository.ChallengeProgressRepository;
import pepperstone.backend.common.repository.ChallengesRepository;
import pepperstone.backend.common.repository.UnlockStatusRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeProgressRepository challengeProgressRepository;
    private final ChallengesRepository challengesRepository;
    private final UnlockStatusRepository unlockStatusRepository;

    // 도전과제 달성 아이템 수령 메서드
    @Transactional
    public ChallengeReceiveDto receiveReward(UserEntity user, Long challengeProgressId) {
        // 도전 과제 진행 상황 조회
        ChallengeProgressEntity progress = challengeProgressRepository.findByIdAndUsers(challengeProgressId, user)
                .orElseThrow(() -> new IllegalArgumentException("Challenge progress not found."));

        // 이미 수령 완료된 경우 예외 발생
        if (progress.getReceive()) {
            throw new IllegalArgumentException("Challenge reward has already been received.");
        }

        // 수령 여부 업데이트
        progress.setReceive(true);
        challengeProgressRepository.save(progress);

        // 아이템 부여 로직 추가
        ChallengesEntity challenge = progress.getChallenges();
        String itemValue = getItemForChallenge(challenge.getType()); // 과제별 아이템 값 가져오기
        if (itemValue != null) {
            UnlockStatusEntity unlockStatus = UnlockStatusEntity.builder()
                    .itemType(ItemType.SKIN) // 모든 과제에 대해 SKIN 타입 아이템 부여
                    .itemValue(itemValue)
                    .users(user)
                    .build();
            unlockStatusRepository.save(unlockStatus);
            log.info("아이템 해금: 사용자={}, 아이템 타입={}, 아이템 값={}", user.getName(), ItemType.SKIN, itemValue);
        }

        // DTO에 응답 데이터 세팅 후 반환
        return ChallengeReceiveDto.builder()
                .challengeProgressId(progress.getId())
                .receive(progress.getReceive())
                .build();
    }

    // 도전과제 리스트 반환 메서드
    public List<ChallengeDto> getChallengeList(UserEntity user) {
        // 모든 도전 과제 유형에 대해 진행 상황을 조회하고 없으면 초기값으로 생성
        for (ChallengeType type : ChallengeType.values()) {
            getOrCreateChallengeProgress(user, type);
        }

        // 유저의 진행 상황 리스트 반환
        List<ChallengeProgressEntity> progressList = challengeProgressRepository.findByUsers(user);

        if (progressList.isEmpty()) {
            throw new IllegalArgumentException("No challenges found for the user.");
        }

        return progressList.stream()
                .map(this::toChallengeResponseDTO)
                .collect(Collectors.toList());
    }

    // ============== private method ================

    // 도전 과제를 조회하고 없으면 생성, 진행 상황을 조회하고 없으면 초기값으로 생성하는 메서드
    private void getOrCreateChallengeProgress(UserEntity user, ChallengeType type) {
        // 도전 과제 조회 또는 생성
        ChallengesEntity challenge = challengesRepository.findByType(type)
                .orElseGet(() -> {
                    ChallengesEntity newChallenge = ChallengesEntity.builder()
                            .name(getChallengeName(type))
                            .description(getChallengeDescription(type))
                            .requiredCount(getChallengeRequiredCount(type))
                            .type(type)
                            .build();
                    return challengesRepository.save(newChallenge);
                });

        // 진행 상황 조회 또는 초기값으로 생성
        challengeProgressRepository.findByUsersAndChallenges(user, challenge)
                .orElseGet(() -> {
                    ChallengeProgressEntity newProgress = new ChallengeProgressEntity();
                    newProgress.setUsers(user);
                    newProgress.setChallenges(challenge);
                    newProgress.setCurrentCount(0);
                    newProgress.setCompleted(false);
                    newProgress.setReceive(false);
                    return challengeProgressRepository.save(newProgress);
                });
    }

    // 과제별 아이템 값 매핑 메서드
    private String getItemForChallenge(ChallengeType type) {
        return switch (type) {
            case BOARD_READ -> "S1";
            case JOB_QUEST_MAX -> "S2";
            case LEADER_QUEST_MAX -> "S3";
            case PROJECT_PARTICIPATION -> "S4";
            case PERFORMANCE_A -> "S5";
        };
    }

    // 엔티티를 ChallengeResponseDTO로 변환하는 메서드
    private ChallengeDto toChallengeResponseDTO(ChallengeProgressEntity progress) {
        ChallengesEntity challenge = progress.getChallenges();

        return ChallengeDto.builder()
                .challengesId(challenge.getId())
                .name(challenge.getName())
                .description(challenge.getDescription())
                .requiredCount(challenge.getRequiredCount())
                .challengeProgress(toChallengeProgress0Dto(progress))
                .build();
    }

    // 엔티티를 ChallengeProgressResponseDTO로 변환하는 메서드
    private ChallengeProgressDto toChallengeProgress0Dto(ChallengeProgressEntity progress) {
        return ChallengeProgressDto.builder()
                .challengeProgressId(progress.getId())
                .currentCount(progress.getCurrentCount())
                .completed(progress.getCompleted())
                .receive(progress.getReceive())
                .build();
    }

    // 도전 과제 유형에 따른 이름 반환
    private String getChallengeName(ChallengeType type) {
        return switch (type) {
            case JOB_QUEST_MAX -> "직무별 퀘스트 MAX 기준 10회 달성";
            case LEADER_QUEST_MAX -> "리더부여 퀘스트 MAX 기준 10회 달성";
            case PROJECT_PARTICIPATION -> "전사프로젝트 1회 참여";
            case PERFORMANCE_A -> "인사평가 A 등급 달성";
            case BOARD_READ -> "게시글 5개 조회 달성";
        };
    }

    // 도전 과제 유형에 따른 설명 반환
    private String getChallengeDescription(ChallengeType type) {
        return switch (type) {
            case JOB_QUEST_MAX -> "직무별 퀘스트에서 MAX 기준을 10회 달성해보세요!";
            case LEADER_QUEST_MAX -> "리더부여 퀘스트에서 MAX 기준을 10회 달성해보세요!";
            case PROJECT_PARTICIPATION -> "전사프로젝트에 1회 참여해보세요!";
            case PERFORMANCE_A -> "인사평가에서 A 등급을 달성해보세요!";
            case BOARD_READ -> "게시판 탭에서 게시글을 5개 확인해보세요!";
        };
    }

    // 도전 과제 유형에 따른 필요 달성 횟수 반환
    private int getChallengeRequiredCount(ChallengeType type) {
        return switch (type) {
            case JOB_QUEST_MAX, LEADER_QUEST_MAX -> 10;
            case PROJECT_PARTICIPATION, PERFORMANCE_A -> 1;
            case BOARD_READ -> 5;
        };
    }
}