package com.example.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.profiles.active=test"})
public class RateLimitingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    void testRateLimitingDisabledInTestProfile() {
        // Test that rate limiting is disabled in test profile
        // Make many requests quickly - they should all succeed
        for (int i = 0; i < 20; i++) {
            ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/players", String.class);
            
            assertTrue(response.getStatusCode().is2xxSuccessful(),
                "Request " + (i + 1) + " should be successful (rate limiting disabled in test profile)");
        }
    }

    @Test
    void testRateLimitingDisabledForHighVolumeRequests() {
        // Test that rate limiting is disabled even for high volume requests
        int successCount = 0;
        int rateLimitedCount = 0;
        
        // Make 150 requests quickly - all should succeed since rate limiting is disabled
        for (int i = 0; i < 150; i++) {
            ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/players", String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                successCount++;
            } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                rateLimitedCount++;
            }
        }
        
        // All requests should succeed since rate limiting is disabled in test profile
        assertEquals(150, successCount, "All requests should succeed (rate limiting disabled)");
        assertEquals(0, rateLimitedCount, "No requests should be rate limited");
        
        System.out.println("Successful requests: " + successCount);
        System.out.println("Rate limited requests: " + rateLimitedCount);
    }

    @Test
    void testRateLimitingDisabledAcrossDifferentEndpoints() {
        // Test that rate limiting is disabled across different endpoints
        String[] endpoints = {
            "/api/players",
            "/api/games",
            "/api/players/leaderboard"
        };
        
        int totalRequests = 0;
        int successfulRequests = 0;
        int rateLimitedRequests = 0;
        
        // Make requests to different endpoints
        for (String endpoint : endpoints) {
            for (int i = 0; i < 25; i++) { // 25 requests per endpoint = 75 total
                ResponseEntity<String> response = restTemplate.getForEntity(
                    baseUrl + endpoint, String.class);
                
                totalRequests++;
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    successfulRequests++;
                } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    rateLimitedRequests++;
                }
            }
        }
        
        System.out.println("Total requests made: " + totalRequests);
        System.out.println("Successful requests: " + successfulRequests);
        System.out.println("Rate limited requests: " + rateLimitedRequests);
        
        // All requests should succeed since rate limiting is disabled
        assertEquals(totalRequests, successfulRequests, "All requests should succeed");
        assertEquals(0, rateLimitedRequests, "No requests should be rate limited");
    }

    @Test
    void testExcludedEndpointsStillWorkWhenRateLimitingDisabled() {
        // Test that excluded endpoints still work when rate limiting is disabled
        String[] excludedEndpoints = {
            "/h2-console",
            "/actuator/health"
        };
        
        // These endpoints should not be rate limited (and rate limiting is disabled anyway)
        for (String endpoint : excludedEndpoints) {
            // Make multiple requests to excluded endpoints
            for (int i = 0; i < 10; i++) {
                ResponseEntity<String> response = restTemplate.getForEntity(
                    baseUrl + endpoint, String.class);
                
                // Should not get rate limited (might get 404 or other errors, but not 429)
                assertNotEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode(),
                    "Excluded endpoint " + endpoint + " should not be rate limited");
            }
        }
    }

    @Test
    void testRateLimitingDisabledConsistentlyOverTime() throws InterruptedException {
        // This test verifies that rate limiting remains disabled consistently over time
        
        // Make many requests initially
        int initialSuccessCount = 0;
        for (int i = 0; i < 50; i++) {
            ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/players", String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                initialSuccessCount++;
            }
        }
        
        assertEquals(50, initialSuccessCount, "All initial requests should succeed");
        
        // Wait a bit and try again
        Thread.sleep(2000); // Wait 2 seconds
        
        // Make more requests after waiting
        int successAfterWait = 0;
        for (int i = 0; i < 50; i++) {
            ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/players", String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                successAfterWait++;
            }
        }
        
        // All requests should still succeed since rate limiting is disabled
        assertEquals(50, successAfterWait, 
            "All requests after waiting should still succeed (rate limiting disabled)");
    }
}
