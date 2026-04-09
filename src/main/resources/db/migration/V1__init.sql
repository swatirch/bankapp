CREATE TABLE accounts (
    account_id      VARCHAR(36)     NOT NULL,
    account_number  VARCHAR(255)    NOT NULL,
    owner_name      VARCHAR(255)    NOT NULL,
    account_type    VARCHAR(20)     NOT NULL,
    balance         NUMERIC(19, 4)  NOT NULL,
    account_status  VARCHAR(20)     NOT NULL,
    created_at      TIMESTAMP       NOT NULL,

    CONSTRAINT pk_accounts PRIMARY KEY (account_id),
    CONSTRAINT uq_account_number UNIQUE (account_number)
);

CREATE TABLE transactions (
    transaction_id  VARCHAR(36)     NOT NULL,
    amount          NUMERIC(19, 4)  NOT NULL,
    type            VARCHAR(20)     NOT NULL,
    balance_after   NUMERIC(19, 4)  NOT NULL,
    description     VARCHAR(255)    NOT NULL,
    timestamp       TIMESTAMP       NOT NULL,
    account_id      VARCHAR(36)     NOT NULL,

    CONSTRAINT pk_transactions PRIMARY KEY (transaction_id),
    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);