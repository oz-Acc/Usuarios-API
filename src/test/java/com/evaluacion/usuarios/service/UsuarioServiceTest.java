package com.evaluacion.usuarios.service;

import com.evaluacion.usuarios.exception.ResourceNotFoundException;
import com.evaluacion.usuarios.model.Usuario;
import com.evaluacion.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private com.evaluacion.usuarios.security.JwtUtil jwtUtil;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void findAll_shouldReturnAll() {
        when(usuarioRepository.findAll()).thenReturn(List.of(
            new Usuario(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Juan", "juan@example.com", "pwd1", null),
            new Usuario(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Ana", "ana@example.com", "pwd2", null)
        ));

        var result = usuarioService.findAll();

        assertThat(result).hasSize(2);
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    void findById_whenExists_shouldReturn() {
        var u = new Usuario(UUID.fromString("00000000-0000-0000-0000-00000000000a"), "Luis", "luis@example.com", "pwdluis", null);
        var id = UUID.fromString("00000000-0000-0000-0000-00000000000a");
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(u));

        var result = usuarioService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getCorreo()).isEqualTo("luis@example.com");
    }

    @Test
    void findById_whenMissing_shouldThrow() {
        var missing = UUID.fromString("00000000-0000-0000-0000-000000000063");
        when(usuarioRepository.findById(missing)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.findById(missing));
    }

    @Test
    void create_shouldSaveAndReturn() {
        var u = new Usuario("New", "new@example.com", "NewPass1A");
        when(usuarioRepository.findByCorreo(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED_NEWPWD");
        when(jwtUtil.generateToken(anyString())).thenReturn("TOKEN123");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario uarg = invocation.getArgument(0);
            uarg.setId(UUID.fromString("00000000-0000-0000-0000-000000000005"));
            return uarg;
        });

        var result = usuarioService.create(u);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getContrasena()).isEqualTo("ENCODED_NEWPWD");
        assertThat(result.getToken()).isEqualTo("TOKEN123");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void create_withValidPasswordWithDot_shouldSave() {
        var u = new Usuario("DotPass", "dot@example.com", "Hunter123.");
        when(usuarioRepository.findByCorreo(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED_HUNTER");
        when(jwtUtil.generateToken(anyString())).thenReturn("TOKEN_HUNTER");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario uarg = invocation.getArgument(0);
            uarg.setId(UUID.fromString("00000000-0000-0000-0000-000000000007"));
            return uarg;
        });

        var result = usuarioService.create(u);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getContrasena()).isEqualTo("ENCODED_HUNTER");
        assertThat(result.getToken()).isEqualTo("TOKEN_HUNTER");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void create_whenPasswordInvalid_shouldThrow() {
        var u = new Usuario("New", "ok@example.com", "short");
        when(usuarioRepository.findByCorreo("ok@example.com")).thenReturn(Optional.empty());

        assertThrows(com.evaluacion.usuarios.exception.PasswordInvalidException.class, () -> usuarioService.create(u));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void create_whenCorreoExists_shouldThrow() {
        var u = new Usuario("New", "exists@example.com", "NewPass1A");
        when(usuarioRepository.findByCorreo("exists@example.com")).thenReturn(Optional.of(new Usuario(UUID.fromString("00000000-0000-0000-0000-000000000099"), "Exists", "exists@example.com", "pwd", null)));

        assertThrows(com.evaluacion.usuarios.exception.EmailAlreadyRegisteredException.class, () -> usuarioService.create(u));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void replace_shouldSetModificado() {
        var id = UUID.fromString("00000000-0000-0000-0000-000000000010");
        var existing = new Usuario(id, "Nombre", "correo@ex.com", "pwd", null);
        existing.setModificado(null);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(existing));
        when(jwtUtil.generateToken(anyString())).thenReturn("TOKEN_REPLACED");
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED_PWD");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var incoming = new Usuario(id, "Nuevo", "nuevo@ex.com", "PassWord1A", null);
        var result = usuarioService.replace(id, incoming);

        assertThat(result.getModificado()).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Nuevo");
        assertThat(result.getCorreo()).isEqualTo("nuevo@ex.com");
    }

    @Test
    void update_shouldSetModificado() {
        var id = UUID.fromString("00000000-0000-0000-0000-000000000011");
        var existing = new Usuario(id, "Orig", "orig@ex.com", "pwd", null);
        existing.setModificado(null);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(existing));
        when(jwtUtil.generateToken(anyString())).thenReturn("TOKEN_UPDATED");
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED_NEW");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var partial = new Usuario();
        partial.setNombre("Parcial");
        partial.setContrasena("NewPass1A");
        var result = usuarioService.update(id, partial);

        assertThat(result.getModificado()).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Parcial");
        assertThat(result.getToken()).isEqualTo("TOKEN_UPDATED");
    }
}
