package com.phraiz.back.common.util;

import com.phraiz.back.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.server.ServerHttpRequest;

// 로그 작성 Util
public class LogUtil {

    public static void logError(Logger logger, HttpServletRequest request, ErrorCode errorCode, Throwable ex) {
        if (ex != null) {
            Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);
            logger.error("[❌ ERROR {}] {} {} | code={}, message={}, exception={}, , rootCause={} - {}",
                    errorCode.getService(),
                    request.getMethod(),
                    request.getRequestURI(),
                    errorCode.getCode(),
                    errorCode.getMessage(),
                    ex.getMessage(),
                    (root != null ? root.getClass().getSimpleName() : "n/a"),
                    (root != null ? root.getMessage() : "n/a"),
                    ex // 전체 스택 출력
            );
        } else {
            logger.error("[❌ ERROR {}] {} {} | code={}, message={}",
                    errorCode.getService(),
                    request.getMethod(),
                    request.getRequestURI(),
                    errorCode.getCode(),
                    errorCode.getMessage()
            );
        }
    }

    public static void logWarn(Logger logger, HttpServletRequest request, ErrorCode errorCode, Throwable ex) {
        if (ex != null) {
            logger.warn("[❗ WARN {}] {} {} | code={}, message={}, exception={}",
                    errorCode.getService(),
                    request.getMethod(),
                    request.getRequestURI(),
                    errorCode.getCode(),
                    errorCode.getMessage(),
                    ex.getMessage()
            );
        } else {
            logger.warn("[❗ WARN {}] {} {} | code={}, message={}",
                    errorCode.getService(),
                    request.getMethod(),
                    request.getRequestURI(),
                    errorCode.getCode(),
                    errorCode.getMessage()
            );
        }
    }

    public static void logRequestDuration(Logger logger, ServerHttpRequest request, long duration) {
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        String uri = request.getURI().getPath();

        if (duration > 1000) {
            logger.warn("[SLOW API] {} {} took {}ms", method, uri, duration);
        } else {
            logger.info("[GATEWAY] {} {} took {}ms", method, uri, duration);
        }
    }

}
