package com.zero.bwtableback.reservation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.reservation.entity.Notification;
import com.zero.bwtableback.reservation.repository.EmitterRepository;
import com.zero.bwtableback.reservation.repository.NotificationRepository;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationEmitterService {

    private final ObjectMapper objectMapper;
    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;

    // 사용자가 서버와 SSE 연결을 설정할 때 호출
    public SseEmitter subscribe(Long memberId, String lastEventId) {
        String emitterId = memberId + "_" + System.currentTimeMillis();
        SseEmitter emitter = new SseEmitter(3_600_000L); // 기본 타임아웃 한 시간으로 설정

        emitterRepository.saveEmitter(emitterId, emitter);

        // 마지막 알림 이후의 알림부터 전송
        long lastEventIdLong = parseLastEventId(lastEventId);
        if (lastEventIdLong > 0) {
            List<Notification> missedNotifications = notificationRepository
                    .findByReservation_Member_IdAndIdGreaterThan(memberId, lastEventIdLong);
            missedNotifications.forEach(notification -> sendNotificationToConnectedUser(memberId, notification));
        }

        emitter.onCompletion(() -> emitterRepository.removeEmitter(emitterId));
        emitter.onTimeout(() -> emitterRepository.removeEmitter(emitterId));

        return emitter;
    }

    // 동일한 알림을 고객과 가게 주인에게 모두 전송
    public void sendNotificationToCustomerAndOwner(Long customerId, Long ownerId, Notification notification) {
        sendNotificationToConnectedUser(customerId, notification);
        sendNotificationToConnectedUser(ownerId, notification);
    }

    // 회원의 활성화된 emitter에 알림 전송
    public void sendNotificationToConnectedUser(Long memberId, Notification notification) {
        List<String> emitterIds = emitterRepository.findAllEmitterIdsByMemberId(memberId);
        String jsonMessage = createNotificationMessage(notification);

        emitterIds.forEach(emitterId -> {
            SseEmitter emitter = emitterRepository.findById(emitterId);
            if (emitter != null) {
                sendMessageByEmitter(emitter, emitterId, jsonMessage);
            }
        });
    }

    // 해당 emitter로 메시지 전송
    private void sendMessageByEmitter(SseEmitter emitter, String emitterId, String message) {
        try {
            emitter.send(SseEmitter.event()
                    .name("reservation-notification")
                    .id(emitterId) // 이벤트 ID를 emitterId로 설정
                    .data(message));
        } catch (Exception e) {
            log.error("알림 전송이 실패했습니다. {}: {}", emitterId, e.getMessage());
            emitter.completeWithError(e); // 연결 종료
            emitterRepository.removeEmitter(emitterId); // emitter 제거
        }
    }

    // 전달 받은 마지막 알림 아이디가 있으면 파싱하고 없으면 0으로 설정
    private long parseLastEventId(String lastEventId) {
        if (lastEventId != null && !lastEventId.isEmpty()) {
            return Long.parseLong(lastEventId);
        }
        return 0L;
    }

    // Notification 객체를 JSON 문자열로 변환하는 메서드
    private String createNotificationMessage(Notification notification) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("message", notification.getMessage());
        notificationData.put("customerId", notification.getReservation().getMember().getId());
        notificationData.put("ownerId", notification.getReservation().getRestaurant().getMember().getId());
        notificationData.put("reservationId", notification.getReservation().getId());
        notificationData.put("status", notification.getStatus());

        try {
            return objectMapper.writeValueAsString(notificationData);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.JSON_PARSING_FAILED);
        }
    }
}