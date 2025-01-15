package pepperstone.backend.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

// FCM 전송 Format DTO
@Getter
@Builder
public class FcmMessageDto {
    private boolean validateOnly; // 메시지 유효성 검사만 수행할지 여부
    private Message message; // 메시지 데이터

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Message {
        private Data notification; // 알림 정보
        private String token; // 수신자 토큰
        private Map<String, String> data; // timestamp를 포함한 data 필드 추가
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Data {
        private String title; // 알림 제목
        private String body; // 알림 내용
        private String image; // 알림에 첨부할 이미지 URL
    }
}