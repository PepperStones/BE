package pepperstone.backend.member.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import pepperstone.backend.common.entity.CenterGroupEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.UserRole;
import pepperstone.backend.common.repository.CenterGroupRepository;
import pepperstone.backend.common.repository.UserRespository;
import pepperstone.backend.member.dto.request.MemberUpdateRequestDTO;

@RequiredArgsConstructor
@Slf4j
@Service
public class MemberService {
    private final UserRespository userRepo;
    private final CenterGroupRepository centerGroupRepo;

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

    public UserEntity getUserInfo(final Long userId) {
        return userRepo.findById(userId).orElse(null);
    }

    public void deleteMember(final Long userId) {
        userRepo.deleteById(userId);
    }

    public void updateMember(final UserEntity user, final MemberUpdateRequestDTO dto) {
        if (isNotBlank(dto.getCompanyNum()))
            user.setCompanyNum(dto.getCompanyNum());

        if (isNotBlank(dto.getName()))
            user.setName(dto.getName());

        if (dto.getJoinDate() != null)
            user.setJoinDate(dto.getJoinDate());

        final CenterGroupEntity centerInfo = centerGroupRepo.findByCenterName(dto.getCenterGroup());

        if(centerInfo != null)
            user.getJobGroup().setCenterGroup(centerInfo);
        else
            throw new IllegalArgumentException("존재하지 않는 센터입니다.");

        if (isNotBlank(dto.getJobGroup()))
            user.getJobGroup().setJobName(dto.getJobGroup());

        if (isNotBlank(dto.getLevel()))
            user.setLevel(dto.getLevel());

        userRepo.save(user);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
