package com.evaluacion.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload para login de usuario")
public record LoginRequest(
        @Schema(description = "Correo electrónico de usuario", example = "juan@rodriguez.org")
        @NotBlank @Email String correo,
        @Schema(description = "Contraseña en texto plano a validar", example = "Password1!")
        @NotBlank String contrasena
) {}
