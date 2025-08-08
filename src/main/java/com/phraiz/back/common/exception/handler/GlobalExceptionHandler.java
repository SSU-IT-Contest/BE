package com.phraiz.back.common.exception.handler;

import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.exception.custom.InvalidRefreshTokenException;
import com.phraiz.back.common.exception.custom.RefreshTokenExpiredException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 1. DTO 유효성 검사 실패 (e.g. @Valid, @Pattern)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        // 필드별 오류 메시지 수집
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        // 응답 구조 생성
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("code", "VALIDATION_ERROR");
        response.put("message", "입력값 검증에 실패했습니다.");
        response.put("errors", fieldErrors);
        response.put("timestamp", LocalDateTime.now());
        response.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    // 2. refresh 토큰 만료
    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleRefreshTokenExpired(RefreshTokenExpiredException ex, HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 403);
        error.put("code", "REFRESH_EXPIRED");
        error.put("message", ex.getMessage());
        error.put("service", "AUTH");
        error.put("path", request.getRequestURI());
        error.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // 3. 유효하지 않다고 판단된 토큰
    @ExceptionHandler({InvalidRefreshTokenException.class})
    public ResponseEntity<Map<String, Object>> handleRefreshTokenErrors(InvalidRefreshTokenException ex, HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 401);
        error.put("code", "INVALID_REFRESH_TOKEN");
        error.put("message", ex.getMessage());
        error.put("path", request.getRequestURI());
        error.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // 4. 라이브러리에서 발생하는 JWT 관련 예외 처리
    @ExceptionHandler({SignatureException.class, JwtException.class})
    public ResponseEntity<Map<String, Object>> handleJwtExceptions(Exception ex, HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 401);
        error.put("code", "INVALID_TOKEN");
        error.put("message", ex.getMessage() != null ? ex.getMessage() : "Invalid JWT token.");
        error.put("path", request.getRequestURI());
        error.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // 5. 나머지 모든 예외 (catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex, HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 500);
        error.put("code", "SYS000");
        error.put("message", "서버 내부 오류가 발생했습니다.");
        error.put("service", "GLOBAL");
        error.put("path", request.getRequestURI()); // 또는 request에서 동적으로 추출 가능
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
