package com.evaluacion.usuarios.dto;

import jakarta.validation.constraints.Email;
import java.util.List;

public record UsuarioPatchRequest(
        String nombre,
        @Email String correo,
        String contrasena,
        List<TelefonoRequest> telefonos
) {}
