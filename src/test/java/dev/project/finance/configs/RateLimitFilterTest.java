package dev.project.finance.configs;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
        ReflectionTestUtils.setField(rateLimitFilter, "trustProxy", false);
        ReflectionTestUtils.setField(rateLimitFilter, "maxAttempts", 1L);
        ReflectionTestUtils.setField(rateLimitFilter, "windowSeconds", 60L);
        ReflectionTestUtils.setField(rateLimitFilter, "cacheTtlMinutes", 10L);
    }

    @Test
    void aplicaRateLimitNoLoginMesmoComContextPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/app/auth/login");
        request.setContextPath("/app");
        request.setServletPath("/auth/login");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        rateLimitFilter.doFilterInternal(request, response, filterChain);
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        assertEquals(429, response.getStatus());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void naoAplicaRateLimitForaDoLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/refresh");
        request.setServletPath("/auth/refresh");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }
}
