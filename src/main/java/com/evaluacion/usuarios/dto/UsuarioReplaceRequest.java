package com.evaluacion.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record UsuarioReplaceRequest(
        @NotBlank String nombre,
        @NotBlank @Email String correo,
        String contrasena,
        List<TelefonoRequest> telefonos
) {}
