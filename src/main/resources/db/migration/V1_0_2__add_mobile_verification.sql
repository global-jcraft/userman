ALTER TABLE huddey_core.users
    ADD COLUMN phone_number_verified BOOLEAN DEFAULT FALSE;

ALTER TABLE huddey_core.users
    ADD COLUMN phone_number_verification_token VARCHAR(255);

ALTER TABLE huddey_core.users
    ADD COLUMN phone_number_verification_token_expires_at TIMESTAMP WITH TIME ZONE;