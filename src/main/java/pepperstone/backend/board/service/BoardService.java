package pepperstone.backend.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import pepperstone.backend.common.entity.BoardsEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.UserRole;
import pepperstone.backend.common.repository.BoardsRepository;
import pepperstone.backend.common.repository.CenterGroupRepository;
import pepperstone.backend.common.repository.UserRepository;

@RequiredArgsConstructor
@Slf4j
@Service
public class BoardService {
    private final UserRepository userRepo;
    private final CenterGroupRepository centerGroupRepo;
    private final BoardsRepository boardsRepo;

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
}
