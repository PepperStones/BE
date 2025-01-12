package pepperstone.backend.notification.service;

import org.springframework.stereotype.Service;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.EvaluationPeriod;
import pepperstone.backend.notification.dto.response.NotificationDto;
import pepperstone.backend.notification.dto.response.NotificationOpenDto;

import java.util.List;
import java.util.Map;

@Service
public interface FcmService {
    void sendPushNotification(String title, String body, String token);
    void saveFcmToken(String token, UserEntity user);
    void sendExperienceNotification(UserEntity user, int experience);
    void sendEvaluationNotification(UserEntity user, EvaluationPeriod period);
    void sendPushChallenge(UserEntity user, String title, String body);
    List<NotificationDto> getNotificationList(UserEntity user);
    NotificationOpenDto openNotification(UserEntity user, Long pushId);
}