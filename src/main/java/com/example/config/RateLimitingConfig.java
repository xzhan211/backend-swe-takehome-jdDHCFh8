package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class RateLimitingConfig {

    @Value("${rate.limiting.enabled:true}")
    private boolean rateLimitingEnabled;
    
    @Value("${rate.limiting.requests-per-minute:60}")
    private int requestsPerMinute;
    
    @Value("${rate.limiting.requests-per-hour:1000}")
    private int requestsPerHour;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Bean
    public RateLimitingFilter rateLimitingFilter() {
        // Disable rate limiting for test profile
        boolean enabled = rateLimitingEnabled && !"test".equals(activeProfile);
        return new RateLimitingFilter(enabled, requestsPerMinute, requestsPerHour);
    }
    

    public static class RateLimitingFilter extends OncePerRequestFilter {
        
        private final boolean enabled;
        private final int maxRequestsPerMinute;
        private final int maxRequestsPerHour;
        
        // Store request counts per IP address
        private final ConcurrentHashMap<String, ClientRequestInfo> clientRequests = new ConcurrentHashMap<>();
        
        public RateLimitingFilter(boolean enabled, int maxRequestsPerMinute, int maxRequestsPerHour) {
            this.enabled = enabled;
            this.maxRequestsPerMinute = maxRequestsPerMinute;
            this.maxRequestsPerHour = maxRequestsPerHour;
        }
        
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            // Skip rate limiting if disabled
            if (!enabled) {
                filterChain.doFilter(request, response);
                return;
            }
            
            String clientIp = getClientIpAddress(request);
            String endpoint = request.getRequestURI();
            
            // Skip rate limiting for health check endpoints
            if (endpoint.contains("/actuator") || endpoint.contains("/h2-console")) {
                filterChain.doFilter(request, response);
                return;
            }
            
            ClientRequestInfo clientInfo = clientRequests.computeIfAbsent(clientIp, 
                k -> new ClientRequestInfo());
            
            LocalDateTime now = LocalDateTime.now();
            
            // Check minute-based rate limit
            if (!clientInfo.canMakeRequest(now, maxRequestsPerMinute, 60)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Rate limit exceeded. Maximum " + 
                    maxRequestsPerMinute + " requests per minute allowed.\"}");
                return;
            }
            
            // Check hour-based rate limit
            if (!clientInfo.canMakeRequest(now, maxRequestsPerHour, 3600)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Rate limit exceeded. Maximum " + 
                    maxRequestsPerHour + " requests per hour allowed.\"}");
                return;
            }
            
            // Record the request
            clientInfo.recordRequest(now);
            
            filterChain.doFilter(request, response);
        }
        
        private String getClientIpAddress(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }
            
            return request.getRemoteAddr();
        }
        
        // Clean up old entries periodically (simple cleanup)
        private void cleanupOldEntries() {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
            clientRequests.entrySet().removeIf(entry -> 
                entry.getValue().getLastRequestTime().isBefore(cutoff));
        }
        
    }
    
    private static class ClientRequestInfo {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private LocalDateTime windowStart;
        private LocalDateTime lastRequestTime;
        
        public ClientRequestInfo() {
            this.windowStart = LocalDateTime.now();
            this.lastRequestTime = LocalDateTime.now();
        }
        
        public boolean canMakeRequest(LocalDateTime now, int maxRequests, int windowSeconds) {
            // Reset window if it has expired
            if (windowStart.plusSeconds(windowSeconds).isBefore(now)) {
                requestCount.set(0);
                windowStart = now;
            }
            
            return requestCount.get() < maxRequests;
        }
        
        public void recordRequest(LocalDateTime now) {
            requestCount.incrementAndGet();
            lastRequestTime = now;
        }
        
        public LocalDateTime getLastRequestTime() {
            return lastRequestTime;
        }
    }
}
