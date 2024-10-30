package com.zero.bwtableback.common.response;

import lombok.Getter;
import lombok.Setter;

// 에러 정보를 담는 클래스
@Getter
@Setter
public class ApiResponseError {
    private String code;
    private String message;
    private String detail;

    public ApiResponseError(String code, String message, String detail) {
        this.code = code;
        this.message = message;
        this.detail = detail;
    }
}