CREATE TABLE IF NOT EXISTS producto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    precio DOUBLE NOT NULL,
    marca VARCHAR(255) NOT NULL,
    estado VARCHAR(20) DEFAULT 'A',
    fecha_hora_crear TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO producto (nombre, precio, marca, estado) VALUES ('Laptop', 2500.00, 'HP', 'A');
INSERT INTO producto (nombre, precio, marca, estado) VALUES ('Mouse', 150.00, 'Logitech', 'A');
INSERT INTO producto (nombre, precio, marca, estado) VALUES ('Teclado', 300.00, 'Corsair', 'A');
