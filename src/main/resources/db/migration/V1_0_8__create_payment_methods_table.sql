CREATE TABLE IF NOT EXISTS huddey_core.payment_methods (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stripe_payment_method_id VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    last_four VARCHAR(4),
    brand VARCHAR(50),
    exp_month INTEGER,
    exp_year INTEGER,
    is_default BOOLEAN DEFAULT FALSE,
    is_backup BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_methods_user_id ON huddey_core.payment_methods(user_id);
CREATE INDEX idx_payment_methods_default ON huddey_core.payment_methods(user_id, is_default) WHERE is_default = TRUE;
CREATE INDEX idx_payment_methods_backup ON huddey_core.payment_methods(user_id, is_backup) WHERE is_backup = TRUE;
CREATE INDEX idx_payment_methods_expiration ON huddey_core.payment_methods(exp_year, exp_month);