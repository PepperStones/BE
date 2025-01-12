package pepperstone.backend.board.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import pepperstone.backend.common.entity.*;
import pepperstone.backend.common.entity.enums.ChallengeType;
import pepperstone.backend.common.entity.enums.UserRole;
import pepperstone.backend.common.repository.*;
import pepperstone.backend.notification.service.FcmService;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class BoardService {
    private final UserRepository userRepo;
    private final CenterGroupRepository centerGroupRepo;
    private final BoardsRepository boardsRepo;
    private final BoardTrackingRepository boardTrackingRepository;
    private final ChallengeProgressRepository challengeProgressRepository;
    private final ChallengesRepository challengesRepository;
    private final FcmService fcmService;

    public Boolean isAdmin(final Long userId) {
        final UserEntity user = userRepo.findById(userId).orElse(null);

        if (user == null)
            throw new IllegalArgumentException("해당하는 유저 정보가 없습니다.");

        return user.getRole() == UserRole.ADMIN;
    }

    public boolean checkCenterGroup(final String centerGroup) {
        return centerGroupRepo.existsByCenterName(centerGroup);
    }

    public void addOrUpdateBoard(final BoardsEntity board) {
        boardsRepo.save(board);
    }

    public Slice<BoardsEntity> getAllBoards(final Pageable pageable) {
        return boardsRepo.findAll(pageable);
    }

    public BoardsEntity getBoard(final Long boardId) {
        final BoardsEntity board = boardsRepo.findById(boardId).orElse(null);

        if (board == null)
            throw new IllegalArgumentException("게시글이 존재하지 않습니다.");

        return board;
    }

    public void deleteBoard(final Long boardId) {
        final BoardsEntity board = boardsRepo.findById(boardId).orElse(null);

        if (board == null)
            throw new IllegalArgumentException("게시글이 존재하지 않습니다.");

        boardsRepo.delete(board);
    }

    public UserEntity getUserInfo(final Long userId) {
        return userRepo.findById(userId).orElse(null);
    }

    public Slice<BoardsEntity> getFilterBoards(final String centerGroup, final String jobGroup, final Pageable pageable) {
        return boardsRepo.findAllWithFilters(centerGroup, jobGroup, pageable);
    }

    public BoardsEntity getBoardUser(final Long boardId, final UserEntity userInfo) {
        final BoardsEntity board = boardsRepo.findById(boardId).orElse(null);

        if (board == null)
            throw new IllegalArgumentException("게시글이 존재하지 않습니다.");

        if (board.getCenterGroup() != null && !board.getCenterGroup().equals(userInfo.getJobGroup().getCenterGroup().getCenterName()))
            throw new IllegalArgumentException("권한이 없는 게시글입니다.");

        if (board.getJobGroup() != null && !board.getJobGroup().equals(userInfo.getJobGroup().getJobName()))
            throw new IllegalArgumentException("권한이 없는 게시글입니다.");

        return board;
    }

    @Transactional
    public void checkAndUpdateChallenge(UserEntity user, BoardsEntity board) {
        // 'BOARD_READ' 유형의 도전 과제 조회 또는 생성
        ChallengesEntity challenge = challengesRepository.findByType(ChallengeType.BOARD_READ)
                .orElseGet(() -> {
                    ChallengesEntity newChallenge = ChallengesEntity.builder()
                            .name("게시글 5개 조회")
                            .description("게시판 탭에서 게시글 5개를 확인해보세요!")
                            .requiredCount(5)
                            .type(ChallengeType.BOARD_READ)
                            .build();
                    return challengesRepository.save(newChallenge);
                });

        // 게시글 조회 기록이 이미 있는지 확인
        boolean alreadyTracked = boardTrackingRepository.existsByUsersAndBoards(user, board);
        if (alreadyTracked) {
            return; // 이미 기록된 게시글이면 로직 종료
        }

        // 새로운 게시글 조회 기록 저장
        BoardTrackingEntity boardTracking = BoardTrackingEntity.builder()
                .users(user)
                .boards(board)
                .build();
        boardTrackingRepository.save(boardTracking);

        // 해당 유저의 도전 과제 진행 상황 조회 또는 초기값으로 생성
        ChallengeProgressEntity progress = challengeProgressRepository.findByUsersAndChallenges(user, challenge)
                .orElseGet(() -> {
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

        // 현재 진행 횟수를 증가
        progress.setCurrentCount(progress.getCurrentCount() + 1);

        // 도전 과제 완료 여부 체크
        if (progress.getCurrentCount() >= challenge.getRequiredCount()) {
            progress.setCompleted(true);

            String title = "도전과제 달성!";
            String body = "도전과제를 달성하셨습니다! 더 자세한 내용은 홈 탭 > 도전과제에서 확인해보세요.";
            fcmService.sendPushChallenge(user, title, body);
        }

        challengeProgressRepository.save(progress);
    }

    // 신규 게시글 등록 후 알림 전송 로직
    public void sendNewBoardNotification(BoardsEntity board) {
        List<UserEntity> targetUsers;

        // centerGroup과 jobGroup이 null인 경우 모든 유저에게 알림
        if (board.getCenterGroup() == null && board.getJobGroup() == null) {
            targetUsers = userRepo.findAll();
        } else {
            // centerGroup과 jobGroup에 따라 해당 소속 유저들에게 알림
            targetUsers = userRepo.findByJobGroup_CenterGroup_CenterNameAndJobGroup_JobName(
                    board.getCenterGroup(), board.getJobGroup()
            );
        }

        String title = "신규 게시글 등록!";
        String body = "신규 게시글이 등록되었습니다. 게시판 탭에서 등록된 내용을 확인해보세요.";

        for (UserEntity user : targetUsers) {
            fcmService.sendPushChallenge(user, body, title);
        }
    }
}
