package com.zero.bwtableback.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;

    // 데이터가 있는 경우
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("success");
        // fixme 응답 메시지 동적으로 변경 필요
        response.setMessage("요청이 성공적으로 처리되었습니다.");
        response.setData(data);
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
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("error");
        response.setMessage(message);
        return response;
    }
}

