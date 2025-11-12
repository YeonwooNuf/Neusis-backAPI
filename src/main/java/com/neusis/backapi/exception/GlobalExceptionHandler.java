// src/main/java/com/neusis/backapi/exception/GlobalExceptionHandler.java
package com.neusis.backapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.*;


// 모든 Controller 계층에서 던져진 예외를 한 곳에서 처리한다.
// 예외 유형별로 HTTP 상태코드/응답 바디를 일관되게 반환
// 서비스/레포지토리에서는 예외를 던지기만 하면 컨트롤러에서 try/catch 불필요
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404: 리소스를 찾을 수 없음 (ex: User/Article 미존재)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException e, HttpServletRequest req) {
        // 서비스에서 throw new NotFoundException("USER_NOT_FOUND") 식으로 사용
        return build(HttpStatus.NOT_FOUND, e.getMessage(), req, null);
    }

    // 400: DTO 검증 실패 (@Valid) - 모든 필드 에러 수집
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid")
                ))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "검증 오류", req, errors);
    }

    // 400: JSON 파싱 오류 (본문이 비어있거나 잘못된 포맷)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "요청 본문을 해석할 수 없습니다.", req, null);
    }

    // 400: 파라미터 타입/포맷 오류 (예: ?page=abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        String msg = String.format("잘못된 파라미터 형식입니다. (%s=%s)", e.getName(), e.getValue());
        return build(HttpStatus.BAD_REQUEST, msg, req, null);
    }

    // 400: 필수 쿼리 파라미터 누락 (예: ?size= 없음)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException e, HttpServletRequest req) {
        String msg = String.format("필수 파라미터가 없습니다. (%s)", e.getParameterName());
        return build(HttpStatus.BAD_REQUEST, msg, req, null);
    }

    // 4xx: 비즈니스 규칙 위반(문자열 코드 기반 매핑)
    // 서비스에서 IllegalArgumentException("CODE")로 던지면 여기서 상태코드 매핑
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest req) {
        String code = Optional.ofNullable(e.getMessage()).orElse("BAD_REQUEST");
        HttpStatus status = switch (code) {
            case "인증에 실패하였습니다." -> HttpStatus.UNAUTHORIZED;  // 인증 실패
            case "인증이 필요합니다."        -> HttpStatus.UNAUTHORIZED;  // 인증 필요
            case "이미 존재하는 이메일입니다."-> HttpStatus.CONFLICT;      // 중복 리소스
            case "올바르지 않은 형식의 닉네임입니다."    -> HttpStatus.BAD_REQUEST;   // 입력 규칙 위반
            case "올바르지 않은 형식의 비밀번호입니다."-> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;                       // 기본 400
        };
        return build(status, code, req, null);
    }

    // 409: 데이터 무결성 위반 (유니크 제약/외래키 제약 등)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException e, HttpServletRequest req) {
        log.warn("Data integrity violation", e);
        return build(HttpStatus.CONFLICT, "데이터 무결성 위반(중복/제약 위반)", req, null);
    }

    // 500: 처리되지 않은 예외 (최종 방어막)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception e, HttpServletRequest req) {
        log.error("Unhandled exception", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.", req, null);
    }

    // 공통 응답 포맷 빌더
    // timestamp : 서버 시각
    // status    : HTTP 상태코드 숫자
    // error     : 상태 텍스트(예: "Bad Request")
    // message   : 에러 메시지(또는 비즈니스 코드 문자열)
    // path      : 요청 URI
    // errors    : (옵션) 필드 검증 오류 배열
    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message,
                                                      HttpServletRequest req,
                                                      List<Map<String, String>> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", req.getRequestURI());
        if (errors != null && !errors.isEmpty()) {
            body.put("errors", errors);
        }
        return ResponseEntity.status(status).body(body);
    }
}