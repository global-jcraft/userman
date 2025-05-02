DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM pg_type
                       WHERE typname = 'notification_status_enum') THEN
            CREATE TYPE notification_status_enum AS ENUM ('NEW', 'QUEUED', 'SENT', 'DELIVERED', 'FAILED');
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM pg_type
                       WHERE typname = 'notification_type_enum') THEN
            CREATE TYPE notification_type_enum AS ENUM ('SMS', 'EMAIL');
        END IF;
    END
$$;

CREATE TABLE IF NOT EXISTS huddey_core.notifications
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           VARCHAR(255)             NOT NULL,
    notification_type notification_type_enum   NOT NULL,
    recipient         VARCHAR(255)             NOT NULL,
    subject           VARCHAR(255)             NOT NULL,
    message           TEXT                     NOT NULL,
    sent_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    is_read           BOOLEAN                  NOT NULL DEFAULT FALSE,
    status            notification_status_enum,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE
);