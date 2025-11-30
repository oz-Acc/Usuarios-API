package com.evaluacion.usuarios.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String TEST_USERNAME = "testuser@example.com";
    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.jwt.token";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotFilter_SwaggerUiPath_ShouldReturnTrue() throws ServletException {
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        assertTrue(result, "Swagger UI paths should skip JWT filter");
    }

    @Test
    void shouldNotFilter_OpenApiDocsPath_ShouldReturnTrue() throws ServletException {
        when(request.getRequestURI()).thenReturn("/v3/api-docs");

        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        assertTrue(result, "OpenAPI docs paths should skip JWT filter");
    }

    @Test
    void shouldNotFilter_H2ConsolePath_ShouldReturnTrue() throws ServletException {
        when(request.getRequestURI()).thenReturn("/h2-console");

        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        assertTrue(result, "H2 console paths should skip JWT filter");
    }

    @Test
    void shouldNotFilter_WebjarsPath_ShouldReturnTrue() throws ServletException {
        when(request.getRequestURI()).thenReturn("/webjars/swagger-ui/index.js");

        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        assertTrue(result, "Webjars paths should skip JWT filter");
    }

    @Test
    void shouldNotFilter_PostCreateUser_ShouldReturnTrue() throws ServletException {
        when(request.getRequestURI()).thenReturn("/api/usuarios");
        when(request.getMethod()).thenReturn("POST");

        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        assertTrue(result, "POST /api/usuarios should skip JWT filter");
    }

    @Test
    void shouldNotFilter_PostLogin_ShouldReturnTrue() throws ServletException {
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getMethod()).thenReturn("POST");

        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        assertTrue(result, "POST /auth/login should skip JWT filter");
    }

    @Test
    void shouldNotFilter_GetUsuarios_ShouldReturnFalse() throws ServletException {
        when(request.getRequestURI()).thenReturn("/api/usuarios");
        when(request.getMethod()).thenReturn("GET");

        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        assertFalse(result, "GET /api/usuarios should NOT skip JWT filter");
    }

    @Test
    void shouldNotFilter_ProtectedEndpoint_ShouldReturnFalse() throws ServletException {
        when(request.getRequestURI()).thenReturn("/api/usuarios/123");
        when(request.getMethod()).thenReturn("GET");

        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        assertFalse(result, "Protected endpoints should NOT skip JWT filter");
    }

    @Test
    void shouldNotFilter_NullPath_ShouldReturnFalse() throws ServletException {
        when(request.getRequestURI()).thenReturn(null);

        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        assertFalse(result, "Null path should NOT skip JWT filter");
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        String authHeader = "Bearer " + VALID_TOKEN;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.getUsername(VALID_TOKEN)).thenReturn(TEST_USERNAME);

        UserDetails userDetails = User.builder()
                .username(TEST_USERNAME)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication(), 
            "Authentication should be set in SecurityContext");
        assertEquals(TEST_USERNAME, SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        String authHeader = "Bearer " + INVALID_TOKEN;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.validateToken(INVALID_TOKEN)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), 
            "Authentication should NOT be set for invalid token");
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).getUsername(anyString());
    }

    @Test
    void doFilterInternal_WithoutAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), 
            "Authentication should NOT be set without Authorization header");
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void doFilterInternal_WithInvalidAuthorizationFormat_ShouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), 
            "Authentication should NOT be set for invalid Authorization format");
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void doFilterInternal_WithEmptyToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), 
            "Authentication should NOT be set for empty token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithValidTokenButNullUsername_ShouldNotSetAuthentication() throws ServletException, IOException {
        String authHeader = "Bearer " + VALID_TOKEN;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.getUsername(VALID_TOKEN)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), 
            "Authentication should NOT be set when username is null");
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void doFilterInternal_WithExistingAuthentication_ShouldNotOverride() throws ServletException, IOException {
        String authHeader = "Bearer " + VALID_TOKEN;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.getUsername(VALID_TOKEN)).thenReturn(TEST_USERNAME);

        UserDetails existingUser = User.builder()
                .username("existing@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        existingUser, null, existingUser.getAuthorities()));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals("existing@example.com", 
            SecurityContextHolder.getContext().getAuthentication().getName(),
            "Existing authentication should NOT be overridden");
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void doFilterInternal_AlwaysCallsFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldSetAuthenticationDetails() throws ServletException, IOException {
        String authHeader = "Bearer " + VALID_TOKEN;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.getUsername(VALID_TOKEN)).thenReturn(TEST_USERNAME);

        UserDetails userDetails = User.builder()
                .username(TEST_USERNAME)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication().getDetails(),
            "Authentication details should be set");
        verify(filterChain).doFilter(request, response);
    }
}
