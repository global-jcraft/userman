CREATE TABLE huddey_core.user_subscriptions
(
    id                     BIGSERIAL PRIMARY KEY,
    user_id                BIGINT NOT NULL,
    stripe_customer_id     VARCHAR(255),
    stripe_subscription_id VARCHAR(255),
    plan_key               VARCHAR(50),
    pending_plan_key       VARCHAR(50),
    interval               VARCHAR(50),
    currency               VARCHAR(50),
    billing_interval       VARCHAR(50),
    seat_count             BIGINT,
    status                 VARCHAR(50),
    created_at             TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    current_period_start   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    current_period_end     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    cancel_at_period_end   BOOLEAN DEFAULT FALSE
);

CREATE SEQUENCE IF NOT EXISTS huddey_core.user_subscriptions_id_seq START WITH 1000000 INCREMENT BY 1 NO CYCLE CACHE 1;

CREATE INDEX idx_user_subscriptions_user_id ON huddey_core.user_subscriptions (user_id);
CREATE INDEX idx_user_subscriptions_stripe_customer_id ON huddey_core.user_subscriptions (stripe_customer_id);
CREATE INDEX idx_user_subscriptions_stripe_subscription_id ON huddey_core.user_subscriptions (stripe_subscription_id);
CREATE INDEX idx_user_subscriptions_status ON huddey_core.user_subscriptions (status);
CREATE INDEX idx_user_subscriptions_plan ON huddey_core.user_subscriptions (plan_key);