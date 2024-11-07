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
    INVALID_BUSINESS_NUMBER_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 사업자 등록번호 형식입니다."),

    MISSING_BUSINESS_NUMBER(HttpStatus.BAD_REQUEST, "사업자등록번호는 사장님 역할에 필수입니다."),

    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),
    PHONE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 전화번호입니다."),
    BUSINESS_NUMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 사업자등록번호입니다."),

    // 인증 관련 오류
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 유효하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // 토큰 관련 오류
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 비어있습니다."),

    // 기타 오류
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String detail;
}