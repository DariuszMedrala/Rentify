CREATE TYPE booking_status_enum AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED');


CREATE TABLE bookings (
  id BIGSERIAL PRIMARY KEY,
  property_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  total_price DECIMAL(10, 2) NOT NULL,
  booking_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  booking_status booking_status_enum NOT NULL,
  FOREIGN KEY (property_id) REFERENCES properties(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);