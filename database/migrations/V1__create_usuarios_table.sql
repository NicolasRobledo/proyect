CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    google_id VARCHAR(255),
    email VARCHAR(255),
    email_verified BOOLEAN,
    nombre VARCHAR(255),
    nombre_pila VARCHAR(100),
    apellido VARCHAR(100),
    foto_url VARCHAR(500),
    locale VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
