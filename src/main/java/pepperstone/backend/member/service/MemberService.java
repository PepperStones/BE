package pepperstone.backend.member.service;

import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import pepperstone.backend.common.entity.CenterGroupEntity;
import pepperstone.backend.common.entity.JobGroupEntity;
import pepperstone.backend.common.entity.UnlockStatusEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.ItemType;
import pepperstone.backend.common.entity.enums.UserRole;
import pepperstone.backend.common.repository.CenterGroupRepository;
import pepperstone.backend.common.repository.JobGroupReository;
import pepperstone.backend.common.repository.UnlockStatusRepository;
import pepperstone.backend.common.repository.UserRepository;
import pepperstone.backend.member.dto.request.MemberAddRequestDTO;
import pepperstone.backend.member.dto.request.MemberUpdateRequestDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Slf4j
@Service
public class MemberService {
    private final UserRepository userRepo;
    private final CenterGroupRepository centerGroupRepo;
    private final JobGroupReository jobGroupRepo;
    private final UnlockStatusRepository unlockStatusRepo;

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

    @Transactional
    public void addMemberAndJobGroup(MemberAddRequestDTO dto) {
        try {
            final JobGroupEntity jobGroup = addJobGroup(dto.getJobGroup(), dto.getCenterGroup());

            final UserEntity user = UserEntity.builder()
                    .companyNum(dto.getCompanyNum())
                    .name(dto.getName())
                    .joinDate(dto.getJoinDate())
                    .level(dto.getLevel())
                    .jobGroup(jobGroup)
                    .userId(dto.getUserId())
                    .initPassword(dto.getInitPassword())
                    .password(dto.getInitPassword())
                    .role(UserRole.USER)
                    .build();

            final Map<ItemType, String> itemMap = Map.of(
                    ItemType.SKIN, "S",
                    ItemType.DECORATION, "D",
                    ItemType.EFFECT, "E"
            );

            for (Map.Entry<ItemType, String> entry : itemMap.entrySet()) {
                final UnlockStatusEntity unlockStatus = UnlockStatusEntity.builder()
                        .itemType(entry.getKey())
                        .itemValue(entry.getValue() + "0")
                        .users(user)
                        .build();
                unlockStatusRepo.save(unlockStatus);
            }

            unlockStatusRepo.save(UnlockStatusEntity.builder()
                    .itemType(ItemType.DECORATION)
                    .itemValue("Dx")
                    .users(user)
                    .build());

            unlockStatusRepo.save(UnlockStatusEntity.builder()
                    .itemType(ItemType.EFFECT)
                    .itemValue("Ex")
                    .users(user)
                    .build());

            addMember(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("구성원 추가 오류. 잠시 후 다시 시도해주세요.", e);
        }
    }

    public Boolean duplicateUserId(final String userId) {
        return userRepo.existsByUserId(userId);
    }

    public Boolean checkCompanyNum(final String companyNum) {
        // 10자리인지 확인 && 숫자만 있는지 확인
        if (companyNum.length() != 10 || !companyNum.matches("\\d+"))
            return false;

        return true;
    }

    // ============== private method ================

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private JobGroupEntity addJobGroup(final String jobGroup, final String centerGroup) {
        final CenterGroupEntity centerInfo = centerGroupRepo.findByCenterName(centerGroup);

        if(centerInfo == null)
            throw new IllegalArgumentException("존재하지 않는 센터입니다.");

        final JobGroupEntity jobGroupEntity = JobGroupEntity.builder()
                .jobName(jobGroup)
                .centerGroup(centerInfo)
                .build();

        return jobGroupRepo.save(jobGroupEntity);
    }

    private void addMember(final UserEntity user) {
        userRepo.save(user);
    }
}
