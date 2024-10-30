package com.zero.bwtableback.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 회원 관련 오류코드
    USER_NOT_FOUND("회원이 존재하지 않습니다."),
    INVALID_CREDENTIALS("이메일 또는 비밀번호가 유효하지 않습니다."),
    EMAIL_ALREADY_REGISTERED("이미 등록된 이메일입니다."),
    PASSWORD_TOO_WEAK("비밀번호는 최소 8자 이상이어야 하며, 숫자와 특수문자를 포함해야 합니다."),
    UNAUTHORIZED_ACCESS("접근 권한이 없습니다."),

    // 기타 오류코드
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.");

    private final String message;
}
