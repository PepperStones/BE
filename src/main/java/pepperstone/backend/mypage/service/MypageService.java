package pepperstone.backend.mypage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import pepperstone.backend.common.entity.PerformanceEvaluationEntity;
import pepperstone.backend.common.entity.UnlockStatusEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.*;
import pepperstone.backend.common.repository.PerformanceEvaluationRepository;
import pepperstone.backend.common.repository.UnlockStatusRepository;
import pepperstone.backend.common.repository.UserRepository;
import pepperstone.backend.mypage.dto.response.StarResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
@Service
public class MypageService {
    private final UserRepository userRepo;
    private final PerformanceEvaluationRepository performanceEvaluationRepo;
    private final UnlockStatusRepository unlockStatusRepo;

    public UserEntity getUserInfo(final Long userId) {
        return userRepo.findById(userId).orElse(null);
    }

    public PerformanceEvaluationEntity getPerformanceEvaluation(final Long userId) {
        final List<PerformanceEvaluationEntity> evaluations = performanceEvaluationRepo.findAllByUsersId(userId);

        final LocalDate currentDate = LocalDate.now();
        final int month = currentDate.getMonthValue();

        final EvaluationPeriod targetPeriod = month < 7 ? EvaluationPeriod.H2 : EvaluationPeriod.H1;
        final int targetYear = month < 7 ? currentDate.getYear() - 1 : currentDate.getYear();

        return evaluations.stream()
                .filter(eval -> eval.getEvaluationPeriod().equals(targetPeriod) && eval.getCreatedAt().getYear() == targetYear)
                .findFirst()
                .orElse(null);
    }

    public Boolean checkPW(final Long userId, final String currentPassword) {
        final UserEntity user = getUserInfo(userId);

        if (user == null)
            return false;

        return user.getPassword().equals(currentPassword);
    }

    public void validation(BindingResult bindingResult, String fieldName) {
        if (bindingResult.hasFieldErrors(fieldName))
            throw new IllegalArgumentException(Objects.requireNonNull(bindingResult.getFieldError(fieldName)).getDefaultMessage());
    }

    public Boolean validPW(final String newPassword) {
        return newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,30}$");
    }

    public UserEntity updateUser(final UserEntity user) {
        return userRepo.save(user);
    }

    public void validateUnlocked(Long userId, ItemType type, String value) {
        // 기본 아이템(S0, D0, E0)은 항상 사용 가능
        if (isDefaultItem(type, value))
            return;

        if (!unlockStatusRepo.existsByUsersIdAndItemTypeAndItemValue(userId, type, value))
            throw new IllegalArgumentException("잠금 해제되지 않은 아이템입니다.");
    }

    public List<StarResponseDTO.unlockList> unlockLists (final UserEntity user) {
        final List<UnlockStatusEntity> unlock = unlockStatusRepo.findAllByUsers(user);

        return unlock.stream()
                .map(u -> StarResponseDTO.unlockList.builder()
                        .skin(u.getItemValue())
                        .decoration("D0")
                        .effect("E1")
                        .build())
                .toList();
    }

    // ============== private method ================

    private boolean isDefaultItem(ItemType type, String value) {
        return switch (type) {
            case SKIN -> value.equals("S0");
            case DECORATION -> value.equals("D0");
            case EFFECT -> value.equals("E0");
        };
    }
}
