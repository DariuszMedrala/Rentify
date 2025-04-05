CREATE TABLE images (
    id BIGSERIAL PRIMARY KEY,
    property_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    description TEXT,
    upload_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES properties(id)
);