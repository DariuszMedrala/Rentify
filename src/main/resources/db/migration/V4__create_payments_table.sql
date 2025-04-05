CREATE TYPE payment_method_enum AS ENUM ('CREDIT_CARD', 'PAYPAL', 'BANK_TRANSFER', 'CASH');
CREATE TYPE payment_status_enum AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED');

CREATE TABLE payments (
  id BIGSERIAL PRIMARY KEY,
  booking_id BIGINT NOT NULL UNIQUE,
  payment_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  payment_method payment_method_enum NOT NULL,
  payment_status payment_status_enum NOT NULL,
  transaction_id VARCHAR(255),
  FOREIGN KEY (booking_id) REFERENCES bookings(id)
);