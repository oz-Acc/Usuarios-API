package com.evaluacion.usuarios.service;

import com.evaluacion.usuarios.exception.ResourceNotFoundException;
import com.evaluacion.usuarios.model.Usuario;
import com.evaluacion.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.evaluacion.usuarios.security.JwtUtil jwtUtil;

    @org.springframework.beans.factory.annotation.Value("${security.password.regex:^(?=.{8,}$)(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)[A-Za-z0-9.@_/!*&#$%^(){}\\[\\]:-]{8,}$}")
    private String passwordRegex;

    @org.springframework.beans.factory.annotation.Value("${security.password.message:La contraseña no cumple el patrón requerido}")
    private String passwordErrorMessage;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, com.evaluacion.usuarios.security.JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    private static final String DEFAULT_PASSWORD_REGEX = "^(?=.{8,}$)(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)[A-Za-z0-9.@_/!*&#$%^(){}\\[\\]:-]{8,}$";

    private String getEffectivePasswordRegex() {
        return (passwordRegex != null && !passwordRegex.isBlank()) ? passwordRegex : DEFAULT_PASSWORD_REGEX;
    }

    private String validateAndEncodePassword(String rawPassword) {
        if (rawPassword == null) return null;
        String pw = rawPassword.trim();

        java.util.regex.Pattern p = java.util.regex.Pattern.compile(getEffectivePasswordRegex());
        if (!p.matcher(pw).matches()) {
            String err = (passwordErrorMessage != null && !passwordErrorMessage.isBlank()) ? passwordErrorMessage : "La contraseña no cumple el patrón requerido";
            throw new com.evaluacion.usuarios.exception.PasswordInvalidException(err);
        }
        return passwordEncoder.encode(pw);
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con id " + id + " no encontrado"));
    }

    public Usuario create(Usuario usuario) {
        if (usuario.getCorreo() != null && usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            throw new com.evaluacion.usuarios.exception.EmailAlreadyRegisteredException("El correo ya está registrado");
        }
        LocalDateTime now = LocalDateTime.now();
        usuario.setCreado(now);
        usuario.setUltimoLogin(now);
        usuario.setModificado(now);
        if (usuario.getContrasena() != null) {
            usuario.setContrasena(usuario.getContrasena().trim());

            usuario.setContrasena(validateAndEncodePassword(usuario.getContrasena()));
        }
        String token = jwtUtil.generateToken(usuario.getCorreo());
        usuario.setToken(token);
        if (usuario.getTelefonos() != null) {
            usuario.getTelefonos().forEach(t -> t.setUsuario(usuario));
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario replace(UUID id, Usuario datos) {
        Usuario usuario = findById(id);
        usuario.setNombre(datos.getNombre());
        usuario.setCorreo(datos.getCorreo());
        if (datos.getContrasena() != null) {
            usuario.setContrasena(validateAndEncodePassword(datos.getContrasena()));
            usuario.setToken(jwtUtil.generateToken(usuario.getCorreo()));
        }

        usuario.getTelefonos().clear();
        if (datos.getTelefonos() != null) {
            datos.getTelefonos().forEach(t -> t.setUsuario(usuario));
            usuario.getTelefonos().addAll(datos.getTelefonos());
        }
        usuario.setModificado(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    public Usuario update(UUID id, Usuario datosParciales) {
        Usuario usuarioExistente = findById(id);
        if (datosParciales.getNombre() != null) {
            usuarioExistente.setNombre(datosParciales.getNombre());
        }
        if (datosParciales.getCorreo() != null) {
            usuarioExistente.setCorreo(datosParciales.getCorreo());
        }
        if (datosParciales.getContrasena() != null) {
            usuarioExistente.setContrasena(validateAndEncodePassword(datosParciales.getContrasena()));
            usuarioExistente.setToken(jwtUtil.generateToken(usuarioExistente.getCorreo()));
        }
        if (datosParciales.getTelefonos() != null) {
            usuarioExistente.getTelefonos().clear();
            datosParciales.getTelefonos().forEach(t -> t.setUsuario(usuarioExistente));
            usuarioExistente.getTelefonos().addAll(datosParciales.getTelefonos());
        }
        usuarioExistente.setModificado(LocalDateTime.now());
        return usuarioRepository.save(usuarioExistente);
    }

    public void delete(UUID id) {
        Usuario usuario = findById(id);
        usuarioRepository.delete(usuario);
    }
}
