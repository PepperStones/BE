package pepperstone.backend.notification.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import pepperstone.backend.common.entity.FcmEntity;
import pepperstone.backend.common.entity.PushEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.entity.enums.EvaluationPeriod;
import pepperstone.backend.common.repository.FcmRepository;
import pepperstone.backend.common.repository.PushRepository;
import pepperstone.backend.notification.dto.request.FcmMessageDto;
import pepperstone.backend.notification.dto.request.FcmSendDto;
import pepperstone.backend.notification.dto.response.NotificationDto;
import pepperstone.backend.notification.dto.response.NotificationOpenDto;
import pepperstone.backend.notification.service.FcmService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    // FCM 토큰을 관리하는 리포지토리
    private final FcmRepository fcmRepository;
    private final PushRepository pushRepository;

    // 푸시 알림을 보내는 메서드
    @Override
    public void sendPushNotification(String title, String body, String token) {
        try {
            // FCM 전송 객체 생성
            FcmSendDto fcmSendDto = FcmSendDto.builder()
                    .token(token)
                    .title(title)
                    .body(body)
                    .build();
            // 메시지 전송
            sendMessageTo(fcmSendDto);
        } catch (Exception e) {
            // 오류 발생 시 로그 출력
            log.error("Error sending push notification: {}", e.getMessage());
        }
    }

    // FCM 토큰을 저장하는 메서드
    @Override
    public void saveFcmToken(String token, UserEntity user) {
        // 토큰이 중복되지 않으면 저장
        if (fcmRepository.findByToken(token).isEmpty()) {
            FcmEntity fcmToken = FcmEntity.builder()
                    .token(token)
                    .users(user)
                    .build();
            fcmRepository.save(fcmToken);
        }
    }

    // 부여된 경험치를 인자로 받아 해당 유저가 가진 모든 FCM 토큰에 푸시 알림을 전송하는 메서드
    // 직무별 퀘스트 / 리더부여 / 전사 프로젝트
    @Override
    public void sendExperienceNotification(UserEntity user, int experience, int maxScore, int mediumScore, String type) {
        List<String> fcmTokens = fcmRepository.findByUsers(user)
                .stream()
                .map(FcmEntity::getToken)
                .toList();

        String title = "";
        String body = "";
        final String exp_content = "do를 획득하셨습니다. 자세한 내용은 홈 탭 > 최근 획득 경험치에서 확인해보세요.";
        final String end_content = "아쉽게 목표에는 달성하지 못했지만 다음엔 꼭 달성하실 수 있도록 곁에서 응원하겠습니다.";

        if (type.equals("job")) {
            if(experience >= maxScore) {
                title = "직무별 퀘스트 MAX 달성!";
                body = "직무별 퀘스트 MAX를 달성하여 " + experience + exp_content;
            } else if (experience >= mediumScore) {
                title = "직무별 퀘스트 MEDIUM 달성!";
                body = "직무별 퀘스트 MEDIUM을 달성하여 " + experience + exp_content;
            } else {
                title = "직무별 퀘스트 종료";
                body = "직무별 퀘스트 기간이 종료되었습니다. " + end_content;
            }
        } else if (type.equals("leader")) {
            if(experience >= maxScore) {
                title = "리더부여 퀘스트 MAX 달성!";
                body = "리더부여 퀘스트 MAX를 달성하여 " + experience + exp_content;
            } else if (experience >= mediumScore) {
                title = "리더부여 퀘스트 MEDIAN 달성!";
                body = "리더부여 퀘스트 MEDIAN을 달성하여 " + experience + exp_content;
            } else {
                title = "리더부여 퀘스트 종료";
                body = "리더부여 퀘스트 기간이 종료되었습니다. " + end_content;
            }
        } else if (type.equals("project")) {
            title = "전사 프로젝트 완료!";
            body = "전사 프로젝트를 완료하여 " + experience + exp_content;
        }

        int successCount = 0;
        for (String token : fcmTokens) {
            try {
                sendPushNotification(title, body, token);
                successCount++;
            } catch (Exception e) {
                log.error("Error sending experience granted notification to user {}: {}", user.getId(), e.getMessage());
            }
        }

        // 푸시 알림 정보를 PushEntity로 저장
        PushEntity push = new PushEntity();
        push.setUsers(user);
        push.setTitle(title);
        push.setContent(body);
        push.setCreatedAt(LocalDate.now());
        push.setOpen(false); // 기본값 false로 설정

        pushRepository.save(push);
        log.info("푸시 알림 정보 저장 완료: 사용자 ID={}, 제목={}", user.getId(), push.getTitle());

        if (successCount == fcmTokens.size()) {
            log.info("푸시 알림 전송 성공: 사용자 ID={}, 경험치={}do", user.getId(), experience);
        } else {
            log.error("푸시 알림 전송 실패: 사용자 ID={}, 경험치={}do", user.getId(), experience);
        }
    }

    // 인사평가 경험치 알림 전송 메서드
    @Override
    public void sendEvaluationNotification(UserEntity user, EvaluationPeriod period) {
        List<String> fcmTokens = fcmRepository.findByUsers(user)
                .stream()
                .map(FcmEntity::getToken)
                .toList();

        // 알림 제목과 내용 설정
        String title = (period == EvaluationPeriod.H1) ? "상반기 인사평가 완료!" : "하반기 인사평가 완료!";
        String body = (period == EvaluationPeriod.H1)
                ? "상반기 인사평가가 등록되었습니다. 자세한 내용은 홈 탭 > 최근 획득 경험치에서 확인해보세요."
                : "하반기 인사평가가 등록되었습니다. 자세한 내용은 홈 탭 > 최근 획득 경험치에서 확인해보세요.";

        int successCount = 0;
        for (String token : fcmTokens) {
            try {
                sendPushNotification(title, body, token);
                successCount++;
            } catch (Exception e) {
                log.error("Error sending experience granted notification to user {}: {}", user.getId(), e.getMessage());
            }
        }

        // 푸시 알림 정보를 PushEntity로 저장
        PushEntity push = new PushEntity();
        push.setUsers(user);
        push.setTitle(title);
        push.setContent(body);
        push.setCreatedAt(LocalDate.now());
        push.setOpen(false); // 기본값 false로 설정

        pushRepository.save(push);
        log.info("푸시 알림 정보 저장 완료: 사용자 ID={}, 제목={}", user.getId(), push.getTitle());

        if (successCount == fcmTokens.size()) {
            log.info("푸시 알림 전송 성공: 사용자 ID={}, 제목={}", user.getId(), push.getTitle());
        } else {
            log.error("푸시 알림 전송 실패: 사용자 ID={}, 제목={}", user.getId(), push.getTitle());
        }
    }

    // 도전과제 알림 전송 메서드
    @Override
    public void sendPushChallenge(UserEntity user, String title, String body) {
        List<String> fcmTokens = fcmRepository.findByUsers(user)
                .stream()
                .map(fcm -> fcm.getToken())
                .toList();

        int successCount = 0;
        for (String token : fcmTokens) {
            try {
                log.info("token: {}", token);
                sendPushNotification(title, body, token);
                successCount++;
            } catch (Exception e) {
                log.error("Error sending push notification to user {}: {}", user.getId(), e.getMessage());
            }
        }
        // 푸시 알림 정보를 PushEntity로 저장
        PushEntity push = new PushEntity();
        push.setUsers(user);
        push.setTitle(title);
        push.setContent(body);
        push.setCreatedAt(LocalDate.now());
        push.setOpen(false); // 기본값 false로 설정

        pushRepository.save(push);
        log.info("푸시 알림 정보 저장 완료: 사용자 ID={}, 제목={}", user.getId(), push.getTitle());

        if (successCount == fcmTokens.size()) {
            log.info("푸시 알림 전송 성공: 사용자 ID={}", user.getId());
        } else {
            log.warn("푸시 알림 전송 실패: 사용자 ID={}", user.getId());
        }
    }

    // 푸시 알림 리스트 조회 메서드
    @Override
    public List<NotificationDto> getNotificationList(UserEntity user) {
        // 푸시 알림 생성 최신순으로 조히
        List<PushEntity> pushList = pushRepository.findByUsersOrderByCreatedAtDesc(user);

        if (pushList.isEmpty()) {
            throw new IllegalArgumentException("No notifications found.");
        }

        // dto 구성
        return pushList.stream()
                .map(push -> NotificationDto.builder()
                        .pushId(push.getId())
                        .title(push.getTitle())
                        .content(push.getContent())
                        .createdAt(push.getCreatedAt())
                        .open(push.getOpen())
                        .build())
                .collect(Collectors.toList());
    }

    // 푸시 알림 내역 열람 확인 메서드
    @Override
    @Transactional
    public NotificationOpenDto openNotification(UserEntity user, Long pushId) {
        // 푸시 알림 정보 조회
        PushEntity push = pushRepository.findByIdAndUsers(pushId, user)
                .orElseThrow(() -> new RuntimeException("Notification not found."));

        // 이미 열람된 알림인지 확인
        if (push.getOpen()) {
            throw new IllegalArgumentException("Notification has already been opened.");
        }

        // 알림을 열람 상태로 변경
        push.setOpen(true);
        pushRepository.save(push);

        // 응답 데이터 반환
        return NotificationOpenDto.builder()
                .pushId(push.getId())
                .open(push.getOpen())
                .build();
    }

    // ============== private method ================

    // FCM 메시지를 실제로 전송하는 메서드
    private int sendMessageTo(FcmSendDto fcmSendDto) throws IOException {
        // 메시지를 JSON 문자열로 생성
        String message = makeMessage(fcmSendDto);
        log.info("FCM Request message: {}", message);

        // REST 요청을 위한 RestTemplate 객체 생성 및 UTF-8 설정
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 액세스 토큰을 가져와서 Authorization 헤더에 설정
        String token = getAccessToken();
        log.info("Authorization token: Bearer {}", token);
        headers.set("Authorization", "Bearer " + token);

        // HTTP 요청 엔터티 생성
        HttpEntity<String> entity = new HttpEntity<>(message, headers);
        String API_URL = "https://fcm.googleapis.com/v1/projects/pushtest-a8210/messages:send";

        try {
            // FCM API에 POST 요청 보내기
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);
            log.info("FCM Response status: {}", response.getStatusCode());
            log.info("FCM Response body: {}", response.getBody());
            // 성공 여부에 따라 1 또는 0 반환
            return response.getStatusCode() == HttpStatus.OK ? 1 : 0;
        } catch (Exception e) {
            // 예외 발생 시 오류 로그 출력 후 0 반환
            log.error("Error sending FCM message: {}", e.getMessage(), e);
            return 0;
        }
    }

    // Google Firebase Admin SDK를 통해 액세스 토큰을 가져오는 메서드
    private String getAccessToken() throws IOException {
        // Firebase 서비스 계정 키 파일 경로
        String firebaseConfigPath = "firebase/pepperstone-firebase-adminsdk.json";

        // GoogleCredentials 객체를 생성하고 필요한 범위를 설정
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        // 토큰이 만료되었으면 갱신
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    // FCM 메시지 요청을 생성하는 메서드
    private String makeMessage(FcmSendDto fcmSendDto) throws JsonProcessingException {
        // ObjectMapper를 사용해 FCM 메시지 객체를 JSON 문자열로 변환
        ObjectMapper om = new ObjectMapper();
        FcmMessageDto fcmMessageDto = FcmMessageDto
                .builder()
                .message(FcmMessageDto.Message.builder()
                        .token(fcmSendDto.getToken())
                        .data(FcmMessageDto.Data.builder()
                                .title(fcmSendDto.getTitle())
                                .body(fcmSendDto.getBody())
                                .image(null) // 이미지 필드는 null로 설정
                                .build()
                        )
                        .build())
                .validateOnly(false) // 실제로 메시지를 전송하도록 설정
                .build();

        // JSON 문자열 반환
        return om.writeValueAsString(fcmMessageDto);
    }
}
