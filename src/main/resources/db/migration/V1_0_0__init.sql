-- V1.0.0__create_initial_schema.sql
CREATE SCHEMA IF NOT EXISTS huddey_userman;

-- V1.0.1__create_roles_table.sql
CREATE TABLE huddey_userman.roles
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- V1.0.2__create_auth_providers_table.sql
CREATE TABLE huddey_userman.auth_providers
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,
    active     BOOLEAN                  DEFAULT TRUE,
    config     JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- V1.0.3__create_users_table.sql
CREATE TABLE huddey_userman.users
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
    created_at                          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at                          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- V1.0.4__create_user_credentials_table.sql
CREATE TABLE huddey_userman.user_credentials
(
    id                              BIGSERIAL PRIMARY KEY,
    user_id                         BIGINT       NOT NULL REFERENCES huddey_userman.users (id),
    auth_provider_id                BIGINT       NOT NULL REFERENCES huddey_userman.auth_providers (id),
    identifier                      VARCHAR(255) NOT NULL, -- email for local auth, provider's user id for social
    password_hash                   VARCHAR(255),          -- NULL for social auth
    password_reset_token            VARCHAR(255),
    password_reset_token_expires_at TIMESTAMP WITH TIME ZONE,
    created_at                      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (auth_provider_id, identifier)
);

-- V1.0.5__create_social_connections_table.sql
CREATE TABLE huddey_userman.social_connections
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL REFERENCES huddey_userman.users (id),
    auth_provider_id  BIGINT       NOT NULL REFERENCES huddey_userman.auth_providers (id),
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

-- V1.0.6__create_user_roles_table.sql
CREATE TABLE huddey_userman.user_roles
(
    user_id    BIGINT REFERENCES huddey_userman.users (id),
    role_id    BIGINT REFERENCES huddey_userman.roles (id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

-- V1.1.0__insert_initial_roles.sql
INSERT INTO huddey_userman.roles (name, description)
VALUES ('ROLE_USER', 'Basic user role for all registered users'),
       ('ROLE_ADMIN', 'Administrator role'),
       ('ROLE_MENTOR', 'Mentor role for Creative Academy'),
       ('ROLE_CONTENT_CREATOR', 'Content creator role');

-- V1.1.1__insert_auth_providers.sql
INSERT INTO huddey_userman.auth_providers (name, config)
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

-- V1.2.0__create_indexes.sql
CREATE INDEX idx_users_email ON huddey_userman.users (email);
CREATE INDEX idx_users_status ON huddey_userman.users (status);
CREATE INDEX idx_user_roles_user_id ON huddey_userman.user_roles (user_id);
CREATE INDEX idx_user_credentials_user_id ON huddey_userman.user_credentials (user_id);
CREATE INDEX idx_user_credentials_identifier ON huddey_userman.user_credentials (identifier);
CREATE INDEX idx_social_connections_user_id ON huddey_userman.social_connections (user_id);
CREATE INDEX idx_social_connections_provider_user ON huddey_userman.social_connections (auth_provider_id, provider_user_id);

-- V1.2.1__create_audit_triggers.sql
CREATE OR REPLACE FUNCTION huddey_userman.update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE
    ON huddey_userman.users
    FOR EACH ROW
EXECUTE FUNCTION huddey_userman.update_updated_at_column();

CREATE TRIGGER update_user_credentials_updated_at
    BEFORE UPDATE
    ON huddey_userman.user_credentials
    FOR EACH ROW
EXECUTE FUNCTION huddey_userman.update_updated_at_column();

CREATE TRIGGER update_social_connections_updated_at
    BEFORE UPDATE
    ON huddey_userman.social_connections
    FOR EACH ROW
EXECUTE FUNCTION huddey_userman.update_updated_at_column();