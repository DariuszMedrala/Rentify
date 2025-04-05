CREATE TYPE property_type_enum AS ENUM ('APARTMENT', 'HOUSE', 'STUDIO', 'VILLA', 'LOFT', 'PENTHOUSE');

CREATE TABLE properties (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    property_type property_type_enum NOT NULL,
    area DOUBLE PRECISION,
    number_of_rooms INTEGER,
    price_per_day DECIMAL(10, 2) NOT NULL,
    availability BOOLEAN DEFAULT TRUE,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);