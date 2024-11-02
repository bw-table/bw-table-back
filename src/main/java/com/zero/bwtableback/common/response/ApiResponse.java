package com.zero.bwtableback.common.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;
    private ApiResponseError error;

    // 데이터가 있는 경우
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("success");
        response.setData(data);
        // fixme 응답 메시지 동적으로 변경 필요
        response.setMessage("요청이 성공적으로 처리되었습니다.");
        return response;
    }

    // 데이터가 없는 경우
    public static ApiResponse<Void> success() {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setStatus("success");
        // fixme 응답 메시지 동적으로 변경 필요
        response.setMessage("요청이 성공적으로 처리되었습니다.");
        return response;
    }

    // 에러 응답 처리
    public static <T> ApiResponse<T> error(String code, String message, String detail) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("fail");
        response.setError(new ApiResponseError(code, message, detail)); // 에러 정보 설정
        return response;
    }
}

