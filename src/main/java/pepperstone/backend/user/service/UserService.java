package pepperstone.backend.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.repository.UserRepository;

import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {
    private final UserRepository userRepo;

    public void validation(BindingResult bindingResult, String fieldName) {
        if (bindingResult.hasFieldErrors(fieldName))
            throw new IllegalArgumentException(Objects.requireNonNull(bindingResult.getFieldError(fieldName)).getDefaultMessage());
    }

    public UserEntity getByCredentials(final String userId, final String password) {
        if (!userRepo.existsByUserId(userId))
            throw new IllegalArgumentException("잘못된 아이디입니다.");

        UserEntity originalUser = userRepo.findByUserId(userId);

        if(userId.equals(originalUser.getUserId()) && password.equals(originalUser.getPassword())) {
            return originalUser;
        } else if(!userId.equals(originalUser.getUserId())) {
            throw new IllegalArgumentException("잘못된 아이디입니다.");
        } else {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
    }

    public UserEntity getUserInfo(final Long userId) {
        return userRepo.findById(userId).orElse(null);
    }
}
