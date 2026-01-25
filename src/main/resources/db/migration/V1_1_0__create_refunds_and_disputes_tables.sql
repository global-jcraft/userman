-- Refunds table
CREATE TABLE refunds (
    id BIGSERIAL PRIMARY KEY,
    stripe_refund_id VARCHAR(255) UNIQUE NOT NULL,
    stripe_charge_id VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    reason VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_refunds_user FOREIGN KEY (user_id) REFERENCES huddey_core.users(id)
);

CREATE INDEX idx_refunds_user_id ON refunds(user_id);
CREATE INDEX idx_refunds_stripe_refund_id ON refunds(stripe_refund_id);
CREATE INDEX idx_refunds_stripe_charge_id ON refunds(stripe_charge_id);

-- Disputes table
CREATE TABLE disputes (
    id BIGSERIAL PRIMARY KEY,
    stripe_dispute_id VARCHAR(255) UNIQUE NOT NULL,
    stripe_charge_id VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    reason VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    evidence_due_by TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_disputes_user FOREIGN KEY (user_id) REFERENCES huddey_core.users(id)
);

CREATE INDEX idx_disputes_user_id ON disputes(user_id);
CREATE INDEX idx_disputes_stripe_dispute_id ON disputes(stripe_dispute_id);
CREATE INDEX idx_disputes_stripe_charge_id ON disputes(stripe_charge_id);
CREATE INDEX idx_disputes_status ON disputes(status);
