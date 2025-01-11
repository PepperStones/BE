package pepperstone.backend.notification.service;

import org.springframework.stereotype.Service;
import pepperstone.backend.common.entity.UserEntity;

@Service
public interface FcmService {
    void sendPushNotification(String title, String body, String token);
    void saveFcmToken(String token, UserEntity user);
}