-- Add shipping_price column to products table
-- Allows the seller to set a shipping price per product (per destination city)
ALTER TABLE products ADD COLUMN IF NOT EXISTS shipping_price NUMERIC(19,2) NOT NULL DEFAULT 15000;
