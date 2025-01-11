package pepperstone.backend.member.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.UserRole;
import pepperstone.backend.common.repository.UserRespository;

@RequiredArgsConstructor
@Slf4j
@Service
public class MemberService {
    private final UserRespository userRepo;

    public Boolean isAdmin(final Long userId) {
        final UserEntity user = userRepo.findById(userId).orElse(null);

        if (user == null)
            throw new IllegalArgumentException("해당하는 유저 정보가 없습니다.");

        return user.getRole() == UserRole.ADMIN;
    }

    public Slice<UserEntity> getAllUsers(final String search, final String centerGroup, final String jobGroup, final Pageable pageable) {
        return userRepo.findAllWithFilters(
                StringUtils.isBlank(search) ? null : search,
                StringUtils.isBlank(centerGroup) ? null : centerGroup,
                StringUtils.isBlank(jobGroup) ? null : jobGroup,
                pageable
        );
    }
}
