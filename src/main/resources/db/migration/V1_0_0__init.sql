-- V1.0.0__create_initial_schema.sql
CREATE SCHEMA IF NOT EXISTS huddey_core;

CREATE TABLE huddey_core.roles
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE huddey_core.auth_providers
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,
    active     BOOLEAN                  DEFAULT TRUE,
    config     JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE huddey_core.users
(
    id                                  BIGSERIAL PRIMARY KEY,
    email                               VARCHAR(255) NOT NULL UNIQUE,
    email_verified                      BOOLEAN                  DEFAULT FALSE,
    email_verification_token            VARCHAR(255),
    email_verification_token_expires_at TIMESTAMP WITH TIME ZONE,
    first_name                          VARCHAR(100),
    last_name                           VARCHAR(100),
    profile_picture_url                 TEXT,
    company_name                        VARCHAR(200),
    phone_number                        VARCHAR(50),
    status                              VARCHAR(20)  NOT NULL    DEFAULT 'PENDING',
    registration_ip                     VARCHAR(45),
    last_login_ip                       VARCHAR(45),
    last_login_at                       TIMESTAMP WITH TIME ZONE,
    last_logout_at                      TIMESTAMP WITH TIME ZONE,
    created_at                          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at                          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE huddey_core.user_credentials
(
    id                              BIGSERIAL PRIMARY KEY,
    user_id                         BIGINT       NOT NULL REFERENCES huddey_core.users (id),
    auth_provider_id                BIGINT       NOT NULL REFERENCES huddey_core.auth_providers (id),
    identifier                      VARCHAR(255) NOT NULL, -- email for local auth, provider's user id for social
    password_hash                   VARCHAR(255),          -- NULL for social auth
    password_reset_token            VARCHAR(255),
    password_reset_token_expires_at TIMESTAMP WITH TIME ZONE,
    created_at                      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (auth_provider_id, identifier)
);

CREATE TABLE huddey_core.social_connections
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL REFERENCES huddey_core.users (id),
    auth_provider_id  BIGINT       NOT NULL REFERENCES huddey_core.auth_providers (id),
    provider_user_id  VARCHAR(255) NOT NULL,
    provider_email    VARCHAR(255),
    provider_username VARCHAR(255),
    access_token      TEXT,
    refresh_token     TEXT,
    token_expires_at  TIMESTAMP WITH TIME ZONE,
    provider_raw_data JSONB,
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (auth_provider_id, provider_user_id)
);

CREATE TABLE huddey_core.user_roles
(
    user_id    BIGINT REFERENCES huddey_core.users (id),
    role_id    BIGINT REFERENCES huddey_core.roles (id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

INSERT INTO huddey_core.roles (name, description)
VALUES ('ROLE_USER', 'Basic user role for all registered users'),
       ('ROLE_ADMIN', 'Administrator role'),
       ('ROLE_MENTOR', 'Mentor role for Creative Academy'),
       ('ROLE_CONTENT_CREATOR', 'Content creator role');

INSERT INTO huddey_core.auth_providers (name, config)
VALUES ('local', '{
  "type": "email"
}'::jsonb),
       ('google', '{
         "type": "oauth2"
       }'::jsonb),
       ('facebook', '{
         "type": "oauth2"
       }'::jsonb),
       ('twitter', '{
         "type": "oauth2"
       }'::jsonb),
       ('linkedin', '{
         "type": "oauth2"
       }'::jsonb);

CREATE INDEX IF NOT EXISTS idx_users_email ON huddey_core.users (LOWER(email));
CREATE INDEX IF NOT EXISTS idx_users_status ON huddey_core.users (status);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON huddey_core.user_roles (user_id);
CREATE INDEX IF NOT EXISTS idx_user_credentials_user_id ON huddey_core.user_credentials (user_id);
CREATE INDEX IF NOT EXISTS idx_user_credentials_identifier ON huddey_core.user_credentials (identifier);
CREATE INDEX IF NOT EXISTS idx_social_connections_user_id ON huddey_core.social_connections (user_id);
CREATE INDEX IF NOT EXISTS idx_social_connections_provider_user ON huddey_core.social_connections (auth_provider_id, provider_user_id);
CREATE INDEX IF NOT EXISTS idx_users_email_verification_token ON huddey_core.users (email_verification_token) WHERE email_verification_token IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_phone_number ON huddey_core.users (phone_number) WHERE phone_number IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_user_credentials_auth_provider_identifier ON huddey_core.user_credentials (auth_provider_id, identifier);
-- Add performance indexes for login operations
CREATE INDEX IF NOT EXISTS idx_users_email_status_verified  ON huddey_core.users (email, status, email_verified)  WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_user_credentials_identifier_provider ON huddey_core.user_credentials (identifier, auth_provider_id) WHERE password_hash IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_user_credentials_password_reset_token ON huddey_core.user_credentials (password_reset_token) WHERE password_reset_token IS NOT NULL;

CREATE SEQUENCE IF NOT EXISTS huddey_core.user_id_seq START WITH 1000000 INCREMENT BY 1 NO CYCLE CACHE 1;
ALTER TABLE huddey_core.users ALTER COLUMN id SET DEFAULT nextval('huddey_core.user_id_seq');

-- create audit triggers
CREATE OR REPLACE FUNCTION huddey_core.update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE
    ON huddey_core.users
    FOR EACH ROW
EXECUTE FUNCTION huddey_core.update_updated_at_column();

CREATE TRIGGER update_user_credentials_updated_at
    BEFORE UPDATE
    ON huddey_core.user_credentials
    FOR EACH ROW
EXECUTE FUNCTION huddey_core.update_updated_at_column();

CREATE TRIGGER update_social_connections_updated_at
    BEFORE UPDATE
    ON huddey_core.social_connections
    FOR EACH ROW
EXECUTE FUNCTION huddey_core.update_updated_at_column();