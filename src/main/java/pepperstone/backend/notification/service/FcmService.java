package pepperstone.backend.notification.service;

import org.springframework.stereotype.Service;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.EvaluationPeriod;

@Service
public interface FcmService {
    void sendPushNotification(String title, String body, String token);
    void saveFcmToken(String token, UserEntity user);
    void sendExperienceNotification(UserEntity user, int experience);
    void sendEvaluationNotification(UserEntity user, EvaluationPeriod period);
    void sendPushChallenge(UserEntity user, String title, String content);
}