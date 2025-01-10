package pepperstone.backend.mypage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import pepperstone.backend.common.entity.PerformanceEvaluationEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.EvaluationPeriod;
import pepperstone.backend.common.repository.PerformanceEvaluationRepository;
import pepperstone.backend.common.repository.UserRespository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
@Service
public class MypageService {
    private final UserRespository userRepo;
    private final PerformanceEvaluationRepository performanceEvaluationRepo;

    public UserEntity getUserInfo(final Long userId) {
        return userRepo.findById(userId).orElse(null);
    }

    public PerformanceEvaluationEntity getPerformanceEvaluation(final Long userId) {
        final List<PerformanceEvaluationEntity> evaluations = performanceEvaluationRepo.findAllByUsersId(userId);

        LocalDate currentDate = LocalDate.now();
        EvaluationPeriod targetPeriod = currentDate.getMonthValue() < 7 ? EvaluationPeriod.H2 : EvaluationPeriod.H1;

        return evaluations.stream()
                .filter(eval -> eval.getEvaluationPeriod().equals(targetPeriod))
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

    public void updatePW(final Long userId, final String newPassword) {
        final UserEntity user =  getUserInfo(userId);

        user.setPassword(newPassword);

        userRepo.save(user);
    }
}
