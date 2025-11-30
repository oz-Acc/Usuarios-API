INSERT INTO usuarios (id, nombre, correo, contrasena, creado, modificado, ultimo_login, activo) VALUES
('11111111-1111-1111-1111-111111111111', 'Juan Rodriguez', 'juan@rodriguez.org', 'hunter2', CURRENT_TIMESTAMP(), NULL, CURRENT_TIMESTAMP(), TRUE),
('22222222-2222-2222-2222-222222222222', 'Ana Perez', 'ana.perez@example.com', 'ana12345', CURRENT_TIMESTAMP(), NULL, CURRENT_TIMESTAMP(), TRUE);

INSERT INTO telefonos (numero, codigo_ciudad, codigo_pais, usuario_id) VALUES
('1234567', '1', '57', '11111111-1111-1111-1111-111111111111'),
('7654321', '2', '57', '22222222-2222-2222-2222-222222222222');
