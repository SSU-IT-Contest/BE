package com.phraiz.back.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final Pattern FORWARDED_FOR = Pattern.compile("for=\"?([^;,\"]+)");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                    .withZone(ZoneId.systemDefault());

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();

        String method = req.getMethod();
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        String ip = clientIp(req);
        String ua = Optional.ofNullable(req.getHeader("User-Agent")).orElse("-");

        Instant requestTime = Instant.now();

        try {
            chain.doFilter(req, res);
        } finally {
            long took = System.currentTimeMillis() - start;
            int status = res.getStatus();
            String ts = FORMATTER.format(requestTime);
            // 예: ip=1.2.3.4 method=POST uri=/login status=200 took=32ms ua="Mozilla/5.0 ..."
            log.info("time={} ip={} method={} uri={}{} status={} took={}ms ua=\"{}\"",
                    ts, ip, method, uri, (query != null ? "?" + query : ""), status, took, ua);
        }
    }

    private String clientIp(HttpServletRequest req) {
        // 1) X-Forwarded-For (프록시 체인일 경우 "client, proxy1, proxy2" 형태 → 첫 번째가 원 IP)
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        // 2) Nginx에서 자주 쓰는 헤더
        String xri = req.getHeader("X-Real-IP");
        if (xri != null && !xri.isEmpty()) return xri;

        // 3) RFC Forwarded
        String fwd = req.getHeader("Forwarded");
        if (fwd != null) {
            Matcher m = FORWARDED_FOR.matcher(fwd);
            if (m.find()) return m.group(1);
        }
        // 4) 프록시가 없다면 WAS가 보는 원격 IP
        return req.getRemoteAddr();
    }
}