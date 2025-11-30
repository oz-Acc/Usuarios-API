package com.evaluacion.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public record TelefonoRequest(
        @NotBlank String numero,
        @NotBlank String codigoCiudad,
        @NotBlank String codigoPais
) {}
