CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ─── Allowed destination cities ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS allowed_cities (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(150) NOT NULL,
    department VARCHAR(150) NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_allowed_cities_name UNIQUE (name)
);

INSERT INTO allowed_cities (name, department) VALUES
    ('Cúcuta',        'Norte de Santander'),
    ('Bucaramanga',   'Santander'),
    ('Bogotá',        'Cundinamarca'),
    ('Medellín',      'Antioquia'),
    ('Cali',          'Valle del Cauca'),
    ('Barranquilla',  'Atlántico')
ON CONFLICT (name) DO NOTHING;

-- ─── Products ────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS products (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id             UUID         NOT NULL,
    name                  VARCHAR(255) NOT NULL,
    description           TEXT,
    category              VARCHAR(100) NOT NULL,
    quantity              NUMERIC(19,2) NOT NULL,
    reserved_quantity     NUMERIC(19,2) NOT NULL DEFAULT 0,
    unit                  VARCHAR(50)  NOT NULL,
    unit_price            NUMERIC(19,2) NOT NULL,
    total_price           NUMERIC(19,2) NOT NULL,
    min_shipment_quantity NUMERIC(19,2) NOT NULL,
    origin_location       VARCHAR(255) NOT NULL,
    destination_cities    TEXT[]       NOT NULL DEFAULT ARRAY[]::TEXT[],
    image_urls            TEXT[]       NOT NULL DEFAULT ARRAY[]::TEXT[],
    status                VARCHAR(20)  NOT NULL DEFAULT 'EN_VENTA',
    harvest_date          DATE,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_products_quantity_positive        CHECK (quantity >= 0),
    CONSTRAINT chk_products_unit_price_positive      CHECK (unit_price >= 0.01),
    CONSTRAINT chk_products_total_price_positive     CHECK (total_price >= 0.01),
    CONSTRAINT chk_products_min_shipment             CHECK (min_shipment_quantity >= 0.01),
    CONSTRAINT chk_products_reserved_lte_quantity    CHECK (reserved_quantity <= quantity),
    CONSTRAINT chk_products_status CHECK (status IN ('EN_VENTA','PAUSADO','AGOTADO'))
);

CREATE INDEX IF NOT EXISTS idx_products_status    ON products (status);
CREATE INDEX IF NOT EXISTS idx_products_seller_id ON products (seller_id);
CREATE INDEX IF NOT EXISTS idx_products_category  ON products (category);

-- ─── Orders ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS orders (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id       UUID         NOT NULL REFERENCES products(id),
    buyer_id         UUID         NOT NULL,
    seller_id        UUID         NOT NULL,
    quantity         NUMERIC(19,2) NOT NULL,
    unit_price       NUMERIC(19,2) NOT NULL,
    subtotal         NUMERIC(19,2) NOT NULL,
    shipping_cost    NUMERIC(19,2) NOT NULL DEFAULT 0,
    total            NUMERIC(19,2) NOT NULL,
    destination_city VARCHAR(150) NOT NULL,
    shipping_address TEXT         NOT NULL,
    status           VARCHAR(30)  NOT NULL DEFAULT 'PENDIENTE_PAGO',
    payment_status   VARCHAR(30)  NOT NULL DEFAULT 'PENDIENTE',
    reservation_expires_at TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_orders_quantity_positive  CHECK (quantity >= 0.01),
    CONSTRAINT chk_orders_total_positive     CHECK (total >= 0),
    CONSTRAINT chk_orders_status CHECK (status IN (
        'PENDIENTE_PAGO','PAGADO','EN_PREPARACION','DESPACHADO','ENTREGADO','CANCELADO')),
    CONSTRAINT chk_orders_payment_status CHECK (payment_status IN (
        'PENDIENTE','APROBADO','RECHAZADO','REEMBOLSADO'))
);

CREATE INDEX IF NOT EXISTS idx_orders_product_id ON orders (product_id);
CREATE INDEX IF NOT EXISTS idx_orders_buyer_id   ON orders (buyer_id);
CREATE INDEX IF NOT EXISTS idx_orders_seller_id  ON orders (seller_id);
CREATE INDEX IF NOT EXISTS idx_orders_status     ON orders (status);

-- ─── Payments ────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS payments (
    id                       UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id                 UUID         NOT NULL REFERENCES orders(id),
    provider                 VARCHAR(50)  NOT NULL DEFAULT 'WOMPI',
    provider_transaction_id  VARCHAR(255) UNIQUE,
    amount                   NUMERIC(19,2) NOT NULL,
    currency                 VARCHAR(10)  NOT NULL DEFAULT 'COP',
    status                   VARCHAR(30)  NOT NULL DEFAULT 'PENDIENTE',
    payment_method           VARCHAR(50),
    provider_response        JSONB,
    processed_at             TIMESTAMPTZ,
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_payments_status CHECK (status IN (
        'PENDIENTE','APROBADO','RECHAZADO','REEMBOLSADO'))
);

CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments (order_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_provider_tx_id
    ON payments (provider_transaction_id)
    WHERE provider_transaction_id IS NOT NULL;

-- ─── Shipments ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS shipments (
    id                      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id              UUID         NOT NULL REFERENCES products(id),
    total_quantity          NUMERIC(19,2) NOT NULL DEFAULT 0,
    min_quantity_threshold  NUMERIC(19,2) NOT NULL,
    status                  VARCHAR(30)  NOT NULL DEFAULT 'ACUMULANDO',
    ready_at                TIMESTAMPTZ,
    dispatched_at           TIMESTAMPTZ,
    tracking_info           TEXT,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_shipments_status CHECK (status IN (
        'ACUMULANDO','LISTO_PARA_DESPACHAR','DESPACHADO'))
);

CREATE INDEX IF NOT EXISTS idx_shipments_product_id ON shipments (product_id);
CREATE INDEX IF NOT EXISTS idx_shipments_status     ON shipments (status);

-- ─── Order ↔ Shipment (M:M) ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS order_shipments (
    order_id    UUID NOT NULL REFERENCES orders(id),
    shipment_id UUID NOT NULL REFERENCES shipments(id),
    PRIMARY KEY (order_id, shipment_id)
);
