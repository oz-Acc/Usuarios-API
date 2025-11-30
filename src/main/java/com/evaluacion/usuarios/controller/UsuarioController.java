package com.evaluacion.usuarios.controller;

import com.evaluacion.usuarios.dto.*;
import com.evaluacion.usuarios.model.Usuario;
import com.evaluacion.usuarios.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/usuarios", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Usuarios", description = "Operaciones CRUD sobre usuarios usando DTOs por operación")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Retorna todos los usuarios (requiere JWT)")
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por id", description = "Retorna un usuario específico (requiere JWT)")
    public ResponseEntity<Usuario> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Crear usuario", description = "Crea usuario público sin JWT y genera token inicial")
    public ResponseEntity<Usuario> crear(@Validated @RequestBody UsuarioCreateRequest dto) {
        Usuario nuevo = new Usuario();
        nuevo.setNombre(dto.nombre());
        nuevo.setCorreo(dto.correo());
        nuevo.setContrasena(dto.contrasena());
        if (dto.telefonos() != null) {
            List<com.evaluacion.usuarios.model.Telefono> telefonos = dto.telefonos().stream()
                    .map(t -> {
                        var tel = new com.evaluacion.usuarios.model.Telefono();
                        tel.setNumero(t.numero());
                        tel.setCodigoCiudad(t.codigoCiudad());
                        tel.setCodigoPais(t.codigoPais());
                        tel.setUsuario(nuevo);
                        return tel;
                    })
                    .collect(Collectors.toList());
            nuevo.setTelefonos(telefonos);
        }
        Usuario creado = usuarioService.create(nuevo);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Reemplazar usuario", description = "PUT completo. Contraseña opcional: si se incluye y válida regenera token")
    public ResponseEntity<Usuario> reemplazar(@PathVariable UUID id,
                                              @Validated @RequestBody UsuarioReplaceRequest dto) {
        Usuario datos = new Usuario();
        datos.setNombre(dto.nombre());
        datos.setCorreo(dto.correo());
        datos.setContrasena(dto.contrasena());
        if (dto.telefonos() != null) {
            List<com.evaluacion.usuarios.model.Telefono> telefonos = dto.telefonos().stream()
                    .map(t -> {
                        var tel = new com.evaluacion.usuarios.model.Telefono();
                        tel.setNumero(t.numero());
                        tel.setCodigoCiudad(t.codigoCiudad());
                        tel.setCodigoPais(t.codigoPais());
                        return tel;
                    })
                    .collect(Collectors.toList());
            datos.setTelefonos(telefonos);
        }
        Usuario actualizado = usuarioService.replace(id, datos);
        return ResponseEntity.ok(actualizado);
    }

    @PatchMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Actualizar parcialmente usuario", description = "PATCH parcial. Solo campos presentes cambian; contraseña opcional")
    public ResponseEntity<Usuario> actualizarParcial(@PathVariable UUID id,
                                                     @RequestBody UsuarioPatchRequest dto) {
        Usuario parcial = new Usuario();
        parcial.setNombre(dto.nombre());
        parcial.setCorreo(dto.correo());
        parcial.setContrasena(dto.contrasena());
        if (dto.telefonos() != null) {
            List<com.evaluacion.usuarios.model.Telefono> telefonos = dto.telefonos().stream()
                    .map(t -> {
                        var tel = new com.evaluacion.usuarios.model.Telefono();
                        tel.setNumero(t.numero());
                        tel.setCodigoCiudad(t.codigoCiudad());
                        tel.setCodigoPais(t.codigoPais());
                        return tel;
                    })
                    .collect(Collectors.toList());
            parcial.setTelefonos(telefonos);
        }
        Usuario resultado = usuarioService.update(id, parcial);
        return ResponseEntity.ok(resultado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Borra usuario por id (requiere JWT)")
    public ResponseEntity<java.util.Map<String,String>> eliminar(@PathVariable UUID id) {
        usuarioService.delete(id);
        return ResponseEntity.ok(java.util.Map.of("mensaje", "Usuario eliminado"));
    }
}
