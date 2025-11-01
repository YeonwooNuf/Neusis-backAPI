package com.neusis.backapi.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.*;

// 모든 Controller 계층의 예외를 전역적으로 처리
// 예외별로 다른 HTTP 상태코드 및 JSON 응답 형식을 제공
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404: 리소스를 찾을 수 없음
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 400: DTO 검증 실패 (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        String msg = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    // 400: 파라미터 타입 불일치 (예: ?page=abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String msg = String.format("잘못된 파라미터 형식입니다. (%s=%s)", e.getName(), e.getValue());
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    // 400: 잘못된 요청 (IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 500: 예상치 못한 예외 (NullPointerException 등)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
        e.printStackTrace(); // ⚠️ 개발 단계에서는 로그 남기기
        return build(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    // 공통 응답 포맷 빌더
    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}