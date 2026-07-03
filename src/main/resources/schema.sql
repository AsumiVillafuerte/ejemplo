CREATE TABLE IF NOT EXISTS personas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    estado VARCHAR(20) DEFAULT 'ACTIVO',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO personas (nombre, apellido, email, estado)
VALUES ('Asumi', 'Villafuerte', 'asumi@gmail.com', 'ACTIVO');

INSERT INTO personas (nombre, apellido, email, estado)
VALUES ('Juan', 'Perez', 'juan@gmail.com', 'ACTIVO');