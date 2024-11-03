package com.zero.bwtableback.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice // 전역적으로 예외를 처리할 수 있게 해주는 어노테이션
public class ExceptionController {
    // CustomException 타입의 예외가 발생했을 때 이 메서드가 처리하도록 지정
    @ExceptionHandler({
            CustomException.class
    })
    public ResponseEntity<ExceptionResponse> customRequestException(final CustomException c){
        log.warn("api Exception : {}",c.getErrorCode()); // 발생한 예외의 ErrorCode를 로그로 남김

        return ResponseEntity.badRequest().body(new ExceptionResponse(c.getMessage(),c.getErrorCode().getHttpStatus()));
    }

    @Getter
    @AllArgsConstructor
    public static class ExceptionResponse{
        private String message;
        private HttpStatus httpStatus;
    }
}
