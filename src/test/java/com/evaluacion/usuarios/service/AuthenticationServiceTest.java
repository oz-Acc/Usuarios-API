package com.evaluacion.usuarios.service;

import com.evaluacion.usuarios.model.Usuario;
import com.evaluacion.usuarios.repository.UsuarioRepository;
import com.evaluacion.usuarios.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Captor
    private ArgumentCaptor<Usuario> usuarioCaptor;

    private Usuario existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new Usuario();
        existingUser.setCorreo("user@mail.com");
        existingUser.setContrasena("ENCODED");
        existingUser.setToken(null);
        existingUser.setUltimoLogin(LocalDateTime.now().minusDays(1));
    }

    @Test
    @DisplayName("login retorna token y persiste cambios cuando credenciales son válidas")
    void login_success_returnsToken_andPersists() {
        when(usuarioRepository.findByCorreo("user@mail.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("raw", "ENCODED")).thenReturn(true);
        when(jwtUtil.generateToken("user@mail.com")).thenReturn("jwt-token");

        String token = authenticationService.login("user@mail.com", "raw");

        assertThat(token).isEqualTo("jwt-token");
        verify(jwtUtil).generateToken("user@mail.com");
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario saved = usuarioCaptor.getValue();
        assertThat(saved.getToken()).isEqualTo("jwt-token");
        assertThat(saved.getUltimoLogin()).isNotNull();
    }

    @Test
    @DisplayName("login retorna null cuando usuario no existe")
    void login_userNotFound_returnsNull() {
        when(usuarioRepository.findByCorreo("missing@mail.com")).thenReturn(Optional.empty());

        String token = authenticationService.login("missing@mail.com", "whatever");

        assertThat(token).isNull();
        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("login retorna null cuando password no coincide")
    void login_passwordMismatch_returnsNull() {
        when(usuarioRepository.findByCorreo("user@mail.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("bad", "ENCODED")).thenReturn(false);

        String token = authenticationService.login("user@mail.com", "bad");

        assertThat(token).isNull();
        verify(usuarioRepository, never()).save(any());
        verify(jwtUtil, never()).generateToken(any());
    }
}
