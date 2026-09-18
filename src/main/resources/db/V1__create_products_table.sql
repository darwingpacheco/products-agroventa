CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(100) NOT NULL,
    quantity NUMERIC(19, 2) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    total_price NUMERIC(19, 2) NOT NULL,
    origin_location VARCHAR(255) NOT NULL,
    image_urls TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    status VARCHAR(20) NOT NULL,
    CONSTRAINT chk_products_quantity_positive CHECK (quantity >= 0.01),
    CONSTRAINT chk_products_unit_price_positive CHECK (unit_price >= 0.01),
    CONSTRAINT chk_products_total_price_positive CHECK (total_price >= 0.01),
    CONSTRAINT chk_products_status CHECK (status IN ('EN_VENTA', 'VENDIDO'))
);

CREATE INDEX IF NOT EXISTS idx_products_status ON products (status);
CREATE INDEX IF NOT EXISTS idx_products_seller_id ON products (seller_id);
CREATE INDEX IF NOT EXISTS idx_products_category ON products (category);
