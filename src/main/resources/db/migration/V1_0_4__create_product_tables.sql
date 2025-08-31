-- Create products table
CREATE TABLE IF NOT EXISTS huddey_core.products (
    id BIGSERIAL PRIMARY KEY,
    stripe_product_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create product_prices table
CREATE TABLE IF NOT EXISTS huddey_core.product_prices (
    id BIGSERIAL PRIMARY KEY,
    stripe_price_id VARCHAR(255) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    plan_id VARCHAR(255) NOT NULL,
    unit_amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    recurring_interval VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_prices_product_id FOREIGN KEY (product_id) REFERENCES huddey_core.products(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_products_stripe_product_id ON huddey_core.products(stripe_product_id);
CREATE INDEX idx_products_active ON huddey_core.products(active);
CREATE INDEX idx_product_prices_stripe_price_id ON huddey_core.product_prices(stripe_price_id);
CREATE INDEX idx_product_prices_product_id ON huddey_core.product_prices(product_id);
CREATE INDEX idx_product_prices_active ON huddey_core.product_prices(active);