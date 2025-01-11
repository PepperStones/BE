package pepperstone.backend.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.repository.UserRespository;

import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {
    private final UserRespository userRepo;

    public void validation(BindingResult bindingResult, String fieldName) {
        if (bindingResult.hasFieldErrors(fieldName))
            throw new IllegalArgumentException(Objects.requireNonNull(bindingResult.getFieldError(fieldName)).getDefaultMessage());
    }

    public UserEntity getByCredentials(final String userId, final String password) {
        UserEntity originalUser = userRepo.findByUserId(userId);

        if(originalUser != null && password.equals(originalUser.getPassword())) {
            return originalUser;
        } else if(originalUser == null) {
            throw new IllegalArgumentException("잘못된 아이디입니다.");
        } else {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
    }
}
