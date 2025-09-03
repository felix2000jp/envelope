CREATE TABLE IF NOT EXISTS account
(
    id      uuid PRIMARY KEY,
    user_id uuid           NOT NULL,
    name    text           NOT NULL,
    balance numeric(12, 2) NOT NULL,
    closed  boolean        NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_account_user_id ON account (user_id);
CREATE INDEX IF NOT EXISTS idx_account_id_user_id ON account (id, user_id);

CREATE TABLE IF NOT EXISTS transaction
(
    id                  uuid PRIMARY KEY,
    account_id          uuid           NOT NULL REFERENCES account (id) ON DELETE CASCADE,
    amount              numeric(12, 2) NOT NULL,
    date_of_transaction date           NOT NULL,
    memo                text           NOT NULL,
    cleared             boolean        NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_transaction_account_id ON transaction (account_id);