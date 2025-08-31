-- Create webhook_events table for idempotency tracking
CREATE TABLE IF NOT EXISTS huddey_core.webhook_events (
    stripe_event_id VARCHAR(255) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PROCESSING', 'SUCCESS', 'FAILED')),
    error_message VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Index for cleanup queries
CREATE INDEX IF NOT EXISTS idx_webhook_events_processed_at ON huddey_core.webhook_events (processed_at);

-- Add comment for documentation
COMMENT ON TABLE huddey_core.webhook_events IS 'Tracks processed Stripe webhook events for idempotency';
COMMENT ON COLUMN huddey_core.webhook_events.stripe_event_id IS 'Unique Stripe event ID';
COMMENT ON COLUMN huddey_core.webhook_events.status IS 'Processing status: PROCESSING, SUCCESS, or FAILED';