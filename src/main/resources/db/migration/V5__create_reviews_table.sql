CREATE TABLE reviews (
     id BIGSERIAL PRIMARY KEY,
     user_id BIGINT NOT NULL,
     property_id BIGINT NOT NULL,
     booking_id BIGINT NOT NULL,
     rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
     comment TEXT,
     review_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
     FOREIGN KEY (user_id) REFERENCES users(id),
     FOREIGN KEY (property_id) REFERENCES properties(id),
     FOREIGN KEY (booking_id) REFERENCES bookings(id)
);