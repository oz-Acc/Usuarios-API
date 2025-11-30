package com.evaluacion.usuarios.controller;

import com.evaluacion.usuarios.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.evaluacion.usuarios.dto.LoginRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Autenticación y emisión de tokens JWT")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Login", description = "Valida credenciales y emite token JWT. Actualiza ultimoLogin y persiste token en usuario.")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest body) {
        String correo = body.correo();
        String contrasena = body.contrasena();

        String token = authenticationService.login(correo, contrasena);
        if (token != null) {
            return ResponseEntity.ok(java.util.Map.of("token", token));
        }

        return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .body(new com.evaluacion.usuarios.dto.ApiError("Credenciales inválidas"));
    }
}
