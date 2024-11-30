package com.zero.bwtableback.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.reservation.entity.Notification;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.repository.EmitterRepository;
import com.zero.bwtableback.reservation.repository.NotificationRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
public class NotificationEmitterServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmitterRepository emitterRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationEmitterService notificationEmitterService;

    @DisplayName("SSE 연결을 저장하고 lastEventId 기준으로 알림 조회를 시도한다")
    @Test
    void givenMemberIdAndLastEventId_whenSubscribe_thenSavesEmitterAndSendsMissedNotifications() {
        // given
        Long memberId = 1L;
        String lastEventId = "123";
        Long ownerId = 2L;

        Notification notification = createMockNotification(memberId, ownerId);
        mockObjectMapperForJson();
        mockEmitterRepositoryForSubscription(memberId);

        given(notificationRepository.findByReservation_Member_IdAndIdGreaterThanAndNotificationTypeIn(
                eq(memberId), eq(123L), eq(List.of(NotificationType.CONFIRMATION, NotificationType.CANCELLATION))))
                .willReturn(List.of(notification));

        // when
        SseEmitter result = notificationEmitterService.subscribe(memberId, lastEventId);

        // then
        assertThat(result).isNotNull();
        verify(emitterRepository, times(1))
                .saveEmitter(anyString(), any(SseEmitter.class));
        verify(notificationRepository, times(1))
                .findByReservation_Member_IdAndIdGreaterThanAndNotificationTypeIn(
                        eq(memberId), eq(123L), eq(List.of(NotificationType.CONFIRMATION, NotificationType.CANCELLATION)));
    }

    @DisplayName("연결된 emitter를 조회하고 메시지 생성 메서드를 호출한다")
    @Test
    void givenMemberIdAndNotification_whenSendNotificationToConnectedUser_thenCreatesJsonMessageAndSendsToEmitters() {
        // given
        Long memberId = 1L;
        Long ownerId = 2L;
        Notification notification = createMockNotification(memberId, ownerId);
        String emitterId = "1_12345";
        SseEmitter emitter = new SseEmitter(3_600_000L);

        given(emitterRepository.findById(emitterId)).willReturn(emitter);
        given(emitterRepository.findAllEmitterIdsByMemberId(memberId)).willReturn(List.of(emitterId));

        // when
        notificationEmitterService.sendNotificationToConnectedUser(memberId, notification);

        // then
        verify(emitterRepository, times(1)).findAllEmitterIdsByMemberId(memberId);
        verify(emitterRepository, times(1)).findById(emitterId);
    }

    @DisplayName("기본 Last-Event-ID를 설정하고 SSE 연결을 저장한다")
    @Test
    void givenNullOrEmptyLastEventId_whenSubscribe_thenSetsDefaultLastEventId() {
        // given
        Long memberId = 1L;

        // mocking
        doNothing().when(emitterRepository).saveEmitter(anyString(), any(SseEmitter.class));

        // when
        SseEmitter resultForNullId = notificationEmitterService.subscribe(memberId, null);
        SseEmitter resultForEmptyId = notificationEmitterService.subscribe(memberId, "");

        // then
        assertThat(resultForNullId).isNotNull();
        assertThat(resultForEmptyId).isNotNull();
        verify(emitterRepository, times(2)).saveEmitter(anyString(), any(SseEmitter.class));
    }

    private Notification createMockNotification(Long memberId, Long ownerId) {
        Notification notification = mock(Notification.class);
        Reservation reservation = mock(Reservation.class);
        Member guest = mock(Member.class);
        Member owner = mock(Member.class);
        Restaurant restaurant = mock(Restaurant.class);

        given(notification.getReservation()).willReturn(reservation);
        given(reservation.getMember()).willReturn(guest);
        given(guest.getId()).willReturn(memberId);
        given(reservation.getRestaurant()).willReturn(restaurant);
        given(restaurant.getMember()).willReturn(owner);
        given(owner.getId()).willReturn(ownerId);

        return notification;
    }

    private void mockObjectMapperForJson() {
        try {
            given(objectMapper.writeValueAsString(any())).willReturn("{\"message\":\"test\"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mockEmitterRepositoryForSubscription(Long memberId) {
        doNothing().when(emitterRepository).saveEmitter(anyString(), any(SseEmitter.class));
        given(emitterRepository.findAllEmitterIdsByMemberId(memberId)).willReturn(List.of("1_12345"));
    }

}
