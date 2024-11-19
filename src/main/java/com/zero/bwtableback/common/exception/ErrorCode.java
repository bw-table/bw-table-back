package com.zero.bwtableback.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 회원 관련 오류
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 이메일 형식입니다."),
    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 닉네임입니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호는 최소 8자 이상이며 대문자, 소문자, 숫자 및 특수문자를 포함해야 합니다."),
    INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "유효하지 않은 전화번호 형식입니다."),
    INVALID_BUSINESS_NUMBER(HttpStatus.BAD_REQUEST, "유효하지 않은 사업자등록번호 형식입니다."),

    MISSING_BUSINESS_NUMBER(HttpStatus.BAD_REQUEST, "사업자등록번호는 사장님 역할에 필수입니다."),

    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 회원입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),
    PHONE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 전화번호입니다."),
    BUSINESS_NUMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 사업자등록번호입니다."),

    // 인증 관련 오류
    INVALID_CREDENTIALS(HttpStatus.FORBIDDEN, "이메일 또는 비밀번호가 유효하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // 토큰 관련 오류
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    // 예약 생성 관련 오류
    INVALID_PEOPLE_COUNT(HttpStatus.BAD_REQUEST, "인원 설정은 최소 한 명입니다."),
    INVALID_RESERVATION_DATE(HttpStatus.BAD_REQUEST, "현재보다 과거의 날짜는 예약이 불가합니다."),
    INVALID_RESERVATION_TIME(HttpStatus.BAD_REQUEST, "현재보다 과거의 시간은 예약이 불가합니다."),

    // 예약 설정 관련 에러 코드
    RESERVATION_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 레스토랑의 유효한 예약 설정을 찾을 수 없습니다."),
    WEEKDAY_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 날짜의 요일 설정을 찾을 수 없습니다."),
    TIMESLOT_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 시간대의 예약 설정을 찾을 수 없습니다."),

    // 예약 내역 조회 관련 오류
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 예약을 찾을 수 없습니다."),
    RESTAURANT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 가게를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."),
    RESERVATION_FULL(HttpStatus.CONFLICT, "해당 시간대 예약이 마감되었습니다."),

    // 예약 상태 변경 관련 오류
    INVALID_RESERVATION_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 예약 상태입니다."),
    INVALID_STATUS_CONFIRM(HttpStatus.CONFLICT, "이미 확정되었거나, 취소, 노쇼, 방문 완료된 예약은 다시 확정할 수 없습니다."),
    INVALID_STATUS_CUSTOMER_CANCEL(HttpStatus.CONFLICT, "고객 취소는 확정된 예약에 대해서만 가능합니다."),
    INVALID_STATUS_OWNER_CANCEL(HttpStatus.CONFLICT, "가게 취소는 확정된 예약에 대해서만 가능합니다."),
    INVALID_STATUS_NO_SHOW(HttpStatus.CONFLICT, "노쇼로 변경은 확정된 예약에 대해서만 가능합니다."),
    INVALID_STATUS_VISITED(HttpStatus.CONFLICT, "방문 완료로 변경은 확정된 예약에 대해서만 가능합니다."),
    CUSTOMER_CANCEL_TOO_LATE(HttpStatus.FORBIDDEN, "예약일 3일 전까지만 취소할 수 있습니다."),

    // 채팅 관련 오류
    CHAT_ROOM_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_INACTIVE(HttpStatus.BAD_REQUEST, "해당 채팅방은 비활성화 되었습니다."),

    // 예약 알림 관련 오류
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),
    NOTIFICATION_ALREADY_SENT(HttpStatus.BAD_REQUEST, "이미 전송된 알림입니다."),
    NOTIFICATION_SCHEDULED_TIME_NOT_REACHED(HttpStatus.BAD_REQUEST, "예정된 전송 시간이 아닙니다."),
    NOTIFICATION_SEND_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "알림 전송에 실패했습니다."),
    JSON_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 데이터 JSON 파싱 작업이 실패했습니다."),

    // 결제 관련 오류
    PAYMENT_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제 처리 중 오류가 발생했습니다. 다시 시도해 주세요."),

    // 기타 오류
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String detail;
}
