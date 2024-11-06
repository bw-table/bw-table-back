package com.zero.bwtableback.reservation.entity;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    private LocalDateTime sentTime;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;


    public void markAsSent() {
        if (this.status == NotificationStatus.SENT) {
            throw new CustomException(ErrorCode.NOTIFICATION_ALREADY_SENT);
        }
        if (LocalDateTime.now().isBefore(this.scheduledTime)) {
            throw new CustomException(ErrorCode.NOTIFICATION_SCHEDULED_TIME_NOT_REACHED);
        }

        this.sentTime = LocalDateTime.now();
        this.status = NotificationStatus.SENT;
    }

}
