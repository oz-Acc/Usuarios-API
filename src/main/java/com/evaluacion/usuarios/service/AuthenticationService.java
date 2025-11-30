package com.evaluacion.usuarios.service;

import com.evaluacion.usuarios.repository.UsuarioRepository;
import com.evaluacion.usuarios.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthenticationService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthenticationService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(String correo, String contrasena) {
        var found = usuarioRepository.findByCorreo(correo);
        if (found.isPresent() && passwordEncoder.matches(contrasena, found.get().getContrasena())) {
            var u = found.get();
            u.setUltimoLogin(LocalDateTime.now());
            String token = jwtUtil.generateToken(u.getCorreo());
            u.setToken(token);
            usuarioRepository.save(u);
            return token;
        }
        return null;
    }
}
