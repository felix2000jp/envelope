CREATE TABLE IF NOT EXISTS note
(
    id         uuid PRIMARY KEY,
    appuser_id uuid NOT NULL,
    title      text NOT NULL,
    content    text NOT NULL
);