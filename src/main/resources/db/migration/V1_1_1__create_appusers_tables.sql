CREATE TABLE IF NOT EXISTS appuser
(
    id       uuid PRIMARY KEY,
    username text UNIQUE NOT NULL,
    password text        NOT NULL
);

CREATE TABLE IF NOT EXISTS appuser_scopes
(
    appuser_id uuid REFERENCES appuser (id) ON DELETE CASCADE,
    scope      text NOT NULL,
    PRIMARY KEY (appuser_id, scope)
);