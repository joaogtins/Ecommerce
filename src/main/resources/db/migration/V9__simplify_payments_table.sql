ALTER TABLE payments DROP COLUMN mercadopago_payment_id;
ALTER TABLE payments DROP COLUMN qr_code;
ALTER TABLE payments DROP COLUMN qr_code_base64;
DROP INDEX IF EXISTS idx_payments_mercadopago_id;
ALTER TABLE payments ADD COLUMN confirmed_by VARCHAR(255);
ALTER TABLE payments ADD COLUMN confirmed_at TIMESTAMP;
