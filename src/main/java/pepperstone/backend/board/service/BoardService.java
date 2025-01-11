package pepperstone.backend.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void addBoard(final BoardsEntity board) {
        boardsRepo.save(board);
    }

    public Slice<BoardsEntity> getAllBoards(final Pageable pageable) {
        return boardsRepo.findAll(pageable);
    }
}
